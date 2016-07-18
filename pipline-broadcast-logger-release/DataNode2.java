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

import java.util.logging.Level;
 
public class DataNode2 {
    private static String serverName = null;
    private static String adjServerName = null;
    private static String defaultName = Context.conf.getDefaultServerName();
    private static String defaultAdjServerName = Context.conf.getDefaultAdjServerName();
    private static MyLogger logger = new MyLogger("../logs/DataNode2.log");

    public static void main(String[] args) throws IOException {
        if (args.length == 2) {
            serverName = args[1];
            adjServerName = args[2];
        } else {
            serverName = defaultName;
            adjServerName = defaultAdjServerName;
        }

        Context.conf.readConf("../datanode2-conf/conf.txt");
        serverName = Context.conf.getDefaultServerName();
        adjServerName = Context.conf.getDefaultAdjServerName();

        // Create the selector for receiving data
        final Selector selector = Selector.open();
        final ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.socket().bind(new InetSocketAddress(Context.conf.getIp(serverName), Context.conf.getPort(serverName)), 5);
        // Register both channels with selector
        server.register(selector, SelectionKey.OP_ACCEPT);

        new Thread(new SocketDaemon(selector, serverName, adjServerName, logger)).start();
        logger.log(Level.INFO, "----- Listening " + serverName +
            ", port " + Context.conf.getPort(serverName) + " -----");
        logger.log(Level.INFO, "----- Adjcacent server name [" + adjServerName +
            "], port " + (adjServerName != null ? Context.conf.getPort(adjServerName) : 0) + " -----");
    }
}
