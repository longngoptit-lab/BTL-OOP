package qlht.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JButton;
import qlht.model.LichHoc;
import qlht.model.MonHoc;

public class ThoiKhoaBieuPanel extends TabPanel {
    private static final long serialVersionUID = 1L;
    private final LichHocForm form = new LichHocForm(2);
    private final RowTable table = new RowTable(
            new String[]{"ID", "Môn học", "Thứ", "Tiết BĐ", "Tiết KT", "Phòng"},
            new int[]{40, 220, 90, 70, 70, 150});

    public ThoiKhoaBieuPanel(AppContext ctx) {
        super(ctx);
        JButton btnThem = new JButton("Thêm vào TKB");
        btnThem.addActionListener(e -> UiUtil.safe(this, this::them));
        add(addBox("Thêm lịch học", form, btnThem), BorderLayout.NORTH);

        add(tableBox("Thời khóa biểu trong tuần (dòng đỏ: trùng lịch học; nhấp đúp để sửa)", table),
                BorderLayout.CENTER);

        JButton btnSua = new JButton("Sửa lịch học đã chọn");
        JButton btnXoa = new JButton("Xóa lịch học đã chọn");
        btnSua.addActionListener(e -> UiUtil.safe(this, this::sua));
        btnXoa.addActionListener(e -> UiUtil.safe(this, this::xoa));
        add(buttonsRight(btnSua, btnXoa), BorderLayout.SOUTH);
        table.onDoubleClick(() -> UiUtil.safe(this, this::sua));
    }

    @Override
    public void refresh() {
        List<MonHoc> mon = ctx.monHocDao.layTatCa();
        form.reloadMon(mon);
        Set<Integer> xungDot = ctx.conflicts.idLichHocXungDot();
        List<RowTable.Row> rows = new ArrayList<>();
        for (LichHoc l : ctx.lichHocDao.layThoiKhoaBieu()) {
            rows.add(new RowTable.Row(l.getId(),
                    xungDot.contains(l.getId()) ? RowTable.Style.CONFLICT : RowTable.Style.NONE,
                    l.getId(), l.getTenMon(), l.getThu(), l.getTietBd(), l.getTietKt(), l.getPhong()));
        }
        table.setRows(rows);
    }

    /** Cảnh báo nếu trùng lịch học khác. Trả về true nếu người dùng vẫn muốn lưu. */
    private boolean xacNhan(LichHoc l, Component parent) {
        List<LichHoc> trung = ctx.conflicts.trungLichHoc(
                l.getThu(), l.getTietBd(), l.getTietKt(), l.isNew() ? null : l.getId());
        if (trung.isEmpty()) return true;
        String ds = trung.stream().map(LichHoc::moTa).collect(Collectors.joining("\n"));
        return UiUtil.confirmWarn(parent, "⚠ Cảnh báo trùng lịch học",
                "Lịch học (" + l.getThu() + ", tiết " + l.getTietBd() + "-" + l.getTietKt()
                        + ") bị TRÙNG với:\n" + ds + "\n\nBạn vẫn muốn lưu?");
    }

    private boolean luu(LichHoc l, Component parent) {
        if (!xacNhan(l, parent)) return false;
        if (l.isNew()) ctx.lichHocDao.them(l);
        else ctx.lichHocDao.sua(l);
        return true;
    }

    private void them() {
        LichHoc l = form.read(this);
        if (l == null || !luu(l, this)) return;
        form.clear();
        ctx.refreshAll();
    }

    private void sua() {
        Integer id = table.selectedId();
        if (id == null) {
            UiUtil.warn(this, "Chưa chọn", "Vui lòng chọn lịch học cần sửa.");
            return;
        }
        LichHoc lh = ctx.lichHocDao.layTheoId(id);
        if (lh == null) {
            UiUtil.error(this, "Lỗi", "Không tìm thấy lịch học này.");
            ctx.refreshAll();
            return;
        }
        LichHocForm f = new LichHocForm(1);
        f.reloadMon(ctx.monHocDao.layTatCa());
        f.fill(lh);
        new EditDialog<>(window(), "Sửa lịch học", f, (l, dlg) -> {
            l.setId(id);
            if (!luu(l, dlg)) return false;
            ctx.refreshAll();
            UiUtil.info(dlg, "Thành công", "Đã cập nhật lịch học.");
            return true;
        }).setVisible(true);
    }

    private void xoa() {
        Integer id = table.selectedId();
        if (id == null) {
            UiUtil.warn(this, "Chưa chọn", "Vui lòng chọn dòng cần xóa.");
            return;
        }
        LichHoc l = ctx.lichHocDao.layTheoId(id);
        if (l == null) {
            ctx.refreshAll();
            return;
        }
        if (UiUtil.confirm(this, "Xác nhận", "Xóa lịch học '" + l.getTenMon() + "' ("
                + l.getThu() + ", tiết " + l.getTietBd() + "-" + l.getTietKt() + ")?")) {
            ctx.lichHocDao.xoa(id);
            ctx.refreshAll();
        }
    }
}
