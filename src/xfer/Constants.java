package xfer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class Constants {

    /**
     * Defines the type of data being sent
     */
    public enum Type {
        // ack packet
        ACKNOWLEDGE,

        // initialization packets
        RQST_INIT,
        RESP_INIT,

        // file listing packets
        RQST_LIST,
        RESP_LIST,

        // download request packets
        DNLD_FILE,

        // upload request packets
        UPLD_FILE,

        // data packets
        DATA,

        // error packets
        INVALID;

        // Generate a hash map of entries
        private static Map<Integer, Type> entries = new HashMap<>();
        static {
            for (Type t : Type.values()) {
                entries.put(t.ordinal(), t);
            }
        }

        /**
         * Return the enum value for a given ordinal
         * @param ordinal the ordinal to look for
         * @return the enum value that matches the ordinal, or null if one does not exist
         */
        public static Type valueOf(int ordinal) {
            return entries.get(ordinal);
        }
    }

    public static final int XFER_VERSION = 1; // should make it so that this is loaded on startup
    public static final int MAX_LEN = 65507; // maximum total size
    public static final int TRANSFER_PORT = 4360;

}
