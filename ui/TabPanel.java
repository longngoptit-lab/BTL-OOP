package qlht.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/** Lớp cha của mọi tab: giữ AppContext và quy định mỗi tab phải biết tự làm mới dữ liệu. */
public abstract class TabPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    protected final transient AppContext ctx;

    protected TabPanel(AppContext ctx) {
        super(new BorderLayout(0, 8));
        this.ctx = ctx;
        setBorder(new EmptyBorder(10, 15, 10, 15));
    }

    /** Nạp lại dữ liệu từ CSDL và vẽ lại tab. */
    public abstract void refresh();

    protected Window window() {
        return SwingUtilities.getWindowAncestor(this);
    }

    protected static JPanel titled(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title), new EmptyBorder(4, 6, 6, 6)));
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    /** Khung "thêm mới": form ở trên, nút ở dưới (căn phải). */
    protected static JPanel addBox(String title, JComponent form, JButton addButton) {
        JPanel box = new JPanel(new BorderLayout());
        box.add(form, BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(addButton);
        box.add(south, BorderLayout.SOUTH);
        return titled(title, box);
    }

    protected static JPanel tableBox(String title, RowTable table) {
        return titled(title, new JScrollPane(table));
    }

    protected static JPanel buttonsRight(JComponent... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        for (JComponent c : comps) p.add(c);
        return p;
    }
}
