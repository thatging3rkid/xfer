package xfer.server;

import java.io.File;
import xfer.KeyFile;

public class xfer_server {

    public static void usage() {
        System.out.println("ssage: xfer_server [public-key]");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        // List debugging info
        System.out.println("xfer_utils (file transfer) server");

        // Check command-line arguments
        if (args.length != 1) {
            usage();
        }

        KeyFile pubkey = new KeyFile(new File(args[0]));

        // Start accepting connections
        try {
            ServerNetworkThread net = new ServerNetworkThread();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
