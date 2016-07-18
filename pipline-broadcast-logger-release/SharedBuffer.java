import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.Random;

public class SharedBuffer {
    private static SharedBuffer instance = null;
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 1024 + 4;
    public static final int SERVER_COUNTS = Context.conf.getServerCnts();
    private byte[] clientData = new byte[DEFAULT_BUFFER_SIZE];
    private byte[] serverData = new byte[DEFAULT_BUFFER_SIZE];
    private boolean[] stats = new boolean[SERVER_COUNTS];

    private final Random rand;
    private int idx = 0;

    public static SharedBuffer getInstance() {
        if (instance == null) {
            synchronized(SharedBuffer.class) {
                if (instance == null) {
                    instance = new SharedBuffer();
                }
            }
        }
        return instance;
    }

    private SharedBuffer() {
        this.rand = new Random();
    }

    private synchronized void randomGenBytes() {
        boolean result = true;
        for (boolean b : stats) result &= b;
        if (result) rand.nextBytes(clientData);
    }

    private synchronized void randomGenBytes(boolean pipline) {
        if (pipline) {
            rand.nextBytes(clientData);
            return;
        }
        boolean result = true;
        for (boolean b : stats) result &= b;
        if (result) rand.nextBytes(clientData);
    }

    public synchronized void setState() {
        System.out.println("SERVER COUNT : " + SERVER_COUNTS);
        stats[idx++] = true;
        if (idx >= SERVER_COUNTS) resetAll();
    }

    public synchronized void resetAll() {
        for (int i = 0; i < stats.length; i++) stats[i] = false;
        idx = 0;
    }

    public byte[] getClientBuffer() throws IOException {
        randomGenBytes(true);
        return clientData;
    }

    public byte[] getClientBuffer(boolean pipline) throws IOException {
        randomGenBytes(pipline);
        return clientData;
    }

    public byte[] getServerBuffer() {
        return serverData;
    }

    public byte[] getSharedBuffer() {
        return clientData;
    }

    public byte[] getBuffer(int length) throws IOException {
            return (clientData = new byte[length]);
    }

}
