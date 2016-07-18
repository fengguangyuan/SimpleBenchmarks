import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.CancelledKeyException;
import java.security.MessageDigest;
import java.util.Iterator;
import java.time.*;

import java.util.logging.Level;

public class ReceiveHandler implements Runnable {
    private static final long timeout = 300 * 1000;
    private MyLogger logger;
    private SharedBuffer sb = SharedBuffer.getInstance();
    private final int buf_size = sb.DEFAULT_BUFFER_SIZE;
    private static int counter = 0;
    private final TcpChannel channel;
    private boolean working = true;

    ReceiveHandler(SocketChannel channel, MyLogger logger) throws Exception {
        this.logger = logger;
        this.channel = new TcpChannel(channel, System.currentTimeMillis() + timeout, SelectionKey.OP_READ, logger);
    }

    public void run() {
        logger.log(Level.INFO, "ReceiveHandler is running......");
        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.now();
            logger.log(Level.INFO, "[START TIME]: " + startTime);
            while (working) {
                work();
                synchronized (ReceiveHandler.class) {
                    if ((++counter & 16383) == 0) {
                        System.out.println("DataNode2 is counting :" + counter);
                    }
                }
            }
            endTime = LocalTime.now();
            logger.log(Level.INFO, (counter - 1) + " data blocks has been received!!!!");
            logger.log(Level.INFO, "[END TIME]: " + endTime);
            logger.log(Level.INFO, "[TOTAL TIME]: " + (endTime.toSecondOfDay() - startTime.toSecondOfDay()) + "s");
        } catch (CancelledKeyException cke) {
            // Communication has ended. Do nothing
            stop();
            logger.log(Level.INFO, "All data has been received!!!!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            channel.cleanup();
        }
    }

    // allocate buffer to receive data from client
    private void work() throws IOException {
        byte[] cache = new byte[buf_size], reply = new byte[5];
        read(cache, reply);
    }

    private void read(byte[] cache, byte[] reply) throws IOException {
        // read data from channel
        ByteBuffer buff = ByteBuffer.wrap(sb.getServerBuffer());

        // Copy the shared data into local buffer, more efficient.
        synchronized(buff) {
            channel.recv(buff);
            System.arraycopy(sb.getServerBuffer(), 0, cache, 0, buf_size);
        }

        // some checks for the data
        int length = Util.byteArrayToInt(cache);
        // Avoid stucking in ServiceHandler, because of not clean up the previous data
        for (int i = 0; i < 4; i++) cache[i] = 0;
        // Stop working on reading socket
        if (length == 0) stop();

        // TODO: Should promote this in the future.
        if (length > 0 && length != buf_size - 4) logger.log(Level.SEVERE, "Data is wrong");
        // ACK
        // channel.send(buff);
    }

    private void stop() {
        this.working = false;
    }

}
