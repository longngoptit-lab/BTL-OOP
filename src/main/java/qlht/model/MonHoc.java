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

    @Override
    public void validate() {
        Validation.required(tenMon, 255, "tên môn học");
        if (soTinChi < 1 || soTinChi > 10) throw new ValidationException("Số tín chỉ phải từ 1 đến 10.");
        Validation.optional(giangVien, 255, "Tên giảng viên");
    }

    public String getTenMon() { return tenMon; }
    public int getSoTinChi() { return soTinChi; }
    public String getGiangVien() { return giangVien; }
    public String getGhiChu() { return ghiChu; }

    @Override
    public String toString() { return getId() + " - " + tenMon; }
}
