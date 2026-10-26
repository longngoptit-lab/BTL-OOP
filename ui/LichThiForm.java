package qlht.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import qlht.model.LichThi;
import qlht.model.MonHoc;
import qlht.util.Const;
import qlht.util.TimeUtil;

public class LichThiForm extends FormPanel implements EntityForm<LichThi> {
    private static final long serialVersionUID = 1L;
    private final MonCombo mon = new MonCombo(false);
    private final DatePicker ngay = new DatePicker();
    private final TimePicker gio = new TimePicker();
    private final JTextField phong = new JTextField(14);
    private final JComboBox<String> hinhThuc = new JComboBox<>(Const.HINH_THUC_THI.toArray(new String[0]));
    private final JLabel gioCanhBao = new JLabel(" ");

    public LichThiForm(int pairs) {
        super(pairs);
        hinhThuc.setEditable(true);
        hinhThuc.setSelectedItem("");
        gioCanhBao.setForeground(new Color(0xe65100));
        addField("Môn học:", mon);
        addField("Ngày thi:", ngay);
        addField("Giờ thi (giờ : phút):", gio);
        addField("Phòng thi:", phong);
        addField("Hình thức:", hinhThuc);
        addWide("", gioCanhBao);
    }

    @Override public JComponent component() { return this; }

    @Override public void reloadMon(List<MonHoc> list) { mon.reload(list); }

    @Override
    public void fill(LichThi l) {
        mon.setMonId(l.getMonHocId());
        ngay.setDate(l.getNgayThi());
        gio.setTime(l.getGioThi());
        String g = l.getGioThi();
        if (g != null && !g.isBlank() && TimeUtil.chuanHoaGio(g) == null) {
            gioCanhBao.setText("Giờ cũ “" + g + "” không đọc được, đã đặt tạm "
                    + Const.GIO_MAC_DINH + ". Hãy chọn lại.");
        } else {
            gioCanhBao.setText(" ");
        }
        phong.setText(l.getPhong() == null ? "" : l.getPhong());
        hinhThuc.setSelectedItem(l.getHinhThuc() == null ? "" : l.getHinhThuc());
    }

    @Override
    public void clear() {
        ngay.setDate(null);
        gio.setTime(Const.GIO_MAC_DINH);
        phong.setText("");
        gioCanhBao.setText(" ");
    }

    @Override
    public LichThi read(Component parent) {
        Integer monId = mon.getMonId();
        if (monId == null) {
            UiUtil.warn(parent, "Thiếu dữ liệu", "Vui lòng chọn môn học và ngày thi.");
            return null;
        }
        if (ngay.getDate() == null) {
            UiUtil.warn(parent, "Sai định dạng", "Ngày thi phải theo dạng dd/mm/yyyy.");
            return null;
        }
        String gioChon = gio.getTime();
        if (gioChon == null) {
            UiUtil.warn(parent, "Sai giờ thi",
                    "Giờ thi không hợp lệ.\nHãy chọn giờ từ 00 đến 23 và phút từ 00 đến 59.");
            return null;
        }
        String ht = String.valueOf(hinhThuc.getEditor().getItem()).trim();
        return new LichThi(monId, null, ngay.getDate(), gioChon, phong.getText().trim(), ht);
    }
}
