import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.InetSocketAddress;

public class DatanodeClientHandler extends GeneralClientHandler {

    DatanodeClientHandler(int id) throws Exception {
        super(id);
    }

    DatanodeClientHandler(int id, String serverName) throws Exception {
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
        byte[] data = sb.getServerBuffer();
        rand.nextBytes(cache);

        // write data length into cache[]
        byte[] len = Util.intToBytes(buf_size - len_size);
        if (working) {
            for (int i = 0; i < 4; i++) cache[i] = len[i];
        } else {
            for (int i = 0; i < 4; i++) cache[i] = 0;
        }

        // copy locat data to shared buffer
        synchronized(data) {
            System.arraycopy(cache, 0, data, 0, buf_size);
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
