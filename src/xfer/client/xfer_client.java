package xfer.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import xfer.PacketData;
import xfer.Constants.Type;
import xfer.KeyFile;
import xfer.Utils;

public class xfer_client {

    public static void usage() {
        System.out.println("ssage: xfer_client [server] [directory] [private-key]");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        // List debugging info
        System.out.println("xfer_utils (file transfer) client");

        // Setup necessary variables
        String server = null;
        File directory = null;
        KeyFile privkey = null;

        // Check command-line arguments
        if (args.length != 0) {
            // Check to see if the user is asking for help
            if (args[0].equals("--help")) {
                usage();
            } else {
                server = args[0];
            }

            // Parse the directory parameter
            if (args.length >= 2) {
                directory = new File(args[1]);
            }

            // Parse for the private key
            if (args.length >= 3) {
                privkey = new KeyFile(new File(args[2]));
            }
        }

        // Ask the user to input the values if they didn't pass them in on the command line
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        if (server == null) {
            System.out.println("server: ");
            server = br.readLine();
        }

        if (directory == null) {
            System.out.println("output directory: ");
            directory = new File(br.readLine());
        }

        if (privkey == null) {
            System.out.println("private key file: ");
            privkey = new KeyFile(new File(br.readLine()));
        }

        // Make the output directory if it doesn't exist
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try {

            // Initialize the connection
            System.out.print("\n");
            ClientNetworkThread net = new ClientNetworkThread(server);
            net.send(Type.RQST_INIT, new byte[0], new byte[0]);
            net.receive(Type.RESP_INIT, 10);

            loop:
            while (true) {
                // Prompt the user
                System.out.print(net.getIDString() + ":" + server + "> ");
                String command = br.readLine();

                // Parse it and check the result
                List<String> split = Utils.parse(command);
                if (split == null) {
                    continue;
                }

                byte[] params = new byte[0];
                byte[] data = new byte[0];

                switch (split.get(0)) {
                    case "list":
                        // Pass in any arguments
                        if (split.size() > 1) {
                            params = split.get(1).getBytes(StandardCharsets.UTF_8);
                        }

                        // Send the request and wait for the response
                        net.send(Type.RQST_LIST, params, new byte[0]);
                        data = net.receive(Type.RESP_LIST, 10);

                        // Print the message
                        System.out.println(new String(data));
                        break;



                    case "quit":
                    case "exit":
                        break loop;

                    default:
                        System.err.println("Unknown command");
                        System.err.flush();
                        break;
                }

            }

            // Clean up resources
            br.close();
            net.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
