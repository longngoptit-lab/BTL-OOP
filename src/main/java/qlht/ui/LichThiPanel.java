package qlht.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JButton;
import qlht.model.LichHoc;
import qlht.model.LichThi;
import qlht.service.ConflictService;
import qlht.util.DateUtil;

public class LichThiPanel extends TabPanel {
    private static final long serialVersionUID = 1L;
    private final LichThiForm form = new LichThiForm(2);
    private final RowTable table = new RowTable(
            new String[]{"ID", "Môn học", "Ngày thi", "Giờ thi", "Phòng thi", "Hình thức"},
            new int[]{40, 200, 100, 100, 100, 120});

    public LichThiPanel(AppContext ctx) {
        super(ctx);
        JButton btnThem = new JButton("Thêm lịch thi");
        btnThem.addActionListener(e -> UiUtil.safe(this, this::them));
        add(addBox("Thêm lịch thi", form, btnThem), BorderLayout.NORTH);

        add(tableBox("Danh sách lịch thi (đỏ: trùng lịch thi hoặc trùng giờ học; "
                + "cam: cùng ngày có lịch học, chưa rõ giờ thi; nhấp đúp để sửa)", table), BorderLayout.CENTER);

        JButton btnSua = new JButton("Chỉnh sửa");
        JButton btnXoa = AppTheme.danger(new JButton("Xóa"));
        btnSua.addActionListener(e -> UiUtil.safe(this, this::sua));
        btnXoa.addActionListener(e -> UiUtil.safe(this, this::xoa));
        add(buttonsRight(btnSua, btnXoa), BorderLayout.SOUTH);
        table.onDoubleClick(() -> UiUtil.safe(this, this::sua));
    }

    @Override
    public void refresh() {
        form.reloadMon(ctx.monHocDao.layTatCa());
        ConflictService.XungDotThi xd = ctx.conflicts.idLichThiXungDot();
        List<RowTable.Row> rows = new ArrayList<>();
        for (LichThi l : ctx.lichThiDao.layTatCa()) {
            RowTable.Style s = xd.do_().contains(l.getId()) ? RowTable.Style.CONFLICT
                    : xd.cam().contains(l.getId()) ? RowTable.Style.WARN : RowTable.Style.NONE;
            rows.add(new RowTable.Row(l.getId(), s, l.getId(), l.getTenMon(),
                    DateUtil.formatVn(l.getNgayThi()), l.getGioThi(), l.getPhong(), l.getHinhThuc()));
        }
        table.setRows(rows);
    }

    /** Cảnh báo nếu lịch thi trùng lịch thi / lịch học khác. True nếu người dùng vẫn muốn lưu. */
    private boolean xacNhan(LichThi lt, Component parent) {
        Integer boQua = lt.isNew() ? null : lt.getId();
        List<String> canhBao = new ArrayList<>();

        List<LichThi> trungThi = ctx.conflicts.trungLichThi(lt.getNgayThi(), lt.getGioThi(), boQua);
        if (!trungThi.isEmpty()) {
            canhBao.add("Trùng lịch THI cùng ngày với:\n"
                    + trungThi.stream().map(LichThi::moTa).collect(Collectors.joining("\n")));
        }
        ConflictService.TrungHoc th =
                ctx.conflicts.trungLichHocVoiThi(lt.getNgayThi(), lt.getGioThi(), lt.getMonHocId());
        if (!th.danhSach().isEmpty()) {
            String ds = th.danhSach().stream().map(LichHoc::moTa).collect(Collectors.joining("\n"));
            if (th.chinhXac()) {
                canhBao.add("Giờ thi " + lt.getGioThi() + " TRÙNG GIỜ HỌC của môn khác:\n" + ds);
            } else {
                canhBao.add("Ngày thi " + DateUtil.formatVn(lt.getNgayThi()) + " có LỊCH HỌC của môn khác "
                        + "(chưa đọc được giờ thi nên chưa so sánh chính xác):\n" + ds);
            }
        }
        if (canhBao.isEmpty()) return true;
        return UiUtil.confirmWarn(parent, "⚠ Cảnh báo trùng lịch",
                String.join("\n\n", canhBao) + "\n\nBạn vẫn muốn lưu lịch thi này?");
    }

    private boolean luu(LichThi lt, Component parent) {
        if (!xacNhan(lt, parent)) return false;
        if (lt.isNew()) ctx.lichThiDao.them(lt);
        else ctx.lichThiDao.sua(lt);
        return true;
    }

    private void them() {
        LichThi lt = form.read(this);
        if (lt == null || !luu(lt, this)) return;
        form.clear();
        ctx.refreshAll();
    }

    private void sua() {
        Integer id = table.selectedId();
        if (id == null) {
            UiUtil.warn(this, "Chưa chọn", "Vui lòng chọn lịch thi cần sửa.");
            return;
        }
        LichThi cu = ctx.lichThiDao.layTheoId(id);
        if (cu == null) {
            UiUtil.error(this, "Lỗi", "Không tìm thấy lịch thi này.");
            ctx.refreshAll();
            return;
        }
        LichThiForm f = new LichThiForm(1);
        f.reloadMon(ctx.monHocDao.layTatCa());
        f.fill(cu);
        new EditDialog<>(window(), "Sửa lịch thi", f, (lt, dlg) -> {
            lt.setId(id);
            if (!luu(lt, dlg)) return false;
            ctx.refreshAll();
            UiUtil.info(dlg, "Thành công", "Đã cập nhật lịch thi.");
            return true;
        }).setVisible(true);
    }

    private void xoa() {
        Integer id = table.selectedId();
        if (id == null) {
            UiUtil.warn(this, "Chưa chọn", "Vui lòng chọn dòng cần xóa.");
            return;
        }
        LichThi l = ctx.lichThiDao.layTheoId(id);
        if (l == null) {
            ctx.refreshAll();
            return;
        }
        if (UiUtil.confirm(this, "Xác nhận", "Xóa lịch thi môn '" + l.getTenMon()
                + "' ngày " + DateUtil.formatVn(l.getNgayThi()) + "?")) {
            ctx.lichThiDao.xoa(id);
            ctx.refreshAll();
        }
    }
}
