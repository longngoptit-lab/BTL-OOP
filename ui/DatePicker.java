package qlht.ui;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import qlht.util.DateUtil;

/**
 * Ô chọn ngày dd/MM/yyyy. Có thể gõ tay, hoặc bấm mũi tên lên/xuống để tăng giảm
 * phần ngày / tháng / năm tùy vị trí con trỏ.
 */
public class DatePicker extends JSpinner {
    private static final long serialVersionUID = 1L;

    public DatePicker() {
        super(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(this, "dd/MM/yyyy");
        editor.getTextField().setColumns(8);
        setEditor(editor);
        setDate(LocalDate.now());
    }

    /** Ngày đang hiển thị trong ô; null nếu người dùng gõ sai định dạng hoặc ngày không tồn tại. */
    public LocalDate getDate() {
        return DateUtil.parseVn(((JSpinner.DateEditor) getEditor()).getTextField().getText());
    }

    public void setDate(LocalDate d) {
        LocalDate v = d == null ? LocalDate.now() : d;
        setValue(Date.from(v.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
}
