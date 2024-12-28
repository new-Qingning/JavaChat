package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

public class IconLoader {
    private static final ImageIcon SHEEP_ICON;

    static {
        ImageIcon icon = null;
        try {
            // 确保正确加载资源
            URL iconURL = IconLoader.class.getClassLoader().getResource("images/sheep.png");
            if (iconURL != null) {
                icon = new ImageIcon(iconURL);
                // 设置统一的图标大小
                Image scaledImage = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImage);
                System.out.println("成功加载图标");
            } else {
                System.err.println("错误: 找不到图标文件 images/sheep.png");
                // 尝试直接从文件系统加载
                icon = new ImageIcon("src/main/resources/images/sheep.png");
                if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    Image scaledImage = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaledImage);
                    System.out.println("从文件系统成功加载图标");
                }
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
            System.out.println("已设置窗口图标: " + window.getTitle());
        } else {
            System.err.println("警告: 无法设置窗口图标 - " + window.getTitle());
        }
    }
}
