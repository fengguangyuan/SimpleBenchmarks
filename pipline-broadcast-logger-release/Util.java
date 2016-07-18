public final class Util {

    public static byte[] intToBytes(int dec) {
        byte[] result = new byte[4];
        result[0] = (byte)((dec >> 24) & 0xFF);
        result[1] = (byte)((dec >> 16) & 0xFF);
        result[2] = (byte)((dec >> 8) & 0xFF);
        result[3] = (byte)(dec & 0xFF);
        return result;
    }

    public static int byteArrayToInt(byte[] bytes) {
        int value= 0;
        // from high to low
        for (int i = 0; i < 4; i++) {
            int shift= (4 - 1 - i) * 8;
            value +=(bytes[i] & 0x000000FF) << shift;
        }
        return value;
    }
}
