package qlht.model;

import qlht.util.TimeUtil;

public class LichHoc extends Entity {
    private final int monHocId;
    private final String tenMon; // chỉ có khi đọc từ CSDL (JOIN), có thể null
    private final String thu;
    private final int tietBd;
    private final int tietKt;
    private final String phong;

    public LichHoc(int monHocId, String tenMon, String thu, int tietBd, int tietKt, String phong) {
        this.monHocId = monHocId;
        this.tenMon = tenMon;
        this.thu = thu;
        this.tietBd = tietBd;
        this.tietKt = tietKt;
        this.phong = phong;
    }

    public int getMonHocId() { return monHocId; }
    public String getTenMon() { return tenMon; }
    public String getThu() { return thu; }
    public int getTietBd() { return tietBd; }
    public int getTietKt() { return tietKt; }
    public String getPhong() { return phong; }

    /** Hai khoảng tiết giao nhau trong cùng một thứ. */
    public boolean trungVoi(LichHoc other) {
        return thu.equals(other.thu) && tietBd <= other.tietKt && other.tietBd <= tietKt;
    }

    /** Dùng trong hộp thoại cảnh báo. */
    public String moTa() {
        String p = (phong == null || phong.isBlank()) ? "—" : phong;
        return "  • " + tenMon + " (" + thu + ", tiết " + tietBd + "-" + tietKt + ", "
                + TimeUtil.moTaGioTiet(tietBd, tietKt) + ", phòng " + p + ")";
    }
}
