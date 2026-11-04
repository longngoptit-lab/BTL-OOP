package qlht.ui;

import java.awt.Component;
import java.util.List;
import javax.swing.JComponent;
import qlht.model.MonHoc;

/**
 * Một form nhập liệu cho một loại đối tượng. Cùng một form được dùng cho cả
 * việc thêm mới (nhúng trong tab) và chỉnh sửa (đặt trong hộp thoại).
 */
public interface EntityForm<T> {
    JComponent component();

    /** Đổ dữ liệu của đối tượng vào các ô nhập. */
    void fill(T entity);

    /** Xóa trắng / đặt lại các ô sau khi thêm. */
    void clear();

    /** Đọc dữ liệu: lỗi nhập liệu trả null; vi phạm quy tắc model có thể ném ValidationException. */
    T read(Component parent);

    /** Nạp lại danh sách môn học cho các ô chọn môn (mặc định: form không có ô chọn môn). */
    default void reloadMon(List<MonHoc> list) { }
}
