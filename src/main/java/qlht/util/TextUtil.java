package qlht.util;

import java.text.Normalizer;
import java.util.Locale;

public final class TextUtil {
    private TextUtil() {}

    /** Bỏ dấu tiếng Việt và chuyển về chữ thường để so khớp khi tìm kiếm. */
    public static String boDau(String text) {
        String t = Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFD)
                .replaceAll("\\p{Mn}", "");
        return t.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    /** Chuẩn hóa tên để so sánh trùng: bỏ dấu, chữ thường, gộp khoảng trắng. */
    public static String chuanHoaTen(String text) {
        return String.join(" ", boDau(text).trim().split("\\s+"));
    }
}
