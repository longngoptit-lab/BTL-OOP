package qlht.ui;

import java.awt.Color;
import java.awt.Component;
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

    public RowTable(String[] columns, int[] widths) {
        this(new Model(columns), widths);
    }

    private RowTable(Model model, int[] widths) {
        super(model);
        this.model = model;
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setRowHeight(26);
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
            c.setBackground(s.bg != null ? s.bg : getBackground());
        }
        return c;
    }
}
