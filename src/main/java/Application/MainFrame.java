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

    private JButton serialEGCNOButton = new JButton("English");
    private byte[] receiveBuff;
    private JLabel inverterAddrLabel = new JLabel();
    private JTextField inverterAddrTextField = new JTextField();
    //谐振频率
    private JLabel setResenantFreqLabel = new JLabel();
    private JTextField resonantFreqTextField = new JTextField();
    private JButton readResonantFreqButton = new JButton("读取");
    private JButton writeResonantFreqButton = new JButton("写入");
    //焊接能量
    private JLabel powerConsumptionLabel = new JLabel();
    private JTextField powerConsumptionTextField = new JTextField();
    private JButton readPowerConsumptionButton = new JButton("读取");
    //设定电流
    private JLabel setCurrentLabel = new JLabel();
    private JTextField setCurrentTextField = new JTextField();
    private JButton readSetCurrentButton = new JButton("读取");
    private JButton writeSetCurrentButton = new JButton("写入");
    //温度反馈
    private JLabel tempFeedbackLabel = new JLabel();
    private JComboBox tempFeedbackComBox = new JComboBox();
    private JButton readTempFeedbackButton = new JButton("读取");
    private JButton writeTempFeedbackButton = new JButton("写入");
    //电流反馈
    private JLabel currentFeedbackLabel = new JLabel();
    private JComboBox currentFeedbackComBox = new JComboBox();
    private JButton readCurrentFeedbackButton = new JButton("读取");
    private JButton writeCurrentFeedbackButton = new JButton("写入");
    //停用模式
    private JLabel stopLabel = new JLabel();
    private JTextField stopTextField = new JTextField();
    private JButton writeStopButton = new JButton("设置");
    //主板温度
    private JLabel boardTempLabel = new JLabel();
    private JTextField boardTempTextField = new JTextField();
    private JButton readBoardTempButton = new JButton("读取");
    //MOS管温度
    private JLabel mosfetTempLabel = new JLabel();
    private JTextField mosfetTempTextField = new JTextField();
    private JButton readMosfetTempButton = new JButton("读取");
    //焊头温度
    private JLabel headTempLabel = new JLabel();
    private JTextField headTempTextField = new JTextField();
    private JButton readHeadTempButton = new JButton("读取");
    //电压1, 0~5V
    private JLabel voltage1Label = new JLabel();
    private JTextField voltage1TextField = new JTextField();
    private JButton readVoltage1Button = new JButton("读取");
    //电压2, 0~10V
    private JLabel voltage2Label = new JLabel();
    private JTextField voltage2TextField = new JTextField();
    private JButton readVoltage2Button = new JButton("读取");
    //电流1, 4~20mA
    private JLabel current1Label = new JLabel();
    private JTextField current1TextField = new JTextField();
    private JButton readCurrent1Button = new JButton("读取");
    //错误信息
    private JLabel errorLabel = new JLabel();
    private JTextArea errorTextArea = new JTextArea();
    private JButton readErrorButton = new JButton("读取");
    //焊接次数
    private JLabel solderTimesLabel = new JLabel();
    private JTextField solderTimesTextField = new JTextField();
    private JButton readSolderTimesButton = new JButton("读取");
    private JButton writeSolderTimesButton = new JButton("写入");
    //调试开关
    private JLabel debugLabel = new JLabel();
    private JTextField debugTextField = new JTextField();
    private JButton readDebugButton = new JButton("读取");
    private JButton writeDebugButton = new JButton("写入");
    //焊接日志读取
