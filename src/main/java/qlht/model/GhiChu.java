package qlht.model;

import java.time.LocalDate;

public class GhiChu extends Entity {
    private final Integer monHocId; // null = không gắn môn
    private final String tenMon;
    private final String tieuDe;
    private final String noiDung;
    private final LocalDate ngayTao;

    public GhiChu(Integer monHocId, String tenMon, String tieuDe, String noiDung, LocalDate ngayTao) {
        this.monHocId = monHocId;
        this.tenMon = tenMon;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.ngayTao = ngayTao;
    }

    @Override
    public void validate() {
        Validation.subject(monHocId, false);
        Validation.required(tieuDe, 255, "tiêu đề ghi chú");
    }

    public Integer getMonHocId() { return monHocId; }
    public String getTenMon() { return tenMon; }
    public String getTieuDe() { return tieuDe; }
    public String getNoiDung() { return noiDung; }
    public LocalDate getNgayTao() { return ngayTao; }
}
