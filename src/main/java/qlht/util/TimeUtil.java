package qlht.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Giờ học theo tiết và xử lý giờ thi (tính bằng phút kể từ 0h). */
public final class TimeUtil {
    private TimeUtil() {}

    /** tiết -> {phút bắt đầu, phút kết thúc} */
    private static final Map<Integer, int[]> TIET_GIO = new HashMap<>();

    static {
        int[] gioBatDau = {7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19};
        for (int i = 0; i < gioBatDau.length; i++) {
            int batDau = gioBatDau[i] * 60;
            TIET_GIO.put(i + 1, new int[]{batDau, batDau + 50});
        }
    }

    /** {phút bắt đầu, phút kết thúc} của khoảng tiết. Tiết ngoài bảng giờ thì trả về null. */
    public static int[] khoangGioTiet(int tietBd, int tietKt) {
        int[] a = TIET_GIO.get(tietBd);
        int[] b = TIET_GIO.get(tietKt);
        return (a == null || b == null) ? null : new int[]{a[0], b[1]};
    }

    private static String phutRaGio(int phut) {
        return (phut / 60) + "h" + String.format("%02d", phut % 60);
    }

    /** Ví dụ (1, 3) -> "7h00-9h50". */
    public static String moTaGioTiet(int tietBd, int tietKt) {
        int[] k = khoangGioTiet(tietBd, tietKt);
        return k == null ? "chưa rõ giờ" : phutRaGio(k[0]) + "-" + phutRaGio(k[1]);
    }

    /** Bài thi (giả định kéo dài SO_PHUT_THI_TOI_THIEU phút) có chồng lên giờ học không. */
    public static boolean hocTrungGioThi(int phutBatDauThi, int tietBd, int tietKt) {
        int[] k = khoangGioTiet(tietBd, tietKt);
        if (k == null) return true;
        return phutBatDauThi < k[1] && k[0] < phutBatDauThi + Const.SO_PHUT_THI_TOI_THIEU;
    }

    private static final Pattern RE_GIO = Pattern.compile("(\\d{1,2})\\s*[:hH]\\s*(\\d{2})?");

    /** '07:30', '7h30', '7h' -> số phút trong ngày. Không đọc được thì trả về null. */
    public static Integer gioRaPhut(String text) {
        if (text == null) return null;
        Matcher m = RE_GIO.matcher(text);
        if (!m.find()) return null;
        int gio = Integer.parseInt(m.group(1));
        int phut = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
        if (gio > 23 || phut > 59) return null;
        return gio * 60 + phut;
    }

    /** '7h30' -> '07:30'. Không đọc được thì trả về null. */
    public static String chuanHoaGio(String text) {
        Integer p = gioRaPhut(text);
        return p == null ? null : String.format("%02d:%02d", p / 60, p % 60);
    }

    /** Hai giờ thi coi là trùng nếu quá gần nhau hoặc không xác định được giờ. */
    public static boolean trungGioThi(String a, String b) {
        Integer x = gioRaPhut(a), y = gioRaPhut(b);
        if (x == null || y == null) return true;
        return Math.abs(x - y) < Const.SO_PHUT_THI_TOI_THIEU;
    }
}
