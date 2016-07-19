import java.util.Random;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import java.util.logging.Level;

public class GeneralClientHandler implements Runnable {
    protected static MyLogger logger = new MyLogger("../logs/client.log");
    protected static final long timeout = 300 * 1000; // timeout is 30 seconds
    protected static final int len_size = 4;
    protected static int blocks = 1024;
    protected final Random rand;
    protected TcpChannel channel;

    protected final int id;
    protected String serverName = null;
    protected String ip = null;
    protected int port = 0;

    // size of bytes
    protected SharedBuffer sb = SharedBuffer.getInstance();
    protected final int buf_size = sb.DEFAULT_BUFFER_SIZE;
    // flag to controlling the lifetime
    protected boolean working = true;

    protected GeneralClientHandler(int id) throws IOException {
        this.id = id;
        rand = new Random();
        channel = new TcpChannel(SocketChannel.open(), System.currentTimeMillis() + timeout, SelectionKey.OP_WRITE, logger);
    }

    protected GeneralClientHandler(int id, String serverName) throws IOException {
        this.id = id;
        this.serverName = serverName;
        this.ip = getIp();
        this.port = getPort();
        rand = new Random();
        channel = new TcpChannel(SocketChannel.open(), System.currentTimeMillis() + timeout, SelectionKey.OP_WRITE, logger);
    }

    protected String getIp() {
        return Context.conf.getIp(serverName);
    }

    protected int getPort() {
        return Context.conf.getPort(serverName);
    }

    protected void stop() throws IOException {
        this.working = false;
    }

    @Override
    public void run() {
        try {
            channel.connect(new InetSocketAddress(ip, port));
            int i = 1;
            // reading the amount of buffer blocks from context
            if (Context.conf.otherConfMap().containsKey("blocks")) {
                blocks = Integer.parseInt(Context.conf.otherConfMap().get("blocks"));
            }
            while (working) {
                work();
                if ((++i & 16383) == 0) {
                    System.out.println(String.format("client(%1$d): %2$d", id, i));
                }
                // After sent all blocks, stop sending.
                if (i == blocks) stop();
                // Thread.yield();
            }
            // send a data block with 0 length to imply the end of data transmitting
            work();
            logger.log(Level.INFO, String.format("client(%1$d) has sent %2$d blocks!", id, i));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            channel.cleanup();
        }
    }

    protected void work() throws IOException {
        System.out.println("GeneralClientHandler is working.....!");
    }
}
