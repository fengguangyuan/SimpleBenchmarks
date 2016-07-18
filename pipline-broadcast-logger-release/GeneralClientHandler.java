import java.util.Random;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class GeneralClientHandler implements Runnable {
    protected static final long timeout = 300 * 1000; // timeout is 30 seconds
    protected static final int len_size = 4;
    protected static int blocks = 1024;
    protected final Random rand;
    protected TcpChannel channel;
    protected MyLogger logger;

    protected final int id;
    protected String serverName = null;
    protected String ip = null;
    protected int port = 0;

    // size of bytes
    protected SharedBuffer sb = SharedBuffer.getInstance();
    protected final int buf_size = sb.DEFAULT_BUFFER_SIZE;
    // flag to controlling the lifetime
    protected boolean working = true;

    protected GeneralClientHandler(int id, MyLogger logger) throws IOException {
        this.id = id;
        rand = new Random();
        channel = new TcpChannel(SocketChannel.open(), System.currentTimeMillis() + timeout, SelectionKey.OP_WRITE, logger);
    }

    protected GeneralClientHandler(int id, String serverName, MyLogger logger) throws IOException {
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
        work();
    }

    @Override
    public void run() {
        try {
            channel.connect(new InetSocketAddress(ip, port));
            int i = 0;
            // reading the amount of buffer blocks from context
            if (Context.conf.otherConfMap().containsKey("blocks")) {
                blocks = Integer.parseInt(Context.conf.otherConfMap().get("blocks"));
            }
            for (int j = 0; j < blocks; j++) {
                work();
                if ((++i & 16383) == 0) {
                    System.out.println(String.format("client(%1$d): %2$d", id, i));
                }
                // Thread.yield();
            }
            stop();
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
