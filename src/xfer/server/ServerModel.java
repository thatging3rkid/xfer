package xfer.server;

import java.io.File;
import java.nio.charset.Charset;

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
        StringBuilder sb = new StringBuilder();

        for (File f : this.directory.listFiles()) {

            sb.append((f.isDirectory())? 'd' : '-');
            sb.append("  ");
            sb.append(f.getName());
        }

        tuple.data = sb.toString().getBytes(Charset.forName("UTF-8"));

        return tuple;
    }

}
