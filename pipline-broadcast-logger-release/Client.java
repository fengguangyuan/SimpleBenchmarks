import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.DigestException;
import java.security.MessageDigest;
import java.util.Random;
import java.util.Iterator;
import java.util.HashMap;

import java.util.logging.Level;
 
public class Client {
    private static int id = 0;
    private static MyLogger logger = new MyLogger("../logs/client.log");

    public static void main(String[] args) throws Exception {
        // mode of pipline
        if (Context.conf.isPipeline()) {
            logger.log(Level.INFO, ">Pipeline< working mode selected!!!");
            new Thread(new PiplineClientHandler(id++, Context.conf.getDefaultServerName())).start();
            // new Thread(new ClientHandler(id++)).start();
        } else if (Context.conf.isBroadcast()) {
            logger.log(Level.INFO, ">Broadcast< working mode selected!!!");
            // mode of broadcast
            HashMap<String, String> nameToIp = Context.conf.nameToIp();
            Iterator it = nameToIp.keySet().iterator();
            while(it.hasNext()) {
                String serverName = (String) it.next();
                // new Thread(new ClientHandler(id++, "DataNode2")).start();
                new Thread(new BroadcastClientHandler(id++, serverName)).start();
            }
        }
    }
}

class ClientHandler implements Runnable {
    private static final long timeout = 300 * 1000; // timeout is 30 seconds
    private final TcpChannel channel;
    private MyLogger logger;

    private final int id;
    private String ip = "127.0.0.1";
    private int port = 5656;

    private final Random rand;
    // size of bytes
    private SharedBuffer sb = SharedBuffer.getInstance();
    private final int buf_size = sb.DEFAULT_BUFFER_SIZE;
    private final int len_size = 4;
    // if working is false, it means all data has been sent and the last buffer
    // with 0 length, should be sent.
    private int blocks = 1000; // default blocks
    private boolean working = true;

    ClientHandler(int id, MyLogger logger) throws Exception {
        this.id = id;
        this.logger = logger;
        channel = new TcpChannel(SocketChannel.open(), System.currentTimeMillis() + timeout, SelectionKey.OP_WRITE, logger);
        rand = new Random();
    }
 
    ClientHandler(int id, String serverName, MyLogger logger) throws Exception {
        this.id = id;
        this.logger = logger;
        this.ip = Context.conf.getIp(serverName);
        this.port = Context.conf.getPort(serverName);
        channel = new TcpChannel(SocketChannel.open(), System.currentTimeMillis() + timeout, SelectionKey.OP_WRITE, logger);
        rand = new Random();
    }
    private void stop() throws IOException {
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

            logger.log(Level.INFO, blocks + " * 1M's data will be transmitted!");
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

    private void work() throws IOException {
        // first 4 bytes represent the length of the valid data
        byte[] cache = new byte[buf_size], reply = new byte[5];
        write(cache, reply);
    }
 
    private void write(byte[] cache, byte[] reply) throws IOException {
        byte[] data = sb.getServerBuffer();

        // copy locat data to shared buffer
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
