package qlht.ui;

import java.awt.Component;
import java.text.ParseException;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import qlht.model.MonHoc;

public class MonHocForm extends FormPanel implements EntityForm<MonHoc> {
    private static final long serialVersionUID = 1L;
    private final JTextField ten = new JTextField(22);
    private final JSpinner tinChi = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
    private final JTextField giangVien = new JTextField(22);
    private final JTextField ghiChu = new JTextField(22);

    public MonHocForm(int pairs) {
        super(pairs);
        ten.setName("subject.name"); tinChi.setName("subject.credits");
        addField("Tên môn học:", ten);
        addField("Số tín chỉ:", tinChi);
        addField("Giảng viên:", giangVien);
        addField("Ghi chú:", ghiChu);
    }

    @Override public JComponent component() { return this; }

    @Override
    public void fill(MonHoc m) {
        ten.setText(m.getTenMon());
        tinChi.setValue(Math.max(1, Math.min(10, m.getSoTinChi())));
        giangVien.setText(m.getGiangVien() == null ? "" : m.getGiangVien());
        ghiChu.setText(m.getGhiChu() == null ? "" : m.getGhiChu());
    }

    @Override
    public void clear() {
        ten.setText("");
        giangVien.setText("");
        ghiChu.setText("");
    }

    @Override
    public MonHoc read(Component parent) {
        String t = ten.getText().trim();
        if (t.isEmpty()) {
            UiUtil.warn(parent, "Thiếu dữ liệu", "Vui lòng nhập tên môn học.");
            return null;
        }
        int tc;
        try {
            tinChi.commitEdit();
            tc = (Integer) tinChi.getValue();
        } catch (ParseException e) {
            UiUtil.warn(parent, "Sai số tín chỉ", "Số tín chỉ phải là số nguyên từ 1 đến 10.");
            return null;
        }
        MonHoc result = new MonHoc(t, tc, giangVien.getText().trim(), ghiChu.getText().trim());
        result.validate();
        return result;
    }
}
