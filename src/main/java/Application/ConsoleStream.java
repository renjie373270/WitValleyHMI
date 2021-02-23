package Application;

import javax.swing.*;
import java.io.ByteArrayOutputStream;


public class ConsoleStream extends ByteArrayOutputStream {

    private JTextArea logTextArea;

    public ConsoleStream(JTextArea logTextArea) {
        this.logTextArea = logTextArea;
    }

    @Override
    public synchronized void write(int b) {
        super.write(b);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        String str = new String(b);
        if(logTextArea.getText().length() > 10000) {
            logTextArea.setText("");
        }
        logTextArea.append(str);
        logTextArea.setCaretPosition(logTextArea.getText().length());
    }
}
