import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

import java.util.logging.Level;

final class TcpChannel {
    private long endTime;
    private SelectionKey key;
    private MyLogger logger;
    public Selector selector = null;
    public String ip;
    public int port;

    public TcpChannel(SelectableChannel channel, long endTime, int op, MyLogger logger) throws IOException {
        boolean done = false;
        this.logger = logger;
        // Selector selector = null;
        this.endTime = endTime;
        try {
            selector = Selector.open();
            channel.configureBlocking(false);
            key = channel.register(selector, op);
            done = true;
        } finally {
            if (!done && selector != null) {
                selector.close();
            }
            if (!done) {
                channel.close();
            }
        }
    }

    static void blockUntil(SelectionKey key, long endTime) throws IOException {
        long timeout = endTime - System.currentTimeMillis();
        int nkeys = 0;
        if (timeout > 0) {
            nkeys = key.selector().select(timeout);
        } else if (timeout == 0) {
            nkeys = key.selector().selectNow();
        }
        if (nkeys == 0) {
            key.selector().close();
            key.channel().close();
            throw new SocketTimeoutException();
        }
    }

    void cleanup() {
        try {
            key.selector().close();
            key.channel().close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void bind(SocketAddress addr) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.socket().bind(addr);
    }

    void connect(SocketAddress addr) throws IOException {
        logger.log(Level.INFO, "+++++++++++++ Start connecting to " +addr+ " ++++++++++++++");
        SocketChannel channel = (SocketChannel) key.channel();

        key.interestOps(key.interestOps() | SelectionKey.OP_CONNECT);

        try {
            if (!key.isConnectable()) {
                blockUntil(key, endTime);
            }
            if (!channel.connect(addr) && !channel.finishConnect()) {
                throw new ConnectException();
            }
        } finally {
            if (key.isValid()) {
                key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);
            }
        }
    }

    void send(ByteBuffer buffer) throws IOException {
        Send.operate(key, buffer, endTime);
    }

    void recv(ByteBuffer buffer) throws IOException {
        Recv.operate(key, buffer, endTime);
    }
}

interface Operator {
    class Operation {
        static void operate(final int op, final SelectionKey key, final ByteBuffer buffer, final long endTime, final Operator optr)
            throws IOException {
            final SocketChannel channel = (SocketChannel) key.channel();
            final int total = buffer.capacity();
            key.interestOps(op);
            try {
                // Make sure all the bytes in the buffer is read or written.
                //MyLogger.log(Level.INFO, "++++++++++++++++++++ One Buffer ++++++++++++++++++++");
                while (buffer.position() < total) {
                    //System.out.println("Position: " + buffer.position() + " Total: " + total);
                    if (System.currentTimeMillis() > endTime) {
                        // If a thread hasn't completed the mission in {endTime},
                        // the socket timeout exception will be emitted.
                        //MyLogger.log(Level.INFO, "Current time exceeds endTime!!!! Mission failed!!");
                        throw new SocketTimeoutException();
                    }
                    // When key.interestOps() is OP_READ or OP_WRITE, then do io(...).
                    if ((key.readyOps() & op) != 0) {
                        if (optr.io(channel, buffer) < 0) {
                            // If -1 is returned, it means the end of data is reached.
                            // key.channel().close();
                            // throw new EOFException();
                        }
                    } else {
                        TcpChannel.blockUntil(key, endTime);
                    }
                }
                //MyLogger.log(Level.INFO, "Position: " + buffer.position() + " Total: " + total);
                //MyLogger.log(Level.INFO, "++++++++++++++++++++ End ++++++++++++++++++++");
            } finally {
                if (key.isValid()) {
                    key.interestOps(0);
                }
            }
        }
    }
    // read or write
    int io(SocketChannel channel, ByteBuffer buffer) throws IOException;
}
class Send implements Operator {
    public int io(SocketChannel channel, ByteBuffer buffer) throws IOException {
        return channel.write(buffer);
    }
    public static final void operate(final SelectionKey key, final ByteBuffer buffer, final long endTime) throws IOException {
        Operation.operate(SelectionKey.OP_WRITE, key, buffer, endTime, send);
    }
    public static final Send send = new Send();
}

class Recv implements Operator {
    public int io(SocketChannel channel, ByteBuffer buffer) throws IOException {
        return channel.read(buffer);
    }

    public static final void operate(final SelectionKey key, final ByteBuffer buffer, final long endTime) throws IOException {
        Operation.operate(SelectionKey.OP_READ, key, buffer, endTime, recv);
    }
    public static final Recv recv = new Recv();
}
