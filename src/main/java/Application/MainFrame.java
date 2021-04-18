package Application;


import com.google.common.collect.Lists;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.CRCTools;
import tools.SerialCommTools;
import tools.ShowUtils;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("all")
public class MainFrame extends JFrame {

    private static final Logger log = LoggerFactory.getLogger(MainFrame.class);

    //整体窗口
    public final int WIDTH = 1200;
    public final int HEIGHT = 800;
    //串口设置区域
    private List<String> serialPortNameList = Lists.newArrayList();
    private SerialPort serialport;
    private JPanel serialPortBoardPanel = new JPanel();
    private JLabel serialPortNameLabel = new JLabel();
    private JLabel serialPortBaudLabel = new JLabel();
    private JComboBox serialPortNameComboBox = new JComboBox();
    private JComboBox serialPortBaudrateComboBox = new JComboBox();
    //操作区域
    private JPanel inverterOperatePanel = new JPanel();
    private JTextArea mDataInput = new JTextArea();
    private JButton serialPortOpenButton = new JButton("打开串口");
    private byte[] receiveBuff;
    private JLabel inverterAddrLabel = new JLabel();
    private JTextField inverterAddrTextField = new JTextField();
    private JLabel setFreqLabel = new JLabel();
    private JTextField setFreqTextField = new JTextField();
    private JButton readSetFreqButton = new JButton("读取");
    private JButton writeSetFreqButton = new JButton("写入");
    private JLabel actualFreqLabel = new JLabel();
    private JTextField actualFreqTextField = new JTextField();
    private JButton readActualFreqButton = new JButton("读取");
    private JLabel setCurrentLabel = new JLabel();
    private JTextField setCurrentTextField = new JTextField();
    private JButton readSetCurrentButton = new JButton("读取");
    private JButton writeSetCurrentButton = new JButton("写入");
    private JLabel headTempLabel = new JLabel();
    private JTextField headTempTextField = new JTextField();
    private JCheckBox headTempAutoReadCheckBox = new JCheckBox("自动读取");
//    private JCheckBox mosfetTempAutoReadCheckBox = new JCheckBox("自动读取");
    private JButton headTempButton = new JButton("读取");
    private JLabel inverterFaultCodeLabel = new JLabel();
    private JTextField inverterFaultCodeTextField = new JTextField();
    private JButton inverterFaultCodeButton = new JButton("读取");
    private JLabel inverterTimestampLabel = new JLabel();
    private JTextField inverterTimestampTextField = new JTextField();
    private JButton readInverterTimestampButton = new JButton("读取");
    private JButton writeInverterTimestampButton = new JButton("写入");
    private JButton unlockInverterButton = new JButton("出厂解锁");
    private int startAddress = 0;
    private long timestamp = 0;
    //电源
    private JPanel powerOperatePanel = new JPanel();
    private JLabel powewrAddrLabel = new JLabel();
    private JTextField powewrAddrTextField = new JTextField();
    private JLabel powerCurrentLabel = new JLabel();
    private TextField powerCurrentTextField = new TextField();
    private JCheckBox powerCurrentAutoReadCheckBox = new JCheckBox("自动读取");
    private JButton readCurrentButton = new JButton("读取");
    private JLabel powerFaultLabel = new JLabel();
    private JButton powerReadFaultButton = new JButton("读取");
    private TextField powerFaultTextField = new TextField();
    private JLabel powerTimestampLabel = new JLabel();
    private TextField powerTimestampTextField = new TextField();
    private JButton readPowerTimestampButton = new JButton("读取");
    private JButton writePowerTimestampButton = new JButton("写入");
    private JButton unlockPowerButton = new JButton("出厂解锁");
    //日志
    private JPanel logPanel = new JPanel();
    private JTextArea logTextArea = new JTextArea();
    JScrollPane logScrollPane = new JScrollPane(logTextArea);

    /**
     * 整体窗口
     * */
    private void initView() {
        // 关闭程序
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // 禁止窗口最大化
        setResizable(false);

        // 设置程序窗口居中显示
        Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        setBounds(p.x - WIDTH / 2, p.y - HEIGHT / 2, WIDTH, HEIGHT);
        this.setLayout(null);

        setTitle("电磁焊接设备控制软件 v0.0");
    }

