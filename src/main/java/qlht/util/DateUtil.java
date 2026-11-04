package qlht.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/**
 * Xử lý ngày tháng. Trong CSDL ngày lưu dạng ISO (yyyy-MM-dd) để sắp xếp đúng thời gian;
 * trên giao diện hiển thị dạng dd/MM/yyyy.
 */
public final class DateUtil {
    private DateUtil() {}

    private static final DateTimeFormatter VN_IN =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter VN_OUT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern ISO = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    /** '05/01/2026' -> LocalDate. Sai định dạng hoặc ngày không tồn tại thì trả về null. */
    public static LocalDate parseVn(String s) {
        if (s == null) return null;
        try {
            return LocalDate.parse(s.trim(), VN_IN);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String formatVn(LocalDate d) {
        return d == null ? "" : d.format(VN_OUT);
    }

    /** Đọc ngày từ CSDL (ISO, hoặc dd/MM/yyyy kiểu cũ). Không đọc được thì trả về null. */
    public static LocalDate fromDb(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            return parseVn(s);
        }
    }

    public static String toDb(LocalDate d) {
        return d == null ? null : d.toString();
    }

    /** Dùng khi chuyển dữ liệu cũ: trả về chuỗi ISO nếu s là dd/MM/yyyy, ngược lại null (không cần đổi). */
    public static String legacyToIso(String s) {
        if (s == null || s.isBlank() || ISO.matcher(s).matches()) return null;
        LocalDate d = parseVn(s);
        return d == null ? null : d.toString();
    }

    /** LocalDate -> 'Thứ 2' ... 'Chủ nhật'. */
    public static String thuCuaNgay(LocalDate d) {
        return Const.DAYS.get(d.getDayOfWeek().getValue() - 1);
    }
}
