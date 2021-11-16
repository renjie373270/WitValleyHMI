package tools;

import javax.swing.*;
import java.awt.*;

public class CodeSwitchUtils extends JFrame {

    private static Graphics g;
    private static int initX, initY;

    public static void updateSwitch(JFrame frame, int code) {
        g = frame.getGraphics();
        initX = 207;
        initY = 124;

        int posX = initX, posY = initY;
        //红框
        posX = initX; posY = initY;
        g.setColor(Color.RED);
        g.fillRect(posX, posY, 110, 42);

        //8个开关白框
        posX += 10; posY += 12;
        g.setColor(Color.WHITE);
        for(int i = 0; i < 8; i++) {
            g.fillRect(posX, posY, 8, 14);
            posX += 12;
        }

        //开关描色
        posX = initX; posY = initY;
        posX += 10; posY += 12;
        g.setColor(Color.GRAY);
        for(int i = 0; i < 8; i++) {
            if((code & (1 << i)) != 0) {
                g.fillRect(posX, posY + 7, 8, 7);
            } else {
                g.fillRect(posX, posY, 8, 7);
            }
            posX += 12;
        }

        //开关编号
        posX = initX; posY = initY;
        posX += 12; posY += 40;
        g.setColor(Color.WHITE);
        for(int i = 0; i < 8; i++) {
            g.drawString(i + 1 + "", posX, posY);
            posX += 12;
        }

        //开关上面ON
        posX = initX; posY = initY;
        posX += 10; posY += 10;
        g.setColor(Color.WHITE);
        g.drawString("ON", posX, posY);

        //开关厂家LOGO
        posX = initX; posY = initY;
        posX += 75; posY += 10;
        g.setColor(Color.WHITE);
        g.drawString("KXTE", posX, posY);
        g.dispose();
    }
}
