package org.example;

import javax.swing.*;
import java.awt.*;

public class IconLoader {
    private static final ImageIcon SHEEP_ICON;

    static {
        ImageIcon icon = null;
        try {
            // 从resources目录加载图标
            java.net.URL iconURL = IconLoader.class.getClassLoader().getResource("images/sheep.png");
            if (iconURL != null) {
                icon = new ImageIcon(iconURL);
                // 统一图标大小为32x32像素
                Image scaledImage = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImage);
            } else {
                System.err.println("警告: 无法找到图标文件 sheep.png");
            }
        } catch (Exception e) {
            System.err.println("错误: 加载图标失败 - " + e.getMessage());
            e.printStackTrace();
        }
        SHEEP_ICON = icon;
    }

    public static void setWindowIcon(JFrame window) {
        if (SHEEP_ICON != null && SHEEP_ICON.getImageLoadStatus() == MediaTracker.COMPLETE) {
            window.setIconImage(SHEEP_ICON.getImage());
        } else {
            System.err.println("警告: 图标未能正确加载");
        }
    }
}