    private void initPowerOperateArea() {
        // 操作
        powerOperatePanel.setBorder(BorderFactory.createTitledBorder("电源参数"));
        powerOperatePanel.setBounds(10, 420, 400, 170);
        powerOperatePanel.setLayout(null);
        add(powerOperatePanel);

        powewrAddrLabel.setText("地址");
        powewrAddrLabel.setForeground(Color.gray);
        powewrAddrLabel.setBounds(10, 20, 60, 20);
        powerOperatePanel.add(powewrAddrLabel);
        powewrAddrTextField.setText("2");
        powewrAddrTextField.setBounds(80, 20, 40, 20);
        powerOperatePanel.add(powewrAddrTextField);

        powerCurrentLabel.setText("当前电流");
        powerCurrentLabel.setForeground(Color.gray);
        powerCurrentLabel.setBounds(10, 50, 60, 20);
        powerOperatePanel.add(powerCurrentLabel);
        powerCurrentTextField.setBounds(80, 50, 60, 20);
        powerOperatePanel.add(powerCurrentTextField);
        readCurrentButton.setBounds(150, 50, 60, 20);
        powerOperatePanel.add(readCurrentButton);
        powerCurrentAutoReadCheckBox.setBounds(220, 50, 80, 20);
        powerOperatePanel.add(powerCurrentAutoReadCheckBox);

        powerFaultLabel.setText("故障码");
        powerFaultLabel.setForeground(Color.gray);
        powerFaultLabel.setBounds(10, 80, 60, 20);
        powerOperatePanel.add(powerFaultLabel);
        powerFaultTextField.setBounds(80, 80, 60, 20);
        powerOperatePanel.add(powerFaultTextField);
        powerReadFaultButton.setBounds(150, 80, 60, 20);
        powerOperatePanel.add(powerReadFaultButton);

        powerTimestampLabel.setText("出厂时间");
        powerTimestampLabel.setForeground(Color.gray);
        powerTimestampLabel.setBounds(10, 110, 60, 20);
        powerOperatePanel.add(powerTimestampLabel);
        powerTimestampTextField.setBounds(80, 110, 160, 20);
        powerOperatePanel.add(powerTimestampTextField);
        readPowerTimestampButton.setBounds(250, 110, 60, 20);
        powerOperatePanel.add(readPowerTimestampButton);
        writePowerTimestampButton.setBounds(320, 110, 60, 20);
        powerOperatePanel.add(writePowerTimestampButton);

        unlockPowerButton.setBounds(10, 140, 120, 20);
        powerOperatePanel.add(unlockPowerButton);
    }

