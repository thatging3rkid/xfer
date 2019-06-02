package xfer.server;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import xfer.Utils;

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

        // Check for parameters
        if (params.length != 0) {
            p = Paths.get(p.toString(), new String(params));
        }

        // Make the directory to search in, make sure it's valid
        File dir = new File(p.toString());
        if (!dir.exists() || dir.listFiles() == null) {
            return null;
        }

        // Save the data
        tuple.data = Utils.listing(dir).getBytes(StandardCharsets.UTF_8);
        return tuple;
    }

}
