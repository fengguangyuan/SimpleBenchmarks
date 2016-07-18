import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.CancelledKeyException;
import java.util.Iterator;

import java.util.logging.Level;

class SocketDaemon implements Runnable {
    private final Selector selector;
    // adjacent DataNode info
    private String serverName = null;
    private String adjServerName = null;
    private boolean working = true;
    private MyLogger logger = null;

    SocketDaemon(Selector selector, MyLogger logger) {
        this.selector = selector;
        this.logger = logger;
    }
 
    SocketDaemon(Selector selector, String serverName, String adjServerName, MyLogger logger) {
        this.selector = selector;
        this.serverName = serverName;
        this.adjServerName = adjServerName;
        this.logger = logger;
    }

    private void stop() {
        this.working = false;
    }
 
    public void run() {
        while (working) {
            try {
                // Wait for an event
                selector.select();
 
                // Get list of selection keys with pending events
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
 
                // Process each key
                while (it.hasNext()) {
                    // Get the selection key
                    SelectionKey sKey = it.next();
 
                    // Remove it from the list to indicate that it is being processed
                    it.remove();
 
                    // Check if it's a connection request
                    if (sKey.isAcceptable()) {
                        // Get channel with connection request
                        ServerSocketChannel server = (ServerSocketChannel) sKey.channel();
                        // Accept the connection request.
                        // If serverSocketChannel is blocking, this method blocks.
                        // The returned channel is in blocking mode.
                        SocketChannel channel = server.accept();
 
                        // If serverSocketChannel is non-blocking, sChannel may be null
                        if (channel != null) {
                            // Use the socket channel to receive data from the client
                            // Also another thread sending data to the next datanode, should be called.
                            new Thread(new ReceiveHandler(channel, logger)).start();
                            logger.log(Level.INFO, serverName + " reading thread startups successfully!");

                            if (adjServerName != null) {
                                new Thread(new DatanodeClientHandler(100, adjServerName, logger)).start();
                                logger.log(Level.INFO, serverName + " client thread startups successfully!");
                            } else { 
                                logger.log(Level.INFO, "The last DataNode startup successfully!");
                            }
                        } else {
                            logger.log(Level.INFO, "---No Connection---");
                            // There were no pending connection requests; try again later.
                            // To be notified of connection requests,
                        }
                    }
                }
            } catch (CancelledKeyException cke) {
                // Communication has ended. Do nothing
                stop();
                logger.log(Level.INFO, "All data has been received!!!!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
 
