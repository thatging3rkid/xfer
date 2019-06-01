package xfer.server;

import java.io.File;
import xfer.KeyFile;

public class xfer_server {

    public static void usage() {
        System.out.println("ssage: xfer_server [directory] [public-key]");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        // List debugging info
        System.out.println("xfer_utils (file transfer) server");

        // Check command-line arguments
        if (args.length != 2) {
            usage();
        }

        File directory = new File(args[0]);
        KeyFile pubkey = new KeyFile(new File(args[1]));

        // Start accepting connections
        try {
            ServerModel model = new ServerModel(directory);
            ServerNetworkThread net = new ServerNetworkThread(model, pubkey);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
