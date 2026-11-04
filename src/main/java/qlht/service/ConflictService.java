package qlht.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import qlht.db.LichHocDao;
import qlht.db.LichThiDao;
import qlht.model.LichHoc;
import qlht.model.LichThi;
import qlht.util.DateUtil;
import qlht.util.TimeUtil;

/** Nghiệp vụ phát hiện trùng lịch học / lịch thi (tách khỏi giao diện và khỏi truy vấn SQL). */
public class ConflictService {
    private final LichHocDao lichHocDao;
    private final LichThiDao lichThiDao;

    public ConflictService(LichHocDao lichHocDao, LichThiDao lichThiDao) {
        this.lichHocDao = lichHocDao;
        this.lichThiDao = lichThiDao;
    }

    /**
     * Lịch học (môn khác) ảnh hưởng tới một lịch thi.
     * chinhXac=true : đọc được giờ thi, danh sách là các lịch học THẬT SỰ trùng giờ thi.
     * chinhXac=false: không đọc được giờ thi, danh sách là mọi lịch học của môn khác cùng thứ.
     */
    public record TrungHoc(List<LichHoc> danhSach, boolean chinhXac) { }

    /** Tập ID tô đỏ và tập ID tô cam trong bảng lịch thi. */
    public record XungDotThi(Set<Integer> do_, Set<Integer> cam) { }

    // ------------------------------------------------------------ lịch học
    public List<LichHoc> trungLichHoc(String thu, int tietBd, int tietKt, Integer boQuaId) {
        return lichHocDao.timXungDot(thu, tietBd, tietKt, boQuaId);
    }

    /** Tập ID các lịch học đang trùng với một lịch học khác. */
    public Set<Integer> idLichHocXungDot() {
        List<LichHoc> ds = lichHocDao.layThoiKhoaBieu();
        Set<Integer> ids = new HashSet<>();
        for (int i = 0; i < ds.size(); i++) {
            for (int j = i + 1; j < ds.size(); j++) {
                if (ds.get(i).trungVoi(ds.get(j))) {
                    ids.add(ds.get(i).getId());
                    ids.add(ds.get(j).getId());
                }
            }
        }
        return ids;
    }

    // ------------------------------------------------------------ lịch thi
    /** Các lịch thi cùng ngày có giờ thi trùng hoặc quá sát với giờ thi đưa vào. */
    public List<LichThi> trungLichThi(LocalDate ngay, String gio, Integer boQuaId) {
        if (ngay == null) return List.of();
        return lichThiDao.layTatCa().stream()
                .filter(l -> ngay.equals(l.getNgayThi())
                        && !Objects.equals(l.getId(), boQuaId)
                        && TimeUtil.trungGioThi(gio, l.getGioThi()))
                .toList();
    }

    private List<LichHoc> lichHocCungThuKhacMon(List<LichHoc> tkb, LocalDate ngay, Integer monHocId) {
        if (ngay == null) return List.of();
        String thu = DateUtil.thuCuaNgay(ngay);
        return tkb.stream()
                .filter(h -> h.getThu().equals(thu) && !Objects.equals(h.getMonHocId(), monHocId))
                .toList();
    }

    public TrungHoc trungLichHocVoiThi(LocalDate ngay, String gio, Integer monHocId) {
        List<LichHoc> cungNgay = lichHocCungThuKhacMon(lichHocDao.layThoiKhoaBieu(), ngay, monHocId);
        Integer batDau = TimeUtil.gioRaPhut(gio);
        if (batDau == null) return new TrungHoc(cungNgay, false);
        return new TrungHoc(cungNgay.stream()
                .filter(h -> TimeUtil.hocTrungGioThi(batDau, h.getTietBd(), h.getTietKt()))
                .toList(), true);
    }

    /**
     * Đỏ : lịch thi trùng lịch thi khác, hoặc giờ thi trùng giờ học của môn khác.
     * Cam: không đọc được giờ thi nhưng ngày thi có lịch học của môn khác.
     */
    public XungDotThi idLichThiXungDot() {
        List<LichThi> thi = lichThiDao.layTatCa();
        List<LichHoc> tkb = lichHocDao.layThoiKhoaBieu();
        Set<Integer> do_ = new HashSet<>(), cam = new HashSet<>();

        for (int i = 0; i < thi.size(); i++) {
            LichThi a = thi.get(i);
            for (int j = i + 1; j < thi.size(); j++) {
                LichThi b = thi.get(j);
                if (Objects.equals(a.getNgayThi(), b.getNgayThi())
                        && TimeUtil.trungGioThi(a.getGioThi(), b.getGioThi())) {
                    do_.add(a.getId());
                    do_.add(b.getId());
                }
            }
            List<LichHoc> cungNgay = lichHocCungThuKhacMon(tkb, a.getNgayThi(), a.getMonHocId());
            if (cungNgay.isEmpty()) continue;
            Integer batDau = TimeUtil.gioRaPhut(a.getGioThi());
            if (batDau == null) {
                cam.add(a.getId());
            } else if (cungNgay.stream().anyMatch(h -> TimeUtil.hocTrungGioThi(batDau, h.getTietBd(), h.getTietKt()))) {
                do_.add(a.getId());
            }
        }
        return new XungDotThi(do_, cam);
    }
}
