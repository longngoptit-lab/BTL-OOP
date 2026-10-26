package qlht.model;

import java.time.LocalDate;

public class BaiTap extends Entity {
    private final Integer monHocId; // null = không gắn môn
    private final String tenMon;
    private final String tieuDe;
    private final String moTa;
    private final LocalDate hanNop;
    private final String mucDoUuTien;
    private final String trangThai;

    public BaiTap(Integer monHocId, String tenMon, String tieuDe, String moTa,
                  LocalDate hanNop, String mucDoUuTien, String trangThai) {
        this.monHocId = monHocId;
        this.tenMon = tenMon;
        this.tieuDe = tieuDe;
        this.moTa = moTa;
        this.hanNop = hanNop;
        this.mucDoUuTien = mucDoUuTien;
        this.trangThai = trangThai;
    }

    public Integer getMonHocId() { return monHocId; }
    public String getTenMon() { return tenMon; }
    public String getTieuDe() { return tieuDe; }
    public String getMoTa() { return moTa; }
    public LocalDate getHanNop() { return hanNop; }
    public String getMucDoUuTien() { return mucDoUuTien; }
    public String getTrangThai() { return trangThai; }
}
