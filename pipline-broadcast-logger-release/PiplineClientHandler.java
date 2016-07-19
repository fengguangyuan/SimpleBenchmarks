import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.InetSocketAddress;

public class PiplineClientHandler extends GeneralClientHandler {
    public PiplineClientHandler(int id) throws IOException {
        super(id);
    }

    public PiplineClientHandler(int id, String serverName) throws IOException {
        super(id, serverName);
    }

    @Override
    protected void work() throws IOException {
        // first 4 bytes represent the length of the valid data
        byte[] cache = new byte[buf_size], reply = new byte[5];
        write(cache, reply);
    }

    private void write(byte[] cache, byte[] reply) throws IOException {
        // produce random bytes
        byte[] data = sb.getClientBuffer();
        // rand.nextBytes(cache);

        // copy data from shared buffer to local buffer
        synchronized(data) {
            System.arraycopy(data, 0, cache, 0, buf_size);
        }

        // write data length into cache[]
        byte[] len = Util.intToBytes(buf_size - len_size);
        if (working) {
            for (int i = 0; i < 4; i++) cache[i] = len[i];
        } else {
            for (int i = 0; i < 4; i++) cache[i] = 0;
        }

        ByteBuffer buffer = ByteBuffer.wrap(cache);
        channel.send(buffer);
        // buffer = ByteBuffer.wrap(reply);
        // channel.recv(buffer);
        // if (reply[0] != '.') { //
        //    System.out.println("MISMATCH!");
        // }
    }
}
