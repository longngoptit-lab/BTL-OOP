package qlht.ui;

import java.awt.Component;
import java.text.ParseException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import qlht.model.LichHoc;
import qlht.model.MonHoc;
import qlht.util.Const;

public class LichHocForm extends FormPanel implements EntityForm<LichHoc> {
    private static final long serialVersionUID = 1L;
    private final MonCombo mon = new MonCombo(false);
    private final JComboBox<String> thu = new JComboBox<>(Const.DAYS.toArray(new String[0]));
    private final JSpinner tietBd = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
    private final JSpinner tietKt = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
    private final JTextField phong = new JTextField(22);

    public LichHocForm(int pairs) {
        super(pairs);
        thu.setSelectedIndex(-1);
        addField("Môn học:", mon);
        addField("Thứ:", thu);
        addField("Tiết bắt đầu:", tietBd);
        addField("Tiết kết thúc:", tietKt);
        addField("Phòng học:", phong);
    }

    @Override public JComponent component() { return this; }

    @Override public void reloadMon(List<MonHoc> list) { mon.reload(list); }

    @Override
    public void fill(LichHoc l) {
        mon.setMonId(l.getMonHocId());
        thu.setSelectedItem(l.getThu());
        tietBd.setValue(l.getTietBd());
        tietKt.setValue(l.getTietKt());
        phong.setText(l.getPhong() == null ? "" : l.getPhong());
    }

    @Override
    public void clear() {
        phong.setText("");
    }

    @Override
    public LichHoc read(Component parent) {
        Integer monId = mon.getMonId();
        String thuChon = (String) thu.getSelectedItem();
        if (monId == null || thuChon == null) {
            UiUtil.warn(parent, "Thiếu dữ liệu", "Vui lòng chọn môn học và thứ.");
            return null;
        }
        int bd, kt;
        try {
            tietBd.commitEdit();
            tietKt.commitEdit();
            bd = (Integer) tietBd.getValue();
            kt = (Integer) tietKt.getValue();
        } catch (ParseException e) {
            UiUtil.warn(parent, "Lỗi", "Tiết học phải là số trong khoảng 1-12.");
            return null;
        }
        if (kt < bd) {
            UiUtil.warn(parent, "Lỗi", "Tiết kết thúc phải >= tiết bắt đầu.");
            return null;
        }
        return new LichHoc(monId, null, thuChon, bd, kt, phong.getText().trim());
    }
}
