import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import java.util.logging.Level;

class Context {
    private static int block_size = 1024 * 1024;
    private String fileLocation = "../conf.txt";
    private HashMap<String, String> nameToIP = new HashMap<String, String>();
    private HashMap<String, Integer> nameToPort = new HashMap<String, Integer>();
    // other configurations
    private HashMap<String, String> otherConf = new HashMap<String, String>();
    private int serverCnts = 0;
    private String serverName = null;
    private String adjServerName = null;
    public static Context conf = new Context();

    private static final MyLogger logger = new MyLogger("../logs/context.log");

    private Context() {
        try {
            readConf(fileLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> nameToIp() {
        return nameToIP;
    }

    public HashMap<String, Integer> nameToPort() {
        return nameToPort;
    }

    public HashMap<String, String> otherConfMap() {
        return otherConf;
    }

    public String getIp(String name) {
        return nameToIP.get(name);
    }

    public int getPort(String name) {
        return nameToPort.get(name);
    }

    public String getDefaultServerName() {
        return serverName;
    }

    public String getDefaultAdjServerName() {
        return adjServerName;
    }

    public int getServerCnts() {
        return serverCnts;
    }

    public int getBlockSize() {
        if (otherConf.containsKey("blocksize")) {
            return Integer.parseInt(otherConf.get("blocksize"));
        } else {
            return block_size;
        }
    }

    public boolean isPipeline() {
        return otherConf.containsKey("pipeline") && otherConf.get("pipeline").equals("on");
    }

    public boolean isBroadcast() {
        return otherConf.containsKey("pipeline") && otherConf.get("pipeline").equals("off");
    }

    public void readConf(String filename) throws IOException {
        File file = new File(filename); 
        if (checkFile(file) != 0) {
            logger.log(Level.INFO, "Reading file error: Using default configuration!");
            return;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String str;
            String[] contents;
            int line = 1;
            while ((str = reader.readLine()) != null) {
                str = str.trim();
                if (str.indexOf('#') == 0) continue;
                // System.out.println(str);
                contents = str.split(" ");
                // System.out.println(contents.length);
                if (contents.length > 3) {
                    logger.log(Level.SEVERE, "Line " + line + " is incompatible in " + filename);
                    throw new IOException();
                }
                addItems(contents);
                line++;
            }
            checkServerName();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void addItems(String[] items) {
        // First, reading all the usable datanodes into hashmaps
        if (items.length ==3) {
            nameToIP.put(items[0], items[1]);
            nameToPort.put(items[0], Integer.parseInt(items[2]));
            serverCnts++;
        }
        // Second, reading server and adjacent server
        if (items.length == 1 && nameToIP.containsKey(items[0])) {
            serverName = items[0];
            adjServerName = null;
            return;
        }
        if (items.length == 2) {
            if (!nameToIP.containsKey(items[0])) {
                // Finally, reading running mode
                otherConf.put(items[0].toLowerCase(), items[1].toLowerCase());
                return;
            }
            serverName = items[0];
            adjServerName = items[1];
        }
    }

    private void checkServerName() {
        if (serverName == null || serverName != null && !nameToIP.containsKey(serverName)) {
            if (serverName != null) {
                logger.log(Level.SEVERE, "Server name " + serverName + " is invalid, please make sure "
                        + "it is described in the server list.");
            } else {
                logger.log(Level.INFO, "Server name is null, please make sure write a server name in "
                        + fileLocation);
            }
        }
        if (adjServerName != null && !nameToIP.containsKey(adjServerName)) {
            logger.log(Level.SEVERE, "Adjacent server name " + adjServerName + " is invalid, please make sure "
                    + "it is described in the server list.");
        }
    }

    private int checkFile(File file) {
        if (!file.isFile()) logger.log(Level.SEVERE, "File reading error: file name is wrong!");
        if (!file.exists()) logger.log(Level.SEVERE, "File reading error: file path is wrong!");
        if (file.isFile() && file.exists()) return 0;
        return 1;
    }
    @Override
    public String toString() {
        logger.log(Level.INFO, "Printing all the data nodes informations:");
        logger.log(Level.INFO, nameToIP.toString());
        logger.log(Level.INFO, nameToPort.toString());
        return nameToIP.toString() + nameToPort.toString();
    }
    public static void main(String[] args) {
        // Context.conf.toString();
    }
}
