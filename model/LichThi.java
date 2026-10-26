package qlht.model;

import java.time.LocalDate;
import qlht.util.DateUtil;

public class LichThi extends Entity {
    private final int monHocId;
    private final String tenMon;
    private final LocalDate ngayThi;
    private final String gioThi; // "HH:mm" (dữ liệu cũ có thể là chuỗi tự do)
    private final String phong;
    private final String hinhThuc;

    public LichThi(int monHocId, String tenMon, LocalDate ngayThi, String gioThi,
                   String phong, String hinhThuc) {
        this.monHocId = monHocId;
        this.tenMon = tenMon;
        this.ngayThi = ngayThi;
        this.gioThi = gioThi == null ? "" : gioThi;
        this.phong = phong;
        this.hinhThuc = hinhThuc;
    }

    public int getMonHocId() { return monHocId; }
    public String getTenMon() { return tenMon; }
    public LocalDate getNgayThi() { return ngayThi; }
    public String getGioThi() { return gioThi; }
    public String getPhong() { return phong; }
    public String getHinhThuc() { return hinhThuc; }

    public String moTa() {
        String g = gioThi.isBlank() ? "chưa rõ" : gioThi;
        String p = (phong == null || phong.isBlank()) ? "—" : phong;
        return "  • " + tenMon + " (ngày " + DateUtil.formatVn(ngayThi) + ", giờ " + g + ", phòng " + p + ")";
    }
}