//    private JLabel solderLogLabel = new JLabel();
//    private JButton readSolderLogButton = new JButton("读取");

    private JCheckBox headTempAutoReadCheckBox = new JCheckBox("自动读取");
    private JCheckBox headTempAutoReadMOSCheckBox = new JCheckBox("自动读取");
    private JCheckBox headTempAutoReadheadCheckBox = new JCheckBox("自动读取");
    private JLabel timestampLabel = new JLabel();
    private JTextField timestampTextField = new JTextField();
    private JButton readTimestampButton = new JButton("读取");
    private JButton writeTimestampButton = new JButton("写入");

    private int startAddress = 0;
    private long timestamp = 0;

    //日志
    private JPanel logPanel = new JPanel();
    private JTextArea logTextArea = new JTextArea();
    JScrollPane logScrollPane = new JScrollPane(logTextArea);

    /**
     * 整体窗口
     */
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

    private void initInverterOperateArea() {
        int posY = 20;
        // 操作
        inverterOperatePanel.setBorder(BorderFactory.createTitledBorder("逆变器参数"));
        inverterOperatePanel.setBounds(10, 150, 410, 590);
        inverterOperatePanel.setLayout(null);
        add(inverterOperatePanel);

        inverterAddrLabel.setText("地址");
        inverterAddrLabel.setForeground(Color.gray);
        inverterAddrLabel.setBounds(10, posY, 60, 20);
        inverterOperatePanel.add(inverterAddrLabel);
        inverterAddrTextField.setText("1");
        inverterAddrTextField.setBounds(150, posY, 40, 20);
        inverterOperatePanel.add(inverterAddrTextField);
        posY += 30;

        setResenantFreqLabel.setText("谐振频率");
        setResenantFreqLabel.setForeground(Color.gray);
        setResenantFreqLabel.setBounds(10, posY, 120, 20);
        inverterOperatePanel.add(setResenantFreqLabel);
        resonantFreqTextField.setBounds(150, posY, 60, 20);
        inverterOperatePanel.add(resonantFreqTextField);
        readResonantFreqButton.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readResonantFreqButton);
        writeResonantFreqButton.setBounds(310, posY, 80, 20);
        inverterOperatePanel.add(writeResonantFreqButton);
        posY += 30;

        powerConsumptionLabel.setText("用电量");
        powerConsumptionLabel.setForeground(Color.gray);
        powerConsumptionLabel.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(powerConsumptionLabel);
        powerConsumptionTextField.setBounds(150, posY, 60, 20);
        inverterOperatePanel.add(powerConsumptionTextField);
        readPowerConsumptionButton.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readPowerConsumptionButton);
        posY += 30;

        setCurrentLabel.setText("设定电流");
        setCurrentLabel.setForeground(Color.gray);
        setCurrentLabel.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(setCurrentLabel);
        setCurrentTextField.setBounds(150, posY, 60, 20);
        inverterOperatePanel.add(setCurrentTextField);
        readSetCurrentButton.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readSetCurrentButton);
        writeSetCurrentButton.setBounds(310, posY, 80, 20);
        inverterOperatePanel.add(writeSetCurrentButton);
        posY += 30;

        tempFeedbackLabel.setText("温度反馈");
        tempFeedbackLabel.setForeground(Color.gray);
        tempFeedbackLabel.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(tempFeedbackLabel);
        tempFeedbackComBox.setFocusable(false);
        tempFeedbackComBox.setBounds(150, posY, 60, 20);
        tempFeedbackComBox.addItem("NONE");
        inverterOperatePanel.add(tempFeedbackComBox);
        readTempFeedbackButton.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readTempFeedbackButton);
        writeTempFeedbackButton.setBounds(310, posY, 80, 20);
        inverterOperatePanel.add(writeTempFeedbackButton);
        posY += 30;

        currentFeedbackLabel.setText("电流控制");
        currentFeedbackLabel.setForeground(Color.gray);
        currentFeedbackLabel.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(currentFeedbackLabel);
        currentFeedbackComBox.setFocusable(false);
        currentFeedbackComBox.setBounds(150, posY, 60, 20);
        currentFeedbackComBox.addItem("NONE");
        inverterOperatePanel.add(currentFeedbackComBox);
        readCurrentFeedbackButton.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readCurrentFeedbackButton);
        writeCurrentFeedbackButton.setBounds(310, posY, 80, 20);
        inverterOperatePanel.add(writeCurrentFeedbackButton);
        posY += 30;

        timestampLabel.setText("出厂时间");
        timestampLabel.setForeground(Color.gray);
        timestampLabel.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(timestampLabel);
        timestampTextField.setBounds(150, posY, 60, 20);
        inverterOperatePanel.add(timestampTextField);
        readTimestampButton.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readTimestampButton);
        writeTimestampButton.setBounds(310, posY, 80, 20);
        inverterOperatePanel.add(writeTimestampButton);
        posY += 30;

        stopLabel.setText("停用模式");
        stopLabel.setForeground(Color.gray);
        stopLabel.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(stopLabel);
        stopTextField.setBounds(150, posY, 60, 20);
        inverterOperatePanel.add(stopTextField);
        writeStopButton.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(writeStopButton);
        posY += 30;

        boardTempLabel.setText("主板温度");
        boardTempLabel.setForeground(Color.gray);
        boardTempLabel.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(boardTempLabel);
        boardTempTextField.setBounds(150, posY, 60, 20);
        inverterOperatePanel.add(boardTempTextField);
        readBoardTempButton.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readBoardTempButton);

        headTempAutoReadCheckBox.setBounds(310, posY, 90, 20);
        inverterOperatePanel.add(headTempAutoReadCheckBox);

        posY += 30;

        mosfetTempLabel.setText("MOS温度");
        mosfetTempLabel.setForeground(Color.gray);
        mosfetTempLabel.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(mosfetTempLabel);
        mosfetTempTextField.setBounds(150, posY, 60, 20);
        inverterOperatePanel.add(mosfetTempTextField);
        readMosfetTempButton.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readMosfetTempButton);

        headTempAutoReadMOSCheckBox.setBounds(310, posY, 90, 20);
        inverterOperatePanel.add(headTempAutoReadMOSCheckBox);

        posY += 30;

        headTempLabel.setText("焊头温度");
        headTempLabel.setForeground(Color.gray);
        headTempLabel.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(headTempLabel);
        headTempTextField.setBounds(150, posY, 60, 20);
        inverterOperatePanel.add(headTempTextField);
        readHeadTempButton.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readHeadTempButton);

        headTempAutoReadheadCheckBox.setBounds(310, posY, 90, 20);
        inverterOperatePanel.add(headTempAutoReadheadCheckBox);
        posY += 30;

        voltage1Label.setText("电压1");
        voltage1Label.setForeground(Color.gray);
        voltage1Label.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(voltage1Label);
        voltage1TextField.setBounds(150, posY, 60, 20);
        inverterOperatePanel.add(voltage1TextField);
        readVoltage1Button.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readVoltage1Button);
        posY += 30;

        voltage2Label.setText("电压2");
        voltage2Label.setForeground(Color.gray);
        voltage2Label.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(voltage2Label);
        voltage2TextField.setBounds(150, posY, 60, 20);
        inverterOperatePanel.add(voltage2TextField);
        readVoltage2Button.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readVoltage2Button);
        posY += 30;

        current1Label.setText("电流接口");
        current1Label.setForeground(Color.gray);
        current1Label.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(current1Label);
        current1TextField.setBounds(150, posY, 60, 20);
        inverterOperatePanel.add(current1TextField);
        readCurrent1Button.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readCurrent1Button);
        posY += 30;

        errorLabel.setText("错误信息");
        errorLabel.setForeground(Color.gray);
        errorLabel.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(errorLabel);
        errorTextArea.setBounds(150, posY, 160, 50);
        errorTextArea.setLineWrap(true);
        errorTextArea.setWrapStyleWord(true);
        inverterOperatePanel.add(errorTextArea);
        readErrorButton.setBounds(320, posY, 80, 20);
        inverterOperatePanel.add(readErrorButton);
        posY += 60;

        solderTimesLabel.setText("焊接次数");
        solderTimesLabel.setForeground(Color.gray);
        solderTimesLabel.setBounds(10, posY, 1501, 20);
        inverterOperatePanel.add(solderTimesLabel);
        solderTimesTextField.setBounds(150, posY, 60, 20);
        inverterOperatePanel.add(solderTimesTextField);
        readSolderTimesButton.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readSolderTimesButton);
        writeSolderTimesButton.setBounds(310, posY, 80, 20);
        inverterOperatePanel.add(writeSolderTimesButton);
        posY += 30;

        debugLabel.setText("调试开关");
        debugLabel.setForeground(Color.gray);
        debugLabel.setBounds(10, posY, 150, 20);
        inverterOperatePanel.add(debugLabel);
        debugTextField.setBounds(150, posY, 60, 20);
        inverterOperatePanel.add(debugTextField);
        readDebugButton.setBounds(220, posY, 80, 20);
        inverterOperatePanel.add(readDebugButton);
        writeDebugButton.setBounds(310, posY, 80, 20);
        inverterOperatePanel.add(writeDebugButton);
        posY += 30;

