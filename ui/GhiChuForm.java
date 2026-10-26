package qlht.ui;

import java.awt.Component;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import qlht.model.GhiChu;
import qlht.model.MonHoc;
import qlht.util.DateUtil;

public class GhiChuForm extends FormPanel implements EntityForm<GhiChu> {
    private static final long serialVersionUID = 1L;
    private final MonCombo mon = new MonCombo(true);
    private final JTextField tieuDe = new JTextField(30);
    private final JTextArea noiDung;
    private final JLabel ngayTao = new JLabel(" ");

    /** rows = số dòng của vùng nội dung (5 khi thêm, 14 khi xem/sửa). */
    public GhiChuForm(int pairs, int rows, int cols) {
        super(pairs);
        noiDung = new JTextArea(rows, cols);
        noiDung.setLineWrap(true);
        noiDung.setWrapStyleWord(true);
        addField("Môn học:", mon);
        addField("Tiêu đề:", tieuDe);
        addWide("Nội dung:", new JScrollPane(noiDung));
        addWide("Ngày tạo:", ngayTao);
    }

    @Override public JComponent component() { return this; }

    @Override public void reloadMon(List<MonHoc> list) { mon.reload(list); }

    @Override
    public void fill(GhiChu g) {
        mon.setMonId(g.getMonHocId());
        tieuDe.setText(g.getTieuDe());
        noiDung.setText(g.getNoiDung() == null ? "" : g.getNoiDung());
        noiDung.setCaretPosition(0);
        ngayTao.setText(DateUtil.formatVn(g.getNgayTao()));
    }

    @Override
    public void clear() {
        tieuDe.setText("");
        noiDung.setText("");
        ngayTao.setText(" ");
    }

    @Override
    public GhiChu read(Component parent) {
        String t = tieuDe.getText().trim();
        if (t.isEmpty()) {
            UiUtil.warn(parent, "Thiếu dữ liệu", "Vui lòng nhập tiêu đề ghi chú.");
            return null;
        }
        return new GhiChu(mon.getMonId(), null, t, noiDung.getText().trim(), null);
    }
}
