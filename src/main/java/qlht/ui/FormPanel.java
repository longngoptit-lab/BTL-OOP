package qlht.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;

/** Khung form dạng lưới "nhãn : ô nhập". `pairs` = số cặp nhãn/ô trên một hàng (1 hoặc 2). */
public class FormPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final int pairs;
    private int cell = 0;
    private int row = 0;

    public FormPanel(int pairs) {
        super(new GridBagLayout());
        this.pairs = pairs;
        setOpaque(false);
    }

    /** Thêm một cặp nhãn + ô vào vị trí kế tiếp. */
    public void addField(String label, JComponent field) {
        place(label, field, row, (cell % pairs) * 2, 1);
        cell++;
        if (cell % pairs == 0) row++;
    }

    /** Thêm một hàng riêng, ô nhập trải hết bề ngang (dùng cho vùng văn bản nhiều dòng). */
    public void addWide(String label, JComponent field) {
        if (cell % pairs != 0) row++;
        place(label, field, row, 0, pairs * 2 - 1);
        row++;
        cell = 0;
    }

    private void place(String label, JComponent field, int r, int col, int span) {
        boolean tall = field instanceof JScrollPane;
        Insets in = new Insets(5, 0, 5, 16);

        GridBagConstraints l = new GridBagConstraints();
        l.gridx = col;
        l.gridy = r;
        l.anchor = tall ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;
        l.insets = in;
        JLabel caption = AppTheme.label(label.replace(":", ""), 12, java.awt.Font.PLAIN, AppTheme.MUTED);
        caption.setLabelFor(field);
        add(caption, l);

        GridBagConstraints f = new GridBagConstraints();
        f.gridx = col + 1;
        f.gridy = r;
        f.gridwidth = span;
        f.weightx = 1;
        f.anchor = GridBagConstraints.WEST;
        boolean stretch = field instanceof JTextComponent || field instanceof JComboBox || tall;
        f.fill = stretch ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        f.insets = in;
        add(field, f);
    }
}
