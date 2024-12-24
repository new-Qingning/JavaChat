// src/main/java/org/example/UserCellRenderer.java
package org.example;

import javax.swing.*;
import java.awt.*;

public class UserCellRenderer extends JLabel implements ListCellRenderer<User> {
    @Override
    public Component getListCellRendererComponent(JList<? extends User> list, User user, int index, boolean isSelected, boolean cellHasFocus) {
        setText(user.getUsername() + " (" + user.getPassword() + ")");
        setOpaque(true);
        setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
        setForeground(Color.BLACK);

        return this;
    }
}

class CircleIcon implements Icon {
    private final Color color;

    public CircleIcon(Color color) {
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillOval(x, y, getIconWidth(), getIconHeight());
    }

    @Override
    public int getIconWidth() {
        return 10;
    }

    @Override
    public int getIconHeight() {
        return 10;
    }
}