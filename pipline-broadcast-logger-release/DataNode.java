import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.net.InetSocketAddress;

import java.util.logging.Level;
 
public class DataNode {
    private static String serverName = null;
    private static String adjServerName = null;
    private static String defaultAdjServerName = Context.conf.getDefaultAdjServerName();
    private static String defaultName = Context.conf.getDefaultServerName();
    private static MyLogger logger = new MyLogger("../logs/DataNode.log");

    public static void main(String[] args) throws IOException {
        if (args.length == 2) {
            serverName = args[1];
            adjServerName = args[2];
        } else {
            serverName = defaultName;
            adjServerName = defaultAdjServerName;
        }
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
 