    private void initInverterOperateArea() {
        // 操作
        inverterOperatePanel.setBorder(BorderFactory.createTitledBorder("逆变器参数"));
        inverterOperatePanel.setBounds(10, 150, 400, 260);
        inverterOperatePanel.setLayout(null);
        add(inverterOperatePanel);

        inverterAddrLabel.setText("地址");
        inverterAddrLabel.setForeground(Color.gray);
        inverterAddrLabel.setBounds(10, 20, 60, 20);
        inverterOperatePanel.add(inverterAddrLabel);
        inverterAddrTextField.setText("1");
        inverterAddrTextField.setBounds(80, 20, 40, 20);
        inverterOperatePanel.add(inverterAddrTextField);

        setFreqLabel.setText("设定频率抽头");
        setFreqLabel.setForeground(Color.gray);
        setFreqLabel.setBounds(10, 50, 90, 20);
        inverterOperatePanel.add(setFreqLabel);
        setFreqTextField.setBounds(110, 50, 60, 20);
        inverterOperatePanel.add(setFreqTextField);
        readSetFreqButton.setBounds(180, 50, 60, 20);
        inverterOperatePanel.add(readSetFreqButton);
        writeSetFreqButton.setBounds(250, 50, 60, 20);
        inverterOperatePanel.add(writeSetFreqButton);

//        actualFreqLabel.setText("实际频率抽头");
//        actualFreqLabel.setForeground(Color.gray);
//        actualFreqLabel.setBounds(10, 80, 60, 20);
//        inverterOperatePanel.add(actualFreqLabel);
//        actualFreqTextField.setBounds(80, 80, 60, 20);
//        inverterOperatePanel.add(actualFreqTextField);
//        readActualFreqButton.setBounds(150, 80, 60, 20);
//        inverterOperatePanel.add(readActualFreqButton);

        setCurrentLabel.setText("设定电流抽头");
        setCurrentLabel.setForeground(Color.gray);
        setCurrentLabel.setBounds(10, 110, 90, 20);
        inverterOperatePanel.add(setCurrentLabel);
        setCurrentTextField.setBounds(110, 110, 60, 20);
        inverterOperatePanel.add(setCurrentTextField);
        readSetCurrentButton.setBounds(180, 110, 60, 20);
        inverterOperatePanel.add(readSetCurrentButton);
        writeSetCurrentButton.setBounds(250, 110, 60, 20);
        inverterOperatePanel.add(writeSetCurrentButton);

        headTempLabel.setText("焊头温度");
        headTempLabel.setForeground(Color.gray);
        headTempLabel.setBounds(10, 140, 60, 20);
        inverterOperatePanel.add(headTempLabel);
        headTempTextField.setBounds(80, 140, 60, 20);
        inverterOperatePanel.add(headTempTextField);
        headTempButton.setBounds(150, 140, 60, 20);
        inverterOperatePanel.add(headTempButton);
        headTempAutoReadCheckBox.setBounds(220, 140, 80, 20);
        inverterOperatePanel.add(headTempAutoReadCheckBox);

        inverterFaultCodeLabel.setText("故障码");
        inverterFaultCodeLabel.setForeground(Color.gray);
        inverterFaultCodeLabel.setBounds(10, 170, 60, 20);
        inverterOperatePanel.add(inverterFaultCodeLabel);
        inverterFaultCodeTextField.setBounds(80, 170, 60, 20);
        inverterOperatePanel.add(inverterFaultCodeTextField);
        inverterFaultCodeButton.setBounds(150, 170, 60, 20);
        inverterOperatePanel.add(inverterFaultCodeButton);

        inverterTimestampLabel.setText("出厂时间");
        inverterTimestampLabel.setForeground(Color.gray);
        inverterTimestampLabel.setBounds(10, 200, 60, 20);
        inverterOperatePanel.add(inverterTimestampLabel);
        inverterTimestampTextField.setBounds(80, 200, 160, 20);
        inverterOperatePanel.add(inverterTimestampTextField);
        readInverterTimestampButton.setBounds(250, 200, 60, 20);
        inverterOperatePanel.add(readInverterTimestampButton);
        writeInverterTimestampButton.setBounds(320, 200, 60, 20);
        inverterOperatePanel.add(writeInverterTimestampButton);

        unlockInverterButton.setBounds(10, 230, 120, 20);
        inverterOperatePanel.add(unlockInverterButton);
    }

    private void initLogArea() {
        logPanel.setBorder(BorderFactory.createTitledBorder("日志"));
        logPanel.setBounds(420, 10, 770, 750);
        logPanel.setLayout(null);
        add(logPanel);

        logScrollPane.setBounds(10, 20, 750, 720);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logPanel.add(logScrollPane);

        logTextArea.setLineWrap(true);
        logTextArea.setBounds(10, 20, 750, 720);
    }

    /**
     * 串口号显示区域
     * */
    private void initCommNumberDisplayArea() {
        int index;
        //边框
        serialPortBoardPanel.setBorder(BorderFactory.createTitledBorder("串口设置"));
        serialPortBoardPanel.setBounds(10, 10, 300, 100);
        serialPortBoardPanel.setLayout(null);
        add(serialPortBoardPanel);

        //串口标签
        serialPortNameLabel.setText("串口号");
        serialPortNameLabel.setForeground(Color.gray);
        serialPortNameLabel.setBounds(10, 25, 40, 20);
        serialPortBoardPanel.add(serialPortNameLabel);

        serialPortNameComboBox.setFocusable(false);
        serialPortNameComboBox.setBounds(60, 25, 100, 20);
        serialPortBoardPanel.add(serialPortNameComboBox);

        //打开串口
        serialPortOpenButton.setFocusable(false);
        serialPortOpenButton.setBounds(180, 25, 90, 20);
        serialPortBoardPanel.add(serialPortOpenButton);

        //波特率标签
        serialPortBaudLabel.setText("波特率");
        serialPortBaudLabel.setForeground(Color.gray);
        serialPortBaudLabel.setBounds(10, 60, 40, 20);
        serialPortBoardPanel.add(serialPortBaudLabel);

        serialPortBaudrateComboBox.setFocusable(false);
        serialPortBaudrateComboBox.setBounds(60, 60, 100, 20);
        serialPortBoardPanel.add(serialPortBaudrateComboBox);

        initInverterOperateArea();
        initPowerOperateArea();
        initLogArea();
    }

