package org.example;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class UIStyle {
    public static final Color PRIMARY_COLOR = new Color(100, 149, 237); // 淡蓝色
    public static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    public static final Color TEXT_COLOR = new Color(51, 51, 51);
    public static final Font MAIN_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    public static final Font TITLE_FONT = new Font("微软雅黑", Font.BOLD, 16);

    public static void setupGlobalUI() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 设置全局UI属性
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("TextField.font", MAIN_FONT);
        UIManager.put("TextArea.font", MAIN_FONT);
        UIManager.put("Label.font", MAIN_FONT);
        UIManager.put("Button.font", MAIN_FONT);
    }

    public static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    public static Border createRoundedBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    public static void decorateFrame(JFrame frame) {
        frame.getContentPane().setBackground(BACKGROUND_COLOR);
        ((JComponent) frame.getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public static void decorateTextComponent(JTextComponent component) {
        component.setFont(MAIN_FONT);
        component.setBackground(Color.WHITE);
        component.setBorder(createRoundedBorder());
    }
}
