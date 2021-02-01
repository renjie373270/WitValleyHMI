package tools;

import java.util.stream.Stream;

public class HexTools {


    public static String hexByteToString(byte[] data) {
        StringBuilder builder = new StringBuilder();
        Stream.of(data).forEach(da -> {
//            Character c1 = (int)(da / 10)
//            builder.append(((int)da / 10) + '0');
        });
        return "";
    }

    public static void main(String[] args) {
        byte[] data = new byte[]{0x44, 0x4E, 0x00, 0x01, 0x52, 0x01, (byte)0xFE, 0x11, (byte)0x94, 0x00, 0x00, 0x19, 0x00, 0x00, 0x00, (byte)0xAA};
        System.out.println(hexByteToString(data));
        byte da = 0x12;
        int daInt = (int)da;
//        String ss = new String('C');
        System.out.println((daInt / 10) + '0');
    }
}
