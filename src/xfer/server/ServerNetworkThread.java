package xfer.server;

import static xfer.Constants.MAX_LEN;
import static xfer.Constants.TRANSFER_PORT;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import xfer.Constants.PacketData;
import xfer.Constants.Type;
import xfer.KeyFile;
import xfer.server.ServerModel.DataTuple;

public class ServerNetworkThread {

    private DatagramSocket socket;

    private short next_id = 10;
    private Map<Short, Byte> number_map;

    private ServerModel model;
    private KeyFile key;

    public ServerNetworkThread(ServerModel model, KeyFile key) throws IOException {
        this.socket = new DatagramSocket(TRANSFER_PORT);
        this.number_map = new HashMap<>();

        this.model = model;
        this.key = key;

        new ReaderThread().start();
    }

    public synchronized void send(Type type, short id, byte[] params, byte[] data, SocketAddress addr) throws Exception {
        if (number_map.get(id) == null) {
            System.err.println("unknown id");
            System.err.flush();
            return;
        }

        // Make the packet data
        PacketData d = new PacketData(type, id, number_map.get(id), params, data);
        byte[] bytes = d.toByteArray();
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, addr);

        // Send the data and mark it as sent
        socket.send(packet);
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
                    SocketAddress addr = packet.getSocketAddress();

                    // Update the packet number map
                    if (pd.getNumber() != 0) {
                        number_map.put(pd.getID(), (byte) (pd.getNumber() + 1));
                    }

                    switch (pd.getType()) {
                        case RQST_INIT:
                            // Need to initialize the client and send a response
                            number_map.put(next_id, (byte) (pd.getNumber() + 1));

                            // Send a response
                            send(Type.RESP_INIT, next_id, new byte[0], new byte[0], addr);
                            next_id += 1;

                            break;

                        case RQST_LIST:
                            DataTuple tuple = model.request_list(pd.getParams());
                            send(Type.RESP_LIST, pd.getID(), tuple.params, tuple.data, addr);
                            break;

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                // do nothing
            }
        }
    }
}