//        solderLogLabel.setText("焊接日志");
//        solderLogLabel.setForeground(Color.gray);
//        solderLogLabel.setBounds(10, posY, 60, 20);
//        inverterOperatePanel.add(solderLogLabel);
//        readSolderLogButton.setBounds(80, posY, 60, 20);
//        inverterOperatePanel.add(readSolderLogButton);
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
     */
    private void initCommNumberDisplayArea() {
        int index;
        //边框
        serialPortBoardPanel.setBorder(BorderFactory.createTitledBorder("串口设置"));
        serialPortBoardPanel.setBounds(10, 10, 320, 100);
        serialPortBoardPanel.setLayout(null);
        add(serialPortBoardPanel);

        //串口标签
        serialPortNameLabel.setText("串口号");
        serialPortNameLabel.setForeground(Color.gray);
        serialPortNameLabel.setBounds(10, 25, 120, 20);
        serialPortBoardPanel.add(serialPortNameLabel);

        serialPortNameComboBox.setFocusable(false);
        serialPortNameComboBox.setBounds(80, 25, 100, 20);
        serialPortBoardPanel.add(serialPortNameComboBox);

        //打开串口
        serialPortOpenButton.setFocusable(false);
        serialPortOpenButton.setBounds(190, 25, 110, 20);
        serialPortBoardPanel.add(serialPortOpenButton);

        serialEGCNOButton.setFocusable(false);
        serialEGCNOButton.setBounds(190, 60, 110, 20);
        serialPortBoardPanel.add(serialEGCNOButton);


        //波特率标签
        serialPortBaudLabel.setText("波特率");
        serialPortBaudLabel.setForeground(Color.gray);
        serialPortBaudLabel.setBounds(10, 60, 120, 20);
        serialPortBoardPanel.add(serialPortBaudLabel);

        serialPortBaudrateComboBox.setFocusable(false);
        serialPortBaudrateComboBox.setBounds(80, 60, 100, 20);
        serialPortBoardPanel.add(serialPortBaudrateComboBox);

        initInverterOperateArea();
        initLogArea();
    }

    Consumer<Boolean> consumerButtonDisplay = trueFalse -> {
        readResonantFreqButton.setEnabled(trueFalse);
        writeResonantFreqButton.setEnabled(trueFalse);
        writeSetCurrentButton.setEnabled(trueFalse);
        readSetCurrentButton.setEnabled(trueFalse);
        readHeadTempButton.setEnabled(trueFalse);
        writeTimestampButton.setEnabled(trueFalse);
        readTimestampButton.setEnabled(trueFalse);
        headTempAutoReadCheckBox.setEnabled(trueFalse);
        headTempAutoReadMOSCheckBox.setEnabled(trueFalse);
        headTempAutoReadheadCheckBox.setEnabled(trueFalse);

        readPowerConsumptionButton.setEnabled(trueFalse);
        readTempFeedbackButton.setEnabled(trueFalse);
        writeTempFeedbackButton.setEnabled(trueFalse);
        readCurrentFeedbackButton.setEnabled(trueFalse);
        writeCurrentFeedbackButton.setEnabled(trueFalse);
        readDebugButton.setEnabled(trueFalse);
        writeDebugButton.setEnabled(trueFalse);
        writeStopButton.setEnabled(trueFalse);
        readBoardTempButton.setEnabled(trueFalse);
        readVoltage1Button.setEnabled(trueFalse);
        readVoltage2Button.setEnabled(trueFalse);
        readCurrent1Button.setEnabled(trueFalse);
        readErrorButton.setEnabled(trueFalse);
        readSolderTimesButton.setEnabled(trueFalse);
        writeSolderTimesButton.setEnabled(trueFalse);
        readMosfetTempButton.setEnabled(trueFalse);
//        readSolderLogButton.setEnabled(trueFalse);
    };

    /**
     * 按钮监听
     */
    private void initListener() {
        // 串口选择
        serialPortNameComboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                serialPortNameList = SerialCommTools.getPortNamelist(serialEGCNOButton.getText());
                if (serialPortNameList.size() < 1) {

                    if (!"English".equals(serialEGCNOButton.getText())) {
                        ShowUtils.warningMessage("No valid serial port is found！");
                    }else {
                        ShowUtils.warningMessage("没有搜索到有效串口！");
                    }
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
            if (dataArray[1] == 0x03) {
                int size = dataArray[2];
                if (size == 2) {
                    int data = dataArray[3] & 0xFF;
                    data <<= 8;
                    data |= dataArray[4] & 0xFF;
                    switch (startAddress) {
                        case 1:
                            resonantFreqTextField.setText(Integer.toString(data) + "KHz");
                            break;
                        case 2:
                            powerConsumptionTextField.setText(Integer.toString(data) + "J");
                            break;
                        case 3:
                            setCurrentTextField.setText(Integer.toString(data) + "mA");
                            break;
                        case 4:
                            boardTempTextField.setText(Integer.toString(data) + "℃");
                            break;
                        case 5:
                            mosfetTempTextField.setText(Integer.toString(data) + "℃");
                            break;
                        case 6:
                            headTempTextField.setText(Integer.toString(data) + "℃");
                            break;
                        case 14:
                            voltage1TextField.setText(Integer.toString(data) + "mV");
                            break;
                        case 15:
                            voltage2TextField.setText(Integer.toString(data) + "mV");
                            break;
                        case 16:
                            current1TextField.setText(Integer.toString(data) + "mA");
                            break;
                        case 19:
                            debugTextField.setText(Integer.toString(data));
                            break;
                        case 7:
                            StringBuilder builder = new StringBuilder();
                            if (data == 0) {

                                if (!"English".equals(serialEGCNOButton.getText())) {
                                    builder.append("null");
                                }else {
                                    errorTextArea.setText("无");
                                }
                            } else {
                                if ((data & (1 << 0)) != 0) {

                                    if (!"English".equals(serialEGCNOButton.getText())) {
                                        builder.append("The power supply circuit");
                                    }else {
                                        builder.append("电源短路,");
                                    }
                                }
                                if ((data & (1 << 1)) != 0) {

                                    if (!"English".equals(serialEGCNOButton.getText())) {
                                        builder.append("Low welding current");
                                    }else {
                                        builder.append("焊接电流低,");
                                    }
                                }
                                if ((data & (1 << 2)) != 0) {

                                    if (!"English".equals(serialEGCNOButton.getText())) {
                                        builder.append("Abnormal mainboard temperature");
                                    }else {
                                        builder.append("主板温度异常,");
                                    }
                                }
                                if ((data & (1 << 3)) != 0) {

                                    if (!"English".equals(serialEGCNOButton.getText())) {
                                        builder.append("The MOS tube temperature is abnormal");
                                    }else {
                                        builder.append("MOS管温度异常,");
                                    }
                                }
                                if ((data & (1 << 4)) != 0) {


                                    if (!"English".equals(serialEGCNOButton.getText())) {
                                        builder.append("The welding head temperature is abnormal");
                                    }else {
                                        builder.append("焊头温度异常");
                                    }
                                }
                                errorTextArea.setText(builder.toString());
                            }
                            break;
                        default:
                            break;
                    }
                } else if (size == 4) {
                    if (startAddress == 11) {
                        int data = dataArray[3] & 0xFF;
                        data <<= 8;
                        data |= dataArray[4] & 0xFF;
                        data <<= 8;
                        data |= dataArray[5] & 0xFF;
                        data <<= 8;
                        data |= dataArray[6] & 0xFF;
                        timestamp = data;
                        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.ofHours(8));
                        timestampTextField.setText(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    }
                    if (startAddress == 17) {
                        long data = dataArray[3] & 0xFF;
                        data <<= 8;
                        data |= dataArray[4] & 0xFF;
                        data <<= 8;
                        data |= dataArray[5] & 0xFF;
                        data <<= 8;
                        data |= dataArray[6] & 0xFF;
                        solderTimesTextField.setText(Long.toString(data));
                    }
                }
            } else if (dataArray[1] == 0x05) {

            }
        };

        serialEGCNOButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("English".equals(serialEGCNOButton.getText())) {

                    serialPortNameLabel.setText("Serial No");
                    serialPortBaudLabel.setText("Baud Rate");
                    inverterAddrLabel.setText("station No");
                    inverterOperatePanel.setBorder(BorderFactory.createTitledBorder("Inverter Parameters"));
                    setResenantFreqLabel.setText("Resonant Frequency");
                    powerConsumptionLabel.setText("Electricity Consumption");
                    setCurrentLabel.setText("Set Current");
                    tempFeedbackLabel.setText("Temperature Feedback");
                    currentFeedbackLabel.setText("Current Control");
                    timestampLabel.setText("Delivery Time");
                    stopLabel.setText("Disable Mode");
                    boardTempLabel.setText("CPU Temperature");
                    mosfetTempLabel.setText("MOS Temperature");
                    headTempLabel.setText("Head Temperature");
                    voltage1Label.setText("Voltage 1");
                    voltage2Label.setText("Voltage 2");
                    current1Label.setText("Current Interface");
                    errorLabel.setText("Error Message");
                    solderTimesLabel.setText("Welding Number");
                    debugLabel.setText("Debug Switch");
                    logPanel.setBorder(BorderFactory.createTitledBorder("Log"));
                    serialPortBoardPanel.setBorder(BorderFactory.createTitledBorder("SerialPort Settings"));


                    if ("English".equals(serialEGCNOButton.getText())
                            &&"关闭串口".equals(serialPortOpenButton.getText())){
                        serialPortOpenButton.setText("Close Serial");
                    }else {
                    if (!"English".equals(serialEGCNOButton.getText())) {
                        serialPortOpenButton.setText("打开串口");
                    }else {
                        serialPortOpenButton.setText("Open Serial");
                    }}
                    readResonantFreqButton.setText("Read");
                    readResonantFreqButton.setText("Read");
                    writeResonantFreqButton.setText("Write");
                    readPowerConsumptionButton.setText("Read");
                    readSetCurrentButton.setText("Read");
                    writeSetCurrentButton.setText("Write");
                    readTempFeedbackButton.setText("Read");
                    writeTempFeedbackButton.setText("Write");
                    readCurrentFeedbackButton.setText("Read");
                    writeCurrentFeedbackButton.setText("Write");
                    writeStopButton.setText("setting");
                    readBoardTempButton.setText("Read");
                    readMosfetTempButton.setText("Read");
                    readHeadTempButton.setText("Read");
                    readVoltage1Button.setText("Read");
                    readVoltage2Button.setText("Read");
                    readCurrent1Button.setText("Read");
                    readErrorButton.setText("Read");
                    readSolderTimesButton.setText("Read");
                    writeSolderTimesButton.setText("Write");
                    readDebugButton.setText("Read");
                    writeDebugButton.setText("Write");
                    headTempAutoReadCheckBox.setText("Auto Read");
                    headTempAutoReadMOSCheckBox.setText("Auto Read");
                    headTempAutoReadheadCheckBox.setText("Auto Read");
                    readTimestampButton.setText("Read");
                    writeTimestampButton.setText("Write");
                    setTitle("Electromagnetic welding equipment control software v1.0");
                    serialEGCNOButton.setText("简体中文");



                }
                else{

                    serialPortNameLabel.setText("串口号");
                    serialPortBaudLabel.setText("波特率");
                    inverterAddrLabel.setText("地址");
                    inverterOperatePanel.setBorder(BorderFactory.createTitledBorder("逆变器参数"));
                    setResenantFreqLabel.setText("谐振频率");
                    powerConsumptionLabel.setText("用电量");
                    setCurrentLabel.setText("设定电流");
                    tempFeedbackLabel.setText("温度反馈");
                    currentFeedbackLabel.setText("电流控制");
                    timestampLabel.setText("出厂时间");
                    stopLabel.setText("停用模式");
                    boardTempLabel.setText("主板温度");
                    mosfetTempLabel.setText("MOS温度");
                    headTempLabel.setText("焊头温度");
                    voltage1Label.setText("电压1");
                    voltage2Label.setText("电压2");
                    current1Label.setText("电流接口");
                    errorLabel.setText("错误信息");
                    solderTimesLabel.setText("焊接次数");
                    debugLabel.setText("调试开关");
                    logPanel.setBorder(BorderFactory.createTitledBorder("日志"));
                    serialPortBoardPanel.setBorder(BorderFactory.createTitledBorder("串口设置"));


                    if ("简体中文".equals(serialEGCNOButton.getText())
                            &&"Close Serial".equals(serialPortOpenButton.getText())){
                        serialPortOpenButton.setText("关闭串口");
                    }else {
                    if (!"简体中文".equals(serialEGCNOButton.getText())) {
                        serialPortOpenButton.setText("Open Serial");
                    }else {
                        serialPortOpenButton.setText("打开串口");
                    }}

                    readResonantFreqButton.setText("读取");
                    readResonantFreqButton.setText("读取");
                    writeResonantFreqButton.setText("写入");
                    readPowerConsumptionButton.setText("读取");
                    readSetCurrentButton.setText("读取");
                    writeSetCurrentButton.setText("写入");
                    readTempFeedbackButton.setText("读取");
                    writeTempFeedbackButton.setText("写入");
                    readCurrentFeedbackButton.setText("读取");
                    writeCurrentFeedbackButton.setText("写入");
                    writeStopButton.setText("设置");
                    readBoardTempButton.setText("读取");
                    readMosfetTempButton.setText("读取");
                    readHeadTempButton.setText("读取");
                    readVoltage1Button.setText("读取");
                    readVoltage2Button.setText("读取");
                    readCurrent1Button.setText("读取");
                    readErrorButton.setText("读取");
                    readSolderTimesButton.setText("读取");
                    writeSolderTimesButton.setText("写入");
                    readDebugButton.setText("读取");
                    writeDebugButton.setText("写入");
                    headTempAutoReadCheckBox.setText("自动读取");
                    headTempAutoReadMOSCheckBox.setText("自动读取");
                    headTempAutoReadheadCheckBox.setText("自动读取");
                    readTimestampButton.setText("读取");
                    writeTimestampButton.setText("写入");
                    setTitle("电磁焊接设备控制软件 v1.0");
                    serialEGCNOButton.setText("English");
                }
            }
        });

        // 打开|关闭串口
        serialPortOpenButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (("打开串口".equals(serialPortOpenButton.getText())||"Open Serial".equals(serialPortOpenButton.getText())) && Objects.nonNull(serialPortOpenButton)) {
                    String serialPortName = serialPortNameComboBox.getSelectedItem().toString();
                    int serialPortBaudRate = Integer.parseInt(serialPortBaudrateComboBox.getSelectedItem().toString());
                    try {
                        serialport = SerialCommTools.openPort(serialPortName, serialPortBaudRate,serialEGCNOButton.getText());
                        if (!"English".equals(serialEGCNOButton.getText())) {
                            serialPortOpenButton.setText("Close Serial");
                        }else {
                            serialPortOpenButton.setText("关闭串口");
                        }
                        consumerButtonDisplay.accept(true);
                        //接收监听
                        SerialCommTools.addListener(serialport, new SerialCommTools.DataAvailableListener() {
                            @Override
                            public void dataAvailable() {
                                if (Objects.isNull(serialport)) {
                                    if (!"English".equals(serialEGCNOButton.getText())) {
                                        serialPortOpenButton.setText("The serial port object is empty. Listening failed");
                                    }else {
                                        ShowUtils.warningMessage("串口对象为空，监听失败");
                                    }

                                } else {
                                    receiveBuff = SerialCommTools.receiveData(serialport,serialEGCNOButton.getText());
                                    if (Objects.nonNull(receiveBuff) && receiveBuff.length > 0) {
                                        if (!"English".equals(serialEGCNOButton.getText())) {

                                            log.info("Receive  Data <<<<<<<<<< {}", SerialCommTools.byteArrayToStringFunction.apply(receiveBuff));
                                        }else {
                                            log.info("收到数据 <<<<<<<<<< {}", SerialCommTools.byteArrayToStringFunction.apply(receiveBuff));
                                        }

                                        if (CRCTools.checkModbusCRC(receiveBuff, receiveBuff.length,serialEGCNOButton.getText())) {
                                            String addrString = inverterAddrTextField.getText();
                                            int addr = Integer.parseInt(addrString);
                                            if (receiveBuff[0] == addr) {
                                                inverterReceiveConsumer.accept(receiveBuff);
                                            }
                                        } else {
                                            if (!"English".equals(serialEGCNOButton.getText())) {

                                                log.error("CRC Check Failure ********************");
                                            }else {
                                                log.error("CRC 校验失败 ********************");
                                            }

                                        }
                                    }
                                }
                            }
                        },serialEGCNOButton.getText());
                    } catch (PortInUseException e2) {
                        if (!"English".equals(serialEGCNOButton.getText())) {

                            ShowUtils.warningMessage("The serial port is occupied. Procedure");
                            log.error("Open serial{}failure，The serial port is occupied{}", serialPortName, e2.getLocalizedMessage());
                        }else {
                            ShowUtils.warningMessage("打开失败，串口被占用");
                            log.error("打开串口{}失败，串口被占用{}", serialPortName, e2.getLocalizedMessage());
                        }

                    }
                } else {
                    SerialCommTools.closePort(serialport,serialEGCNOButton.getText());
                    if (!"English".equals(serialEGCNOButton.getText())) {
                        serialPortOpenButton.setText("Open Serial");

                    }else {
                        serialPortOpenButton.setText("打开串口");
                    }

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
            command[6] = (byte) crcH;
            command[7] = (byte) crcL;
            SerialCommTools.sendData(serialport, command,serialEGCNOButton.getText());
            startAddress = start;
        };

        readResonantFreqButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(1, 1);
            }
        });

        readPowerConsumptionButton.addActionListener(new ActionListener() {
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

        readTempFeedbackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(8, 1);
            }
        });

        readCurrentFeedbackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(9, 1);
            }
        });

        readDebugButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(19, 1);
            }
        });

        readTimestampButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(11, 2);
            }
        });

        readBoardTempButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(4, 1);
            }
        });

        readMosfetTempButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(5, 1);
            }
        });

        readHeadTempButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(6, 1);
            }
        });

        readVoltage1Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(14, 1);
            }
        });

        readVoltage2Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(15, 1);
            }
        });

        readCurrent1Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(16, 1);
            }
        });

        readErrorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(7, 1);
            }
        });

        readSolderTimesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterReadConsumer.accept(17, 2);
            }
        });

