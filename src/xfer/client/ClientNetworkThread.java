package xfer.client;

import static xfer.Constants.MAX_LEN;
import static xfer.Constants.TRANSFER_PORT;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import xfer.Constants.Type;
import xfer.PacketData;

public class ClientNetworkThread {

    private DatagramSocket socket;
    private SocketAddress address;
    private List<PacketData> unacknowledged;

    private byte next_packetnum = 0;
    private Short id = null;

    private Queue<PacketData> recv_queue;

    public ClientNetworkThread(String host) throws IOException {
        socket = new DatagramSocket(TRANSFER_PORT);
        address = new InetSocketAddress(host, TRANSFER_PORT);

        this.unacknowledged = new CopyOnWriteArrayList<>();
        this.recv_queue = new ConcurrentLinkedQueue<>();

        new ReaderThread().start();
    }

    public String getIDString() {
        return (this.id == null)? "null" : String.format("%05d", this.id);
    }

    public synchronized void close() {
        this.socket.close();
    }

    public synchronized void send(Type type, byte[] params, byte[] data) throws Exception {
        // Resend packets if the unacknowledged list gets too big
        if (unacknowledged.size() >= 16) {
            resend();
        }

        // Make the packet data
        PacketData d = new PacketData(type, id, next_packetnum, params, data);
        byte[] bytes = d.toByteArray();
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, this.address);

        // Send the data and mark it as sent
        socket.send(packet);
        unacknowledged.add(d);
        next_packetnum += 1;
    }

    public synchronized byte[] receive(Type type, int timeout) throws Exception {
        long endtime = System.currentTimeMillis() + (timeout * 1000);
        PacketData match = null;

        loop:
        while (endtime > System.currentTimeMillis()) {
            for (PacketData pd : recv_queue) {
                // Look for an exact match
                if (pd.getType() == type) {
                    match = pd;
                    break loop;
                }

                // Need to check for an invalid packet
                if (pd.getType() == Type.INVALID && pd.getNumber() == next_packetnum) {
                    System.err.println("invalid parameters");
                    return null;
                }
            }

            Thread.sleep(20);
        }

        // See if a match was found, if so remove it and return the data
        if (match != null) {
            recv_queue.remove(match);
            return match.getData();
        }

        throw new TimeoutException("receive(): timed out");
    }

    private synchronized void resend() throws Exception {
        long endtime = System.currentTimeMillis() + (15 * 1000);

        while (endtime > System.currentTimeMillis() && unacknowledged.size() != 0) {
            for (PacketData pd : new ArrayList<>(unacknowledged)) {
                // Resend the packet
                byte[] bytes = pd.toByteArray();
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, this.address);
                socket.send(packet);
            }

            Thread.sleep(1000);
        }

        if (unacknowledged.size() != 0) {
            throw new IOException("resend() failed: list not empty after resending");
        }
    }

    private class ReaderThread extends Thread {
        @Override
        public void run() {
            try {
                byte[] buf = new byte[MAX_LEN];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                while (true) {
                    // Get the packet
                    socket.receive(packet);
                    byte[] data = new byte[packet.getLength()];
                    System.arraycopy(buf, 0, data, 0, packet.getLength());

                    // Parse the packet
                    PacketData pd = new PacketData(data);
                    switch (pd.getType()) {
                        case ACKNOWLEDGE:
                            // Search for a matching packet
                            int index = -1;
                            for (int i = 0; i < unacknowledged.size(); i += 1) {
                                if (pd.getClientID() == unacknowledged.get(i).getClientID()) {
                                    index = i;
                                    break;
                                }
                            }

                            // Remove if a match was found
                            if (index != -1) {
                                unacknowledged.remove(index);
                            }
                            break;

                        case RESP_INIT:
                            id = pd.getClientID();
                            break;
                    }

                    // Add it to the queue if it's not an acknowledge
                    if (pd.getType() != Type.ACKNOWLEDGE) {
                        recv_queue.add(pd);
                    }
                }

            } catch (IOException e) {
                // do nothing
            }
        }
    }
}
