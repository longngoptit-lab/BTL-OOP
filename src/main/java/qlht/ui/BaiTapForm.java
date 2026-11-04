package qlht.ui;

import java.awt.Component;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import qlht.model.BaiTap;
import qlht.model.MonHoc;
import qlht.util.Const;

public class BaiTapForm extends FormPanel implements EntityForm<BaiTap> {
    private static final long serialVersionUID = 1L;
    private final MonCombo mon = new MonCombo(true);
    private final JTextField tieuDe = new JTextField(22);
    private final DatePicker han = new DatePicker();
    private final JComboBox<String> uuTien = new JComboBox<>(Const.PRIORITIES.toArray(new String[0]));
    private final JComboBox<String> trangThai = new JComboBox<>(Const.STATUSES.toArray(new String[0]));
    private final JTextArea moTa = new JTextArea(2, 40);
    private final boolean coTrangThai;

    /** coTrangThai = true khi dùng trong hộp thoại sửa (khi thêm mới, trạng thái luôn là "Chưa hoàn thành"). */
    public BaiTapForm(int pairs, boolean coTrangThai) {
        super(pairs);
        this.coTrangThai = coTrangThai;
        uuTien.setSelectedItem("Trung bình");
        moTa.setLineWrap(true);
        moTa.setWrapStyleWord(true);
        addField("Môn học:", mon);
        addField("Tiêu đề:", tieuDe);
        addField("Hạn nộp:", han);
        addField("Mức ưu tiên:", uuTien);
        if (coTrangThai) addField("Trạng thái:", trangThai);
        addWide("Mô tả:", new JScrollPane(moTa));
    }

    @Override public JComponent component() { return this; }

    @Override public void reloadMon(List<MonHoc> list) { mon.reload(list); }

    @Override
    public void fill(BaiTap b) {
        mon.setMonId(b.getMonHocId());
        tieuDe.setText(b.getTieuDe());
        han.setDate(b.getHanNop());
        uuTien.setSelectedItem(b.getMucDoUuTien());
        trangThai.setSelectedItem(b.getTrangThai());
        moTa.setText(b.getMoTa() == null ? "" : b.getMoTa());
    }

    @Override
    public void clear() {
        tieuDe.setText("");
        han.setDate(null);
        moTa.setText("");
    }

    @Override
    public BaiTap read(Component parent) {
        String t = tieuDe.getText().trim();
        if (t.isEmpty()) {
            UiUtil.warn(parent, "Thiếu dữ liệu", "Vui lòng nhập tiêu đề và hạn nộp.");
            return null;
        }
        if (han.getDate() == null) {
            UiUtil.warn(parent, "Sai định dạng", "Hạn nộp phải theo dạng dd/mm/yyyy.");
            return null;
        }
        String tt = coTrangThai ? (String) trangThai.getSelectedItem() : Const.STATUS_TODO;
        return new BaiTap(mon.getMonId(), null, t, moTa.getText().trim(), han.getDate(),
                (String) uuTien.getSelectedItem(), tt);
    }
}