//        readSolderLogButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                byte[] command = new byte[5];
//                String addrString = inverterAddrTextField.getText();
//                int addrInt = Integer.parseInt(addrString);
//                command[0] = (byte) addrInt;
//                command[1] = (byte) 0xAA;
//                command[2] = (byte) 0xAA;
//                String crcHexString = CRCTools.getModbusCRC(command, 3);
//                int crcH = SerialCommTools.hexStringToInt(crcHexString.substring(0, 2));
//                int crcL = SerialCommTools.hexStringToInt(crcHexString.substring(2, 4));
//                command[3] = (byte)crcH;
//                command[4] = (byte)crcL;
//                SerialCommTools.sendData(serialport, command);
//            }
//        });

        Runnable runnable = () -> {
            while (true) {
                try {
                    if (Objects.nonNull(serialport) &&( serialPortOpenButton.getText().equals("关闭串口")||serialPortOpenButton.getText().equals("Close Serial"))) {
                        if (headTempAutoReadCheckBox.isSelected()) {
                            //todo 单独读取
                            //inverterReadConsumer.accept(5, 1);

                            inverterReadConsumer.accept(4, 1);
                            Thread.sleep(300);
                        }
                        if (headTempAutoReadMOSCheckBox.isSelected()) {
                            inverterReadConsumer.accept(5, 1);
                            Thread.sleep(300);
                        }
                        if (headTempAutoReadheadCheckBox.isSelected()) {
                            inverterReadConsumer.accept(6, 1);
                            Thread.sleep(300);
                        }
                    }
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    log.error("{}", e);
                }
            }
        };
        new Thread(runnable).start();


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
                case 1:
                    dataString = resonantFreqTextField.getText().replaceAll("(?i)khz", "");
                    break;
                case 2:
                    dataString = powerConsumptionTextField.getText();
                    break;
                case 3:
                    dataString = setCurrentTextField.getText().replaceAll("(?i)ma", "");
                    break;
                case 4:
                    dataString = tempFeedbackComBox.getSelectedItem().toString();
                    break;
                case 5:
                    dataString = currentFeedbackComBox.getSelectedItem().toString();
                    break;
                case 19:
                    dataString = debugTextField.getText();
                    break;
                case 7:
                    dataString = timestampTextField.getText();
                    break;
                case 8:
                    dataString = timestampTextField.getText();
                    break;
                case 9:
                    dataString = "170";
                    break;
                case 17:
                    dataString = solderTimesTextField.getText();
                    break;
                case 18:
                    dataString = solderTimesTextField.getText();
                    break;
                default:
                    break;
            }
            int dataInt = 0;
            int dataH = 0;
            int dataL = 0;
            if (StringUtils.isNotEmpty(dataString)) {
                dataInt = Integer.parseInt(dataString);
                dataH = dataInt >> 8;
                dataL = dataInt & 0xFF;
                if (start == 7) {
                    dataH = dataInt >> 24;
                    dataL = (dataInt >> 16) & 0xFF;
                } else if (start == 8) {
                    dataH = dataInt >> 8;
                    dataL = dataInt & 0xFF;
                }

                if (start == 17) {
                    dataH = dataInt >> 24;
                    dataL = (dataInt >> 16) & 0xFF;
                } else if (start == 18) {
                    dataH = dataInt >> 8;
                    dataL = dataInt & 0xFF;
                }
            }
            command[7] = (byte) dataH;
            command[8] = (byte) dataL;
            String crcHexString = CRCTools.getModbusCRC(command, 9);
            if (crcHexString.length() == 1) {
                crcHexString = "000" + crcHexString;
            }
            if (crcHexString.length() == 2) {
                crcHexString = "00" + crcHexString;
            }
            if (crcHexString.length() == 3) {
                crcHexString = "0" + crcHexString;
            }

            int crcH = SerialCommTools.hexStringToInt(crcHexString.substring(0, 2));
            int crcL = SerialCommTools.hexStringToInt(crcHexString.substring(2, 4));
            command[9] = (byte) crcH;
            command[10] = (byte) crcL;
            SerialCommTools.sendData(serialport, command,serialEGCNOButton.getText());
        };

        writeResonantFreqButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterWriteConsumer.accept(1);
            }
        });
        //死区占比改电量 todo
