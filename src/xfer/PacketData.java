package xfer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import xfer.Constants.Type;

/**
 * Defines a data packet, including conversion to byte arrays
 */
public class PacketData {
    private Type type;
    private byte version;
    private byte number;
    private short id;
    private byte[] params;
    private byte[] data;

    public PacketData(Type type, Short id, byte number, byte[] params, byte[] data) {
        this.type = type;
        this.version = Constants.XFER_VERSION;
        this.number = number;
        this.id = (id == null)? 0 : id;
        this.params = new byte[params.length];
        System.arraycopy(params, 0, this.params, 0, params.length);
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);

        // Calculate the size and check it
        if (size() > Constants.MAX_LEN) {
            throw new IllegalArgumentException("Too much data (size: " + size());
        }
    }

    public PacketData(byte[] data) {
        int endptr = 0;

        // Parse the type and version
        this.type = Type.valueOf(data[0]);
        this.version = data[1];
        endptr += 2;

        // Check the version
        if (this.version != Constants.XFER_VERSION) {
            throw new IllegalArgumentException("invalid byte array, versions mismatch");
        }

        // Get the client id and packet number
        this.number = data[2];
        ByteBuffer bb = ByteBuffer.wrap(data, endptr + 1, 2);
        bb.order(ByteOrder.BIG_ENDIAN);
        this.id = bb.getShort();
        endptr += 3;

        // Parse the parameter array
        int len = data[endptr];
        this.params = new byte[len];

        // Copy the data over
        System.arraycopy(data, endptr + 1, this.params, 0, len);
        endptr += 1 + len;

        // Need to parse the length of the data array
        bb = ByteBuffer.wrap(data, endptr, 4);
        bb.order(ByteOrder.BIG_ENDIAN);
        len = bb.getInt();

        // Copy the data over
        this.data = new byte[len];
        System.arraycopy(data, endptr + 4, this.data, 0, len);
    }

    public Type getType() {
        return type;
    }

    public byte getVersion() {
        return version;
    }

    public byte getNumber() {
        return number;
    }

    public short getClientID() {
        return id;
    }

    public byte[] getParams() {
        return params;
    }

    public byte[] getData() {
        return data;
    }

    private int size() {
        return 1 + // one byte for the type
            1 + // one byte for the version
            1 + // one byte for the number
            2 + // two bytes for the id
            (1 + this.params.length) + // one byte for the array length, then the parameters
            (4 + this.data.length); // four bytes for the array length, then the data
    }

    public byte[] toByteArray() {
        byte[] result = new byte[size()];
        int endptr = 0;

        // Basic parameters
        result[0] = (byte) this.type.ordinal();
        result[1] = this.version;
        result[2] = this.number;
        endptr += 3;

        // Need to write the id
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.putShort(id);
        bb.order(ByteOrder.BIG_ENDIAN);
        System.arraycopy(bb.array(), 0, result, endptr, bb.array().length);
        endptr += bb.array().length;

        // Copy parameter data
        result[endptr] = (byte) this.params.length;
        System.arraycopy(this.params, 0, result, endptr + 1, this.params.length);
        endptr += 1 + this.params.length;

        // Need to write the length of the array
        bb = ByteBuffer.allocate(4);
        bb.putInt(this.data.length);
        bb.order(ByteOrder.BIG_ENDIAN);
        System.arraycopy(bb.array(), 0, result, endptr, bb.array().length);
        endptr += bb.array().length;

        // Now, copy the data in the array
        System.arraycopy(this.data, 0, result, endptr, this.data.length);

        return result;
    }
}