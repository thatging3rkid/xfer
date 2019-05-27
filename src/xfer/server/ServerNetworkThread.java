package xfer.server;

import static xfer.Constants.MAX_LEN;
import static xfer.Constants.TRANSFER_PORT;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import xfer.Constants.PacketData;
import xfer.Constants.Type;

public class ServerNetworkThread {

    private DatagramSocket socket;

    private short next_id = 10;

    public ServerNetworkThread() throws IOException {
        this.socket = new DatagramSocket(TRANSFER_PORT);

        new ReaderThread().start();
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
                        case RESP_INIT:
                            System.out.println("init recieved");
                            break;


                    }
                }

            } catch (IOException e) {
                // do nothing
            }
        }
    }
}