    Consumer<Boolean> consumerButtonDisplay = trueFalse -> {
        readSetFreqButton.setEnabled(trueFalse);
        writeSetFreqButton.setEnabled(trueFalse);
        readActualFreqButton.setEnabled(trueFalse);
        writeSetCurrentButton.setEnabled(trueFalse);
        readSetCurrentButton.setEnabled(trueFalse);
        headTempButton.setEnabled(trueFalse);
        inverterFaultCodeButton.setEnabled(trueFalse);
        writeInverterTimestampButton.setEnabled(trueFalse);
        readInverterTimestampButton.setEnabled(trueFalse);
        unlockInverterButton.setEnabled(trueFalse);
        headTempAutoReadCheckBox.setEnabled(trueFalse);
        readCurrentButton.setEnabled(trueFalse);
        readPowerTimestampButton.setEnabled(trueFalse);
        writePowerTimestampButton.setEnabled(trueFalse);
        unlockPowerButton.setEnabled(trueFalse);
        powerCurrentAutoReadCheckBox.setEnabled(trueFalse);
        powerReadFaultButton.setEnabled(trueFalse);
    };

    /**
     * 按钮监听
     * */
    private void initListener() {
        // 串口选择
        serialPortNameComboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                serialPortNameList = SerialCommTools.getPortNamelist();
                if (serialPortNameList.size() < 1) {
                    ShowUtils.warningMessage("没有搜索到有效串口！");
                } else {
                    int index = serialPortNameComboBox.getSelectedIndex();
                    serialPortNameComboBox.removeAllItems();
                    serialPortNameList.forEach(serialPortNameComboBox::addItem);
                    serialPortNameComboBox.setSelectedIndex(index);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // NO OP
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // NO OP
            }
        });

        Function<Integer, Integer> negetaviToPositiveFunction = in -> {
            int out = in & 0x000000FF;
            return out;
        };

        Consumer<byte[]> inverterReceiveConsumer = dataArray -> {
            if(dataArray[1] == 0x03) {
                int size = dataArray[2] - 2;
                if(size == 2) {
                    int data = dataArray[3] & 0xFF;
                    data <<= 8;
                    data |= dataArray[4] & 0xFF;
                    switch (startAddress) {
                        case 1: setFreqTextField.setText(Integer.toString(data) + ""); break;
                        case 2: actualFreqTextField.setText(Integer.toString(data) + ""); break;
                        case 3: setCurrentTextField.setText(Integer.toString(data) + ""); break;
                        case 5:
                            BigDecimal headTempDecimal = BigDecimal.valueOf(data / 100.0);
                            String headTempDecimalString = headTempDecimal.setScale(2, 2).toString() + "℃";
                            headTempTextField.setText(headTempDecimalString);
                            break;
                        case 6:
                            if(data == 0) {
                                inverterFaultCodeTextField.setText("无");
                            } else if(data == 1) {
                                inverterFaultCodeTextField.setText("短路");
                            }
                            break;
                        default: break;
                    }
                }else if(size == 4 && startAddress == 7) {
                    int data = dataArray[3] & 0xFF;
                    data <<= 8;
                    data |= dataArray[4] & 0xFF;
                    data <<= 8;
                    data |= dataArray[5] & 0xFF;
                    data <<= 8;
                    data |= dataArray[6] & 0xFF;
                    timestamp = data;
                    LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.ofHours(8));
                    inverterTimestampTextField.setText(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            }else if(dataArray[1] == 0x05){

            }
        };

        Consumer<byte[]> powerReceiveConsumer = dataArray -> {
            if(dataArray[1] == 0x03) {
                int size = dataArray[2] - 2;
                if(size == 2) {
                    int data = dataArray[3] & 0xFF;
                    data <<= 8;
                    data |= dataArray[4] & 0xFF;
                    switch (startAddress) {
                        case 1: powerCurrentTextField.setText(Integer.toString(data) + "mA"); break;
                        case 2:
                            if(data == 0x00) {
                                powerFaultTextField.setText("无");
                            }else if(data == 0x01) {
                                powerFaultTextField.setText("短路");
                            }else {
                                powerFaultTextField.setText("故障" + Integer.toString(data));
                            }
                            break;
                        default: break;
                    }
                }else if(size == 4 && startAddress == 3) {
                    int data = dataArray[3] & 0xFF;
                    data <<= 8;
                    data |= dataArray[4] & 0xFF;
                    data <<= 8;
                    data |= dataArray[5] & 0xFF;
                    data <<= 8;
                    data |= dataArray[6] & 0xFF;
                    timestamp = data;
                    LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.ofHours(8));
                    powerTimestampTextField.setText(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            }else if(dataArray[1] == 0x05){

            }
        };

        // 打开|关闭串口
        serialPortOpenButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if ("打开串口".equals(serialPortOpenButton.getText()) && Objects.nonNull(serialPortOpenButton)) {
                    String serialPortName = serialPortNameComboBox.getSelectedItem().toString();
                    int serialPortBaudRate = Integer.parseInt(serialPortBaudrateComboBox.getSelectedItem().toString());
                    try {
                        serialport = SerialCommTools.openPort(serialPortName, serialPortBaudRate);
                        serialPortOpenButton.setText("关闭串口");
                        consumerButtonDisplay.accept(true);
                        //接收监听
                        SerialCommTools.addListener(serialport, new SerialCommTools.DataAvailableListener() {
                            @Override
                            public void dataAvailable() {
                                if(Objects.isNull(serialport)) {
                                    ShowUtils.warningMessage("串口对象为空，监听失败");
                                } else {
                                    receiveBuff = SerialCommTools.receiveData(serialport);
                                    if(Objects.nonNull(receiveBuff)) {
                                        log.info("收到数据 {}", SerialCommTools.byteArrayToStringFunction.apply(receiveBuff));
                                        if(CRCTools.checkModbusCRC(receiveBuff, receiveBuff.length)) {
                                            if(receiveBuff[0] == 0x01) {
                                                inverterReceiveConsumer.accept(receiveBuff);
                                            }
                                            if(receiveBuff[0] == 0x02) {
                                                powerReceiveConsumer.accept(receiveBuff);
                                            }
                                        } else {
                                            log.error("CRC 校验失败");
                                        }
                                    }
                                }
                            }
                        });
                    }catch (PortInUseException e2) {
                        ShowUtils.warningMessage("打开失败，串口被占用");
                        log.error("打开串口{}失败，串口被占用{}", serialPortName, e2.getLocalizedMessage());
                    }
                } else {
                    SerialCommTools.closePort(serialport);
                    serialPortOpenButton.setText("打开串口");
                    consumerButtonDisplay.accept(false);
                }
            }
        });

        BiConsumer<Integer, Integer> inverterReadConsumer = (start, size) -> {
            byte[] command = new byte[8];
            String addrString = inverterAddrTextField.getText();
            int addrInt = Integer.parseInt(addrString);
            command[0] = (byte) addrInt;
            command[1] = (byte) 0x03;
            command[2] = (byte) 0x00;
            command[3] = start.byteValue();
            command[4] = (byte) 0x00;
            command[5] = size.byteValue();
            String crcHexString = CRCTools.getModbusCRC(command, 6);
            int crcH = SerialCommTools.hexStringToInt(crcHexString.substring(0, 2));
            int crcL = SerialCommTools.hexStringToInt(crcHexString.substring(2, 4));
            command[6] = (byte)crcH;
            command[7] = (byte)crcL;
            SerialCommTools.sendData(serialport, command);
            startAddress = start;
        };

        readSetFreqButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(1, 1);
            }
        });

        readActualFreqButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(2, 1);
            }
        });

        readSetCurrentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(3, 1);
            }
        });

        BiConsumer<Integer, Integer> powerReadConsumer = (start, size) -> {
            byte[] command = new byte[8];
            String addrString = powewrAddrTextField.getText();
            int addrInt = Integer.parseInt(addrString);
            command[0] = (byte) addrInt;
            command[1] = (byte) 0x03;
            command[2] = (byte) 0x00;
            command[3] = start.byteValue();
            command[4] = (byte) 0x00;
            command[5] = size.byteValue();
            String crcHexString = CRCTools.getModbusCRC(command, 6);
            int crcH = SerialCommTools.hexStringToInt(crcHexString.substring(0, 2));
            int crcL = SerialCommTools.hexStringToInt(crcHexString.substring(2, 4));
            command[6] = (byte)crcH;
            command[7] = (byte)crcL;
            SerialCommTools.sendData(serialport, command);
            startAddress = start;
        };

        readCurrentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                powerReadConsumer.accept(1, 1);
            }
        });

        powerReadFaultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                powerReadConsumer.accept(2, 1);
            }
        });

        readPowerTimestampButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                powerReadConsumer.accept(3, 2);
            }
        });

        Consumer<Integer> powerWriteConsumer = start -> {
            byte[] command = new byte[11];
            String addrString = powewrAddrTextField.getText();
            int addrInt = Integer.parseInt(addrString);
            command[0] = (byte) addrInt;
            command[1] = (byte) 0x05;
            command[2] = (byte) 0x00;
            command[3] = start.byteValue();
            command[4] = (byte) 0x00;
            command[5] = (byte) 0x01;
            command[6] = (byte) 0x02;
            String dataString = null;
            String timestampString = null;
            switch (start) {
                case 3: timestampString = powerTimestampTextField.getText(); break;
                case 4: timestampString = powerTimestampTextField.getText(); break;
                default: break;
            }
            int dataInt = 0;
            int dataH = 0;
            int dataL = 0;
            if(StringUtils.isNotEmpty(dataString)) {
                dataInt = Integer.parseInt(dataString);
                dataH = dataInt >> 8;
                dataL = dataInt & 0xFF;
            }
            if(StringUtils.isNotEmpty(timestampString)) {
                dataInt = Integer.parseInt(timestampString);
                if(start == 3) {
                    dataH = dataInt >> 24;
                    dataL = (dataInt >> 16) & 0xFF;
                } else if(start == 4) {
                    dataH = dataInt >> 8;
                    dataL = dataInt & 0xFF;
                }
            }
            command[7] = (byte) dataH;
            command[8] = (byte) dataL;
            String crcHexString = CRCTools.getModbusCRC(command, 9);
            int crcH = SerialCommTools.hexStringToInt(crcHexString.substring(0, 2));
            int crcL = SerialCommTools.hexStringToInt(crcHexString.substring(2, 4));
            command[9] = (byte)crcH;
            command[10] = (byte)crcL;
            SerialCommTools.sendData(serialport, command);
        };

        writePowerTimestampButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
                powerTimestampTextField.setText(Long.toString(timestamp));
                powerWriteConsumer.accept(3);
                try {
                    Thread.sleep(500);
                }catch (Exception e2) {
                    log.error("{}", e2.getLocalizedMessage());
                }
                powerWriteConsumer.accept(4);
            }
        });

        Runnable runnable = () -> {
            while (true) {
                try {
                    if(Objects.nonNull(serialport) && serialPortOpenButton.getText().equals("关闭串口")) {
                        if(headTempAutoReadCheckBox.isSelected()) {
                            inverterReadConsumer.accept(5, 1);
                            Thread.sleep(10);
                        }
                        if(powerCurrentAutoReadCheckBox.isSelected()) {
                            powerReadConsumer.accept(1, 1);
                            Thread.sleep(10);
                        }
                    }
                    Thread.sleep(10);
                }catch (InterruptedException e) {
                    log.error("{}", e);
                }
            }
        };

        new Thread(runnable).start();

        headTempButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(5, 1);
            }
        });

        inverterFaultCodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(6, 1);
            }
        });

        readInverterTimestampButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(7, 2);
            }
        });

        Consumer<Integer> inverterWriteConsumer = start -> {
            byte[] command = new byte[11];
            String addrString = inverterAddrTextField.getText();
            int addrInt = Integer.parseInt(addrString);
            command[0] = (byte) addrInt;
            command[1] = (byte) 0x05;
            command[2] = (byte) 0x00;
            command[3] = start.byteValue();
            command[4] = (byte) 0x00;
            command[5] = (byte) 0x01;
            command[6] = (byte) 0x02;
            String dataString = null;
            String timestampString = null;
            switch (start) {
                case 1: dataString = setFreqTextField.getText(); break;
                case 3: dataString = setCurrentTextField.getText(); break;
                case 7: timestampString = inverterTimestampTextField.getText(); break;
                case 8: timestampString = inverterTimestampTextField.getText(); break;
            }
            int dataInt = 0;
            int dataH = 0;
            int dataL = 0;
            if(StringUtils.isNotEmpty(dataString)) {
                dataInt = Integer.parseInt(dataString);
                dataH = dataInt >> 8;
                dataL = dataInt & 0xFF;
            }
            if(StringUtils.isNotEmpty(timestampString)) {
                dataInt = Integer.parseInt(timestampString);
                if(start == 7) {
                    dataH = dataInt >> 24;
                    dataL = (dataInt >> 16) & 0xFF;
                } else if(start == 8) {
                    dataH = dataInt >> 8;
                    dataL = dataInt & 0xFF;
                }
            }
            command[7] = (byte) dataH;
            command[8] = (byte) dataL;
            String crcHexString = CRCTools.getModbusCRC(command, 9);
            if(crcHexString.length() == 1) {
                crcHexString = "000" + crcHexString;
            }
            if(crcHexString.length() == 2) {
                crcHexString = "00" + crcHexString;
            }
            if(crcHexString.length() == 3) {
                crcHexString = "0" + crcHexString;
            }

            int crcH = SerialCommTools.hexStringToInt(crcHexString.substring(0, 2));
            int crcL = SerialCommTools.hexStringToInt(crcHexString.substring(2, 4));
            command[9] = (byte)crcH;
            command[10] = (byte)crcL;
            SerialCommTools.sendData(serialport, command);
        };

        writeSetFreqButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterWriteConsumer.accept(1);
            }
        });

        writeSetCurrentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterWriteConsumer.accept(3);
            }
        });

        writeInverterTimestampButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
                inverterTimestampTextField.setText(Long.toString(timestamp));
                inverterWriteConsumer.accept(7);
                try {
                    Thread.sleep(500);
                }catch (Exception e2) {
                    log.error("{}", e2.getLocalizedMessage());
                }
                inverterWriteConsumer.accept(8);
            }
        });

        unlockInverterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                byte[] command = new byte[4];
                command[0] = (byte)0xAA;
                command[1] = (byte)0xAA;
                command[2] = (byte)0xAA;
                command[3] = (byte)0xAA;
                SerialCommTools.sendData(serialport, command);
            }
        });

        unlockPowerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                byte[] command = new byte[4];
                command[0] = (byte)0xAA;
                command[1] = (byte)0xAA;
                command[2] = (byte)0xAA;
                command[3] = (byte)0xAA;
                SerialCommTools.sendData(serialport, command);
            }
        });
    }

    /**
     * 初始化可用串口号
     * */
    private void initSerialPortList() {
        serialPortNameList = SerialCommTools.getPortNamelist();
        if(serialPortNameList.size() < 1) {
            ShowUtils.warningMessage("没有找到可用串口");
        } else {
            serialPortNameList.forEach(serialPortNameComboBox::addItem);
        }
        serialPortBaudrateComboBox.addItem("9600");
        serialPortBaudrateComboBox.addItem("19200");
        serialPortBaudrateComboBox.addItem("38400");
        serialPortBaudrateComboBox.addItem("57600");
        serialPortBaudrateComboBox.addItem("115200");

        consumerButtonDisplay.accept(false);
    }

    /**
     * 初始化所有控件
     * */
    private void initAllComponents() {
        initView();
        initCommNumberDisplayArea();
        initSerialPortList();
        initListener();
    }

    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.initAllComponents();

        ConsoleStream consoleStream = new ConsoleStream(frame.logTextArea);
        PrintStream printStream = new PrintStream(consoleStream);
        System.setOut(printStream);

        frame.setVisible(true);
    }
}