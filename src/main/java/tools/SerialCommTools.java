package tools;


import com.google.common.collect.Lists;
import gnu.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.TooManyListenersException;
import java.util.function.Function;

@SuppressWarnings("all")
public class SerialCommTools {

    private static final Logger log = LoggerFactory.getLogger(SerialCommTools.class);

    /**
     * 获取串口列表
     * */
    public static List<String> getPortNamelist() {
        // 获得当前所有可用串口
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        List<String> portNameList = Lists.newArrayList();
        // 将可用串口名添加到List并返回该List
        while (portList.hasMoreElements()) {
            String portName = portList.nextElement().getName();
            portNameList.add(portName);
            log.info("发现串口，串口号 {}", portName);
        }
        return portNameList;
    }

    /**
     * 打开串口
     * */
    public static SerialPort openPort(String portName, int baudrate) throws PortInUseException {
        try {
            // 通过端口名识别端口
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            // 打开端口，并给端口名字和一个timeout（打开操作的超时时间）
            CommPort commPort = portIdentifier.open(portName, 2000);
            // 判断是不是串口
            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                try {
                    // 设置一下串口的波特率等参数
                    // 数据位：8
                    // 停止位：1
                    // 校验位：None
                    serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    log.info("打开串口成功，串口号 {} 波特率 {} 数据位 {} 停止位 {} 校验位 {}", portName, baudrate, 88, 1, "无");
                } catch (UnsupportedCommOperationException e) {
                    log.error("打开串口失败 {}", e.getLocalizedMessage());
                }
                return serialPort;
            }
        } catch (NoSuchPortException e1) {
            log.error("打开串口失败 {}，串口不存在", e1.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 关闭串口
     * */
    public static void closePort(SerialPort serialPort) {
        if(Objects.nonNull(serialPort)) {
            serialPort.close();
            log.info("关闭串口 {} 成功", serialPort.getName());
        }
    }

    public static Function<byte[], String> byteArrayToStringFunction = dataArray -> {
        StringBuilder builder = new StringBuilder();
        String sample = "0123456789ABCDEF";
        int index = 0;
        for(byte data : dataArray) {
            int daInt = (int)data;
            daInt &= 0x000000FF;
            int d1 = (daInt / 16);
            int d2 = (daInt % 16);
            Character c1 = sample.charAt(d1);
            Character c2 = sample.charAt(d2);
            index++;
            if(index > 16) {
                break;
            }
            builder.append(c1.toString()).append(c2.toString()).append(" ");
        }
        return builder.toString();
    };

    /**
     * 发送数据
     * */
    public static void sendData(SerialPort serialPort, byte[] data) {
        OutputStream out = null;
        try {
            out = serialPort.getOutputStream();
            out.write(data);
            out.flush();
            log.info("{} 发送数据成功 {}", serialPort.getName(), byteArrayToStringFunction.apply(data));
        } catch (IOException e) {
            log.error("发送数据失败 {}", e.getLocalizedMessage());
        } finally {
            try {
                if (Objects.nonNull(out)) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                log.error("关闭数据流失败 {}", e.getLocalizedMessage());
            }
        }
    }

    /**
     * 接收数据
     * */
    public static byte[] receiveData(SerialPort serialPort) {
        InputStream in = null;
        byte[] bytes = new byte[1024];
        int i, index = 0;
        try {
            in = serialPort.getInputStream();
            // 缓冲区大小为一个字节
            byte[] readBuffer = new byte[1];
            int length = in.read(readBuffer);
            while (length > 0) {
                for(i = 0; i < length; i++) {
                    bytes[index + i] = readBuffer[i];
                }
                index += length;
                length = in.read(readBuffer);
            }
            byte[] returnBytes = new byte[index];
            for(i = 0; i < index; i++) {
                returnBytes[i] = bytes[i];
            }
            return returnBytes;
        } catch (IOException e) {
            log.error("IO 异常 {}", e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            try {
                if(Objects.nonNull(in)) {
                    in.close();
                    in = null;
                }
            } catch (IOException e) {
                log.error("IO 异常 {}", e.getLocalizedMessage());
            }
        }
        return null;
    }

    private static Function<byte[], int[]> byteArrayToIntArrayFunction = dataArray -> {
        int[] intArray = new int[1024];
        int index = 0;
        for(byte data : dataArray) {
            int dataInt = (int)data;
            dataInt &= 0x000000FF;
            intArray[index] = dataInt;
            index ++;
        }
        return intArray;
    };

    public static String intToHexString(int number) {
        StringBuilder builder = new StringBuilder();
        String sample = "0123456789ABCDEF";
        int n1 = number / 16;
        int n2 = number % 16;
        Character c1 = sample.charAt(n1);
        Character c2 = sample.charAt(n2);
        builder.append(c1).append(c2).append("H");
        return builder.toString();
    }

    public static int hexStringToInt(String singleHex) {
        StringBuilder builder = new StringBuilder();
        String sample = "0123456789ABCDEF";
        int d1 = sample.indexOf(singleHex.substring(0, 1));
        int d2 = sample.indexOf(singleHex.substring(1, 2));
        int data = d1 * 16 + d2;
        return data;
    }

    public interface DataAvailableListener {
        void dataAvailable();
    }

    public static class SerialPortListener implements SerialPortEventListener {

        private DataAvailableListener dataAvailableListener;

        public SerialPortListener(DataAvailableListener dataAvailableListener) {
            this.dataAvailableListener = dataAvailableListener;
        }

        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            switch (serialPortEvent.getEventType()) {
                case SerialPortEvent.DATA_AVAILABLE: // 1.串口存在有效数据
                    if (Objects.nonNull(dataAvailableListener)) {
                        dataAvailableListener.dataAvailable();
                    }
                    break;

                case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2.输出缓冲区已清空
                    break;

                case SerialPortEvent.CTS: // 3.清除待发送数据
                    break;

                case SerialPortEvent.DSR: // 4.待发送数据准备好了
                    break;

                case SerialPortEvent.RI: // 5.振铃指示
                    break;

                case SerialPortEvent.CD: // 6.载波检测
                    break;

                case SerialPortEvent.OE: // 7.溢位（溢出）错误
                    break;

                case SerialPortEvent.PE: // 8.奇偶校验错误
                    break;

                case SerialPortEvent.FE: // 9.帧错误
                    break;

                case SerialPortEvent.BI: // 10.通讯中断
//                    ShowUtils.warningMessage("与串口设备通讯中断");
                    log.error("与串口设备通讯中断");
                    break;

                default:
                    break;
            }
        }
    }

    public static void addListener(SerialPort serialPort, DataAvailableListener listener) {
        try {
            serialPort.addEventListener(new SerialPortListener(listener));
            serialPort.notifyOnDataAvailable(true);
            serialPort.notifyOnBreakInterrupt(true);
        }catch (TooManyListenersException e) {
            log.error("接收监听出错 {}", e.getLocalizedMessage());
        }
    }
}
