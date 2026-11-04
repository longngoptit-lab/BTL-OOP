package qlht.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

/** Bảng chỉ đọc, mỗi dòng gắn với một ID trong CSDL và một kiểu tô màu. */
public class RowTable extends JTable {
    private static final long serialVersionUID = 1L;

    /** Kiểu tô màu của một dòng: màu chữ và màu nền (null = mặc định). */
    public enum Style {
        NONE(null, null),
        CONFLICT(new Color(0xc62828), new Color(0xffebee)),
        WARN(new Color(0xe65100), new Color(0xfff3e0)),
        DONE(new Color(0x2e7d32), null),
        OVERDUE(new Color(0xc62828), null),
        URGENT(new Color(0xe65100), null);

        private final Color fg;
        private final Color bg;

        Style(Color fg, Color bg) {
            this.fg = fg;
            this.bg = bg;
        }
    }

    public static final class Row {
        private final int id;
        private final Style style;
        private final Object[] cells;

        public Row(int id, Style style, Object... cells) {
            this.id = id;
            this.style = style;
            this.cells = cells;
        }
    }

    private static final class Model extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private final String[] columns;
        private List<Row> rows = new ArrayList<>();

        Model(String[] columns) { this.columns = columns; }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int c) { return columns[c]; }
        @Override public Object getValueAt(int r, int c) { return rows.get(r).cells[c]; }
    }

    private final transient Model model;
    private String emptyTitle = "Chưa có dữ liệu";
    private String emptySubtitle = "Thêm mục đầu tiên bằng biểu mẫu phía trên để bắt đầu.";

    public void setEmptyMessage(String title, String subtitle) {
        emptyTitle = title;
        emptySubtitle = subtitle;
        repaint();
    }

    public RowTable(String[] columns, int[] widths) {
        this(new Model(columns), widths);
    }

    private RowTable(Model model, int[] widths) {
        super(model);
        this.model = model;
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setRowHeight(44);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        setForeground(AppTheme.TEXT);
        setBackground(AppTheme.SURFACE);
        setSelectionBackground(AppTheme.SOFT_GREEN);
        setSelectionForeground(AppTheme.PRIMARY);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        getTableHeader().setPreferredSize(new Dimension(0, 40));
        getTableHeader().setDefaultRenderer((table, value, selected, focus, row, column) -> {
            JLabel label = AppTheme.label(String.valueOf(value), 11, Font.BOLD, AppTheme.MUTED);
            label.setOpaque(true);
            label.setBackground(new Color(0xF7F9FB));
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.LINE),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            return label;
        });
        setFillsViewportHeight(true);
        getTableHeader().setReorderingAllowed(false);
        for (int i = 0; i < widths.length; i++) {
            getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /** Thay toàn bộ dữ liệu, giữ lại dòng đang chọn (nếu vẫn còn). */
    public void setRows(List<Row> rows) {
        Integer keep = selectedId();
        model.rows = rows;
        model.fireTableDataChanged();
        if (keep != null) {
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i).id == keep) {
                    setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    /** ID của dòng đang chọn, hoặc null nếu chưa chọn dòng nào. */
    public Integer selectedId() {
        int r = getSelectedRow();
        return (r < 0 || r >= model.rows.size()) ? null : model.rows.get(r).id;
    }

    /** Nhấp đúp vào một DÒNG (không phải tiêu đề cột) thì chạy `action`. */
    public void onDoubleClick(Runnable action) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)
                        && rowAtPoint(e.getPoint()) >= 0) {
                    action.run();
                }
            }
        });
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (!isRowSelected(row)) {
            Style s = model.rows.get(row).style;
            c.setForeground(s.fg != null ? s.fg : getForeground());
            c.setBackground(s.bg != null ? s.bg : row % 2 == 0 ? getBackground() : new Color(0xFAFBFD));
        }
        if (c instanceof JLabel label) {
            label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            Object value = getValueAt(row, column);
            label.setToolTipText(value == null ? null : value.toString());
        }
        return c;
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getRowCount() != 0) return;
        Graphics2D p = (Graphics2D) g.create();
        p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int middle = getWidth() / 2;
        int height = getVisibleRect().height;
        boolean compact = height < 140;
        int y = compact ? Math.max(12, height / 2 - 8) : Math.min(height / 2 - 15, 110);
        if (!compact) {
            p.setColor(new Color(0xEEF4F1));
            p.fillRoundRect(middle - 23, y - 30, 46, 46, 16, 16);
            AppTheme.icon(5, AppTheme.PRIMARY).paintIcon(this, p, middle - 10, y - 17);
        }
        int titleY = compact ? y : y + 40;
        p.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        p.setColor(AppTheme.TEXT);
        p.drawString(emptyTitle, middle - p.getFontMetrics().stringWidth(emptyTitle) / 2, titleY);
        if (height >= 70) {
            p.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            p.setColor(AppTheme.MUTED);
            p.drawString(emptySubtitle, middle - p.getFontMetrics().stringWidth(emptySubtitle) / 2, titleY + 22);
        }
        p.dispose();
    }
}
