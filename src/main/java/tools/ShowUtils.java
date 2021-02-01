package tools;

import javax.swing.*;

public class ShowUtils {

    public static void warningMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "title", JOptionPane.ERROR_MESSAGE);
    }
}
