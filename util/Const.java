package qlht.util;

import java.util.List;

/** Các hằng số dùng chung trong toàn ứng dụng. */
public final class Const {
    private Const() {}

    public static final List<String> DAYS =
            List.of("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật");
    public static final List<String> PRIORITIES = List.of("Thấp", "Trung bình", "Cao", "Khẩn cấp");
    public static final List<String> STATUSES = List.of("Chưa hoàn thành", "Đang làm", "Hoàn thành");
    public static final String STATUS_TODO = STATUSES.get(0);
    public static final String STATUS_DONE = STATUSES.get(2);
    public static final List<String> HINH_THUC_THI =
            List.of("Tự luận", "Trắc nghiệm", "Vấn đáp", "Thực hành", "Tiểu luận");

    public static final String GIO_MAC_DINH = "07:00";
    /** Hai lịch thi cùng ngày có giờ bắt đầu cách nhau dưới số phút này được xem là trùng. */
    public static final int SO_PHUT_THI_TOI_THIEU = 90;
}
