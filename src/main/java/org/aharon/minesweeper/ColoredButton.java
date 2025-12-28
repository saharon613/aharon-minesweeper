package org.aharon.minesweeper;

import javax.swing.*;
import java.awt.*;

class ColoredButton extends JButton {
    private Color textColor;

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        textColor = fg;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!isEnabled() && textColor != null && getText() != null && !getText().isEmpty()) {
            g.setColor(textColor);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g.drawString(getText(), x, y);
        }
    }
}
