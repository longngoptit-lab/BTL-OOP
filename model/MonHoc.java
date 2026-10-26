package qlht.model;

public class MonHoc extends Entity {
    private final String tenMon;
    private final int soTinChi;
    private final String giangVien;
    private final String ghiChu;

    public MonHoc(String tenMon, int soTinChi, String giangVien, String ghiChu) {
        this.tenMon = tenMon;
        this.soTinChi = soTinChi;
        this.giangVien = giangVien;
        this.ghiChu = ghiChu;
    }

    public String getTenMon() { return tenMon; }
    public int getSoTinChi() { return soTinChi; }
    public String getGiangVien() { return giangVien; }
    public String getGhiChu() { return ghiChu; }

    @Override
    public String toString() { return getId() + " - " + tenMon; }
}
