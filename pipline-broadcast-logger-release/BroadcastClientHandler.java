import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.InetSocketAddress;

public class BroadcastClientHandler extends GeneralClientHandler {

    public BroadcastClientHandler(int id, MyLogger logger) throws IOException {
        // GeneralClientHandler(id);
        super(id, logger);
    }

    public BroadcastClientHandler(int id, String serverName, MyLogger logger) throws IOException {
        // GeneralClientHandler(id, serverName);
        super(id, serverName, logger);
    }

    @Override
    protected void work() throws IOException {
        // first 4 bytes represent the length of the valid data
        byte[] cache = new byte[buf_size], reply = new byte[5];
        write(cache, reply);
    }

    private void write(byte[] cache, byte[] reply) throws IOException {
        // produce random bytes
        byte[] data = sb.getClientBuffer(false);

        // copy received data to local buffer
        System.arraycopy(data, 0, cache, 0, buf_size);
        sb.setState();

        // write data length into cache[]
        byte[] len = Util.intToBytes(buf_size - len_size);
        if (working) {
            for (int i = 0; i < 4; i++) cache[i] = len[i];
        } else {
            for (int i = 0; i < 4; i++) cache[i] = 0;
        }

        ByteBuffer buffer = ByteBuffer.wrap(cache);
        channel.send(buffer);
    }

}
