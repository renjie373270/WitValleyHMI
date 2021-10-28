package tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CRCTools {

    private static final Logger log = LoggerFactory.getLogger(CRCTools.class);

    private static String getCRC(byte[] bytes) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;

        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        int crcH = CRC & 0xFF;
        int crcL = (CRC >> 8) & 0xFF;
        int crcReturn = (crcH << 8) | crcL;
        return Integer.toHexString(crcReturn).toUpperCase();
    }

    public static boolean checkModbusCRC(byte[] inBytes, int length,String code) {
        byte[] bytes = new byte[length - 2];
        int i;
        for(i = 0; i < length - 2; i++) {
            bytes[i] = inBytes[i];
        }
        String crcHexString = getCRC(bytes);
        int crcH = (int)inBytes[length - 2] & 0x000000FF;
        int crcL = (int)inBytes[length - 1] & 0x000000FF;
        int crcInt = (crcH << 8) | crcL;
        boolean result =  crcHexString.equalsIgnoreCase(Integer.toHexString(crcInt));
        if(!result) {
            if (!"English".equals(code)) {

                log.error("Data verification failed, crcReceive = {}, crcCal = {}", Integer.toHexString(crcInt).toUpperCase(), crcHexString);
            }else {
                log.error("数据校验失败, crcReceive = {}, crcCal = {}", Integer.toHexString(crcInt).toUpperCase(), crcHexString);
            }

        }
        return result;
    }

    public static String getModbusCRC(byte[] inBytes, int length) {
        byte[] bytes = new byte[length];
        int i;
        for(i = 0; i < length; i++) {
            bytes[i] = inBytes[i];
        }
        return getCRC(bytes);
    }
}
