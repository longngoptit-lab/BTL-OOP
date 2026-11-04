package qlht.ui;

import java.awt.FlowLayout;
import java.text.ParseException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import qlht.util.Const;
import qlht.util.TimeUtil;

/** Ô chọn giờ gồm hai hộp số: giờ (00-23) và phút (00-59). */
public class TimePicker extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JSpinner gio = new JSpinner(new SpinnerNumberModel(7, 0, 23, 1));
    private final JSpinner phut = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));

    public TimePicker() {
        super(new FlowLayout(FlowLayout.LEFT, 2, 0));
        setOpaque(false);
        gio.setEditor(new JSpinner.NumberEditor(gio, "00"));
        phut.setEditor(new JSpinner.NumberEditor(phut, "00"));
        add(gio);
        add(new JLabel(":"));
        add(phut);
    }

    /** Đặt giờ từ chuỗi như "07:30" hoặc "7h30". Không đọc được thì dùng giờ mặc định. */
    public void setTime(String text) {
        String t = TimeUtil.chuanHoaGio(text);
        if (t == null) t = Const.GIO_MAC_DINH;
        gio.setValue(Integer.parseInt(t.substring(0, 2)));
        phut.setValue(Integer.parseInt(t.substring(3)));
    }

    /** "HH:mm", hoặc null nếu người dùng gõ giá trị không hợp lệ. */
    public String getTime() {
        try {
            gio.commitEdit();
            phut.commitEdit();
        } catch (ParseException e) {
            return null;
        }
        return String.format("%02d:%02d", (Integer) gio.getValue(), (Integer) phut.getValue());
    }
}