//        writeDeadTimeRatioButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                inverterWriteConsumer.accept(2);
//            }
//        });

        writeSetCurrentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterWriteConsumer.accept(3);
            }
        });

        writeTempFeedbackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterWriteConsumer.accept(4);
            }
        });

        writeCurrentFeedbackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterWriteConsumer.accept(5);
            }
        });

        writeDebugButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterWriteConsumer.accept(19);
            }
        });

        writeTimestampButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
                timestampTextField.setText(Long.toString(timestamp));
                inverterWriteConsumer.accept(7);
                try {
                    Thread.sleep(100);
                } catch (Exception e2) {
                    log.error("{}", e2.getLocalizedMessage());
                }
                inverterWriteConsumer.accept(8);
            }
        });

        writeStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterWriteConsumer.accept(9);
            }
        });

        writeSolderTimesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inverterWriteConsumer.accept(17);
                try {
                    Thread.sleep(100);
                } catch (Exception e2) {
                    log.error("{}", e2.getLocalizedMessage());
                }
                inverterWriteConsumer.accept(18);
            }
        });
    }

    /**
     * 初始化可用串口号
     */
    private void initSerialPortList() {
        serialPortNameList = SerialCommTools.getPortNamelist(serialEGCNOButton.getText());
        if(serialPortNameList.size() < 1) {

            if (!"English".equals(serialEGCNOButton.getText())) {
                serialPortOpenButton.setText("No available serial port was found");

            }else {
                ShowUtils.warningMessage("没有找到可用串口");
            }
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
     */
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