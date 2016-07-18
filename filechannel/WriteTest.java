import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.Random;
import java.time.LocalTime;
import java.time.LocalDateTime;

class WriteTest {
    public static Logger logger = Logger.getLogger(WriteTest.class.toString());
    public static String fileName = "data.d";
    private static final int byte_length = 100;
    private static Random rand = new Random();

    public static void main(String[] args) throws IOException, FileNotFoundException {
        init();

        // int len = 0;
        int len = 1024 * 10;
        // for (int i = 1; i <= 1024 * 1024 * 1 / byte_length; i++) {
        for (int i = 1; i <= 1024 * 1; i++) {
            if (i % 10 == 0) len *= 2;
            else if (i % 100 == 0) len *= 4;
            else len += byte_length;

            // RandomAccessFile aFile = new RandomAccessFile(fileName, "rw");
            File file = new File(fileName);
            if (!file.exists()) file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            FileChannel fc = fos.getChannel();
            if (len / 1000 == 0) {
                logger.log(Level.INFO, "---- Writing 1G file with " + (len % 1000) + " bits bytes data block ----");
            } else {
                logger.log(Level.INFO, "---- Writing 1G file with " + (len / 1000) +","+(len % 1000) + " bytes data block ----");
            }

            // LocalTime sTime, eTime;
            long lSTime, lETime;
            // sTime = LocalTime.now();
            lSTime = System.currentTimeMillis();
            byte[] data = new byte[len];
            rand.nextBytes(data);

            writeFile(fc, data, len);
            // fc.force(true);

            // eTime = LocalTime.now();
            lETime = System.currentTimeMillis();
            // logger.log(Level.INFO, "Start: "+sTime);
            // logger.log(Level.INFO, "End: "+eTime);
            // logger.log(Level.INFO, "---- Done! Total time: " +(eTime.toSecondOfDay() - sTime.toSecondOfDay())+ "s ----\n");
            // logger.log(Level.INFO, "---- Done! Total time: " +(eTime.toNanoOfDay() - sTime.toNanoOfDay())+ "s ----\n");
            logger.log(Level.INFO, "---- Done! Total time: " +(lETime - lSTime)+ "ms ----\n");
            fc.close();

            // delete file
            File dfile = new File(fileName);
            if (dfile.isFile() && dfile.exists()) {
                dfile.delete();
            }
        }
    }

    private static void init() throws IOException {
        // initialize logger
        FileHandler fh = new FileHandler("sample-big.log");
        MyFormatter mf = new MyFormatter();
        fh.setFormatter(mf);
        logger.addHandler(fh);
        logger.setLevel(Level.ALL);
        logger.info("Even recorded on " + LocalDateTime.now());
        // generate random bytes
    }

    private static void writeFile(FileChannel fc, byte[] data, long length) throws IOException, FileNotFoundException {
        ByteBuffer bb = ByteBuffer.wrap(data);
        for (long i = 0; i < 1024 * 1024 * 1024 * 5/ length; i++) {
            bb.clear();
            // bb.put(data);
            // bb.flip();
            while (bb.hasRemaining()) {
                fc.write(bb);
            }
            // fc.force(true);
        }
    }

}

class MyFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        return "[" + record.getLevel() + "]: " + record.getMessage() + "\n";
    }

}
