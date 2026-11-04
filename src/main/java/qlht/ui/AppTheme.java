package qlht.ui;

import java.awt.*;
import java.awt.geom.Path2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;

/** Màu sắc, typography và các thành phần dùng chung của giao diện. */
public final class AppTheme {
    public static final Color BACKGROUND = new Color(0xF4F6FA);
    public static final Color SURFACE = Color.WHITE;
    public static final Color TEXT = new Color(0x202C3E);
    public static final Color MUTED = new Color(0x758195);
    public static final Color LINE = new Color(0xE6EBF1);
    public static final Color PRIMARY = new Color(0x217568);
    public static final Color SOFT_GREEN = new Color(0xE9F5EF);
    public static final Color NAVY = new Color(0x182B32);
    public static final Color DANGER = new Color(0xB54745);

    private AppTheme() { }

    public static void install() {
        com.formdev.flatlaf.FlatLightLaf.setup();
        UIManager.put("defaultFont", new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 13));
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.focusColor", new Color(0x9CCBBC));
        UIManager.put("Component.borderColor", LINE);
        UIManager.put("Component.focusedBorderColor", PRIMARY);
        UIManager.put("Component.accentColor", PRIMARY);
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.margin", new Insets(8, 10, 8, 10));
        UIManager.put("FormattedTextField.margin", new Insets(7, 8, 7, 8));
        UIManager.put("TextArea.margin", new Insets(9, 10, 9, 10));
        UIManager.put("ComboBox.padding", new Insets(7, 8, 7, 8));
        UIManager.put("ComboBox.buttonBackground", SURFACE);
        UIManager.put("Spinner.background", SURFACE);
        UIManager.put("ScrollPane.background", SURFACE);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(LINE));
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", SOFT_GREEN);
        UIManager.put("Table.selectionForeground", PRIMARY);
        UIManager.put("Button.font", new FontUIResource(Font.SANS_SERIF, Font.BOLD, 12));
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.background", SURFACE);
        UIManager.put("Button.margin", new Insets(9, 16, 9, 16));
        UIManager.put("OptionPane.yesButtonText", "Có");
        UIManager.put("OptionPane.noButtonText", "Không");
        UIManager.put("OptionPane.cancelButtonText", "Hủy");
    }

    public static JLabel label(String text, int size, int weight, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, weight, size));
        label.setForeground(color);
        return label;
    }

    public static JPanel transparent(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    public static JButton primary(JButton button) {
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        return button;
    }

    public static JButton danger(JButton button) {
        button.setForeground(DANGER);
        return button;
    }

    /** Thẻ bo góc với đường viền nhẹ. */
    public static class Card extends JPanel {
        private static final long serialVersionUID = 1L;
        public Card(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBackground(SURFACE);
            setBorder(new EmptyBorder(18, 20, 18, 20));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.setColor(LINE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Biểu tượng vector không phụ thuộc font hoặc emoji. */
    public static Icon icon(int kind, Color color) {
        return new Icon() {
            @Override public int getIconWidth() { return 20; }
            @Override public int getIconHeight() { return 20; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D p = (Graphics2D) g.create();
                p.translate(x, y);
                p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                p.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                p.setColor(color);
                switch (kind) {
                    case 0 -> {
                        for (int a = 0; a < 2; a++) for (int b = 0; b < 2; b++)
                            p.drawRoundRect(3 + a * 9, 3 + b * 9, 5, 5, 2, 2);
                    }
                    case 1 -> {
                        p.drawRoundRect(4, 2, 12, 16, 2, 2);
                        p.drawLine(7, 2, 7, 18); p.drawLine(10, 7, 13, 7); p.drawLine(10, 10, 13, 10);
                    }
                    case 2, 3 -> {
                        p.drawRoundRect(2, 4, 16, 14, 3, 3);
                        p.drawLine(2, 8, 18, 8); p.drawLine(6, 2, 6, 6); p.drawLine(14, 2, 14, 6);
                        if (kind == 3) { p.drawLine(6, 13, 9, 15); p.drawLine(9, 15, 14, 10); }
                        else { p.drawLine(6, 11, 8, 11); p.drawLine(12, 14, 14, 14); }
                    }
                    case 4 -> {
                        p.drawRoundRect(3, 2, 14, 16, 3, 3);
                        p.drawLine(6, 8, 8, 10); p.drawLine(8, 10, 13, 6);
                        p.drawLine(6, 14, 13, 14);
                    }
                    case 5 -> {
                        p.drawRoundRect(3, 2, 14, 16, 2, 2);
                        p.drawLine(7, 6, 13, 6); p.drawLine(7, 10, 13, 10); p.drawLine(7, 14, 11, 14);
                    }
                    default -> {
                        Path2D path = new Path2D.Double();
                        path.moveTo(4, 10); path.lineTo(8, 14); path.lineTo(16, 6); p.draw(path);
                    }
                }
                p.dispose();
            }
        };
    }
}
