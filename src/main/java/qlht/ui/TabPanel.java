package qlht.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Màn hình nghiệp vụ với form và bảng dạng thẻ. */
public abstract class TabPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    protected final transient AppContext ctx;

    protected TabPanel(AppContext ctx) {
        super(new BorderLayout(0, 16));
        this.ctx = ctx;
        setBorder(new EmptyBorder(24, 28, 22, 28));
        setBackground(AppTheme.BACKGROUND);
    }

    public abstract void refresh();
    protected Window window() { return SwingUtilities.getWindowAncestor(this); }

    protected static JPanel titled(String title, JComponent content) {
        AppTheme.Card card = new AppTheme.Card(new BorderLayout(0, 14));
        JLabel heading = AppTheme.label(title, 14, Font.BOLD, AppTheme.TEXT);
        card.add(heading, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    protected static JPanel addBox(String title, JComponent form, JButton addButton) {
        JPanel box = AppTheme.transparent(new BorderLayout(0, 8));
        box.add(form, BorderLayout.CENTER);
        JPanel south = buttonsRight(AppTheme.primary(addButton));
        box.add(south, BorderLayout.SOUTH);
        JPanel card = titled(title, box);
        JPanel heading = AppTheme.transparent(new BorderLayout());
        heading.add(AppTheme.label(title, 14, Font.BOLD, AppTheme.TEXT), BorderLayout.WEST);
        JButton toggle = new JButton("Thu gọn");
        toggle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        toggle.setMargin(new Insets(3, 8, 3, 8));
        toggle.setToolTipText("Ẩn hoặc hiện biểu mẫu để mở rộng bảng dữ liệu");
        toggle.addActionListener(e -> {
            box.setVisible(!box.isVisible());
            toggle.setText(box.isVisible() ? "Thu gọn" : "Mở biểu mẫu");
            card.revalidate();
            card.repaint();
        });
        heading.add(toggle, BorderLayout.EAST);
        card.remove(0);
        card.add(heading, BorderLayout.NORTH);
        return card;
    }

    protected static JPanel tableBox(String title, RowTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(AppTheme.SURFACE);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        int bracket = title.indexOf('(');
        String heading = bracket < 0 ? title : title.substring(0, bracket).trim();
        JPanel card = titled(heading, scroll);
        ((JComponent) card.getComponent(0)).setToolTipText(title);
        JPanel footer = AppTheme.transparent(new BorderLayout());
        JLabel count = AppTheme.label("0 mục", 11, Font.PLAIN, AppTheme.MUTED);
        table.getModel().addTableModelListener(e -> count.setText(table.getRowCount() + " mục"));
        footer.add(count, BorderLayout.WEST);
        String hint = "Nhấp đúp để chỉnh sửa";
        if (title.contains("xanh:")) hint = "Xanh: hoàn thành · Cam: sắp đến hạn · Đỏ: quá hạn";
        else if (title.contains("dòng đỏ:")) hint = "Đỏ: trùng lịch học · Nhấp đúp để sửa";
        else if (title.contains("đỏ:")) hint = "Đỏ: trùng lịch · Cam: cần kiểm tra giờ · Nhấp đúp để sửa";
        footer.add(AppTheme.label(hint, 11, Font.PLAIN, AppTheme.MUTED), BorderLayout.EAST);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    protected static JPanel buttonsRight(JComponent... comps) {
        JPanel panel = AppTheme.transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        for (JComponent c : comps) panel.add(c);
        return panel;
    }
}
