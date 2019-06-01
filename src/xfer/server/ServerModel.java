package xfer.server;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class ServerModel {

    public static class DataTuple {
        public byte[] params = new byte[0];
        public byte[] data = new byte[0];
    }

    private File directory;

    public ServerModel(File directory) {
        this.directory = directory;
    }

    /**
     * Generate the data for a listing response
     * @return the params and data for the listing response
     */
    public DataTuple request_list(byte[] params) {
        DataTuple tuple = new DataTuple();
        Path p = directory.toPath();

        if (params.length != 0) {
            p = Paths.get(p.toString(), new String(params));
        }

        File dir = new File(p.toString());
        if (!dir.exists() || dir.listFiles() == null) {
            return null;
        }

        // Save data in a StringBuilder
        StringBuilder sb = new StringBuilder();
        sb.append("listing " + LocalDateTime.now());

        // Print the file information
        for (File f : dir.listFiles()) {
            sb.append((f.isDirectory())? 'd' : '-');
            sb.append("  ");
            sb.append(f.getName());
            sb.append("\n");
        }

        tuple.data = sb.toString().getBytes(StandardCharsets.UTF_8);

        return tuple;
    }

}
