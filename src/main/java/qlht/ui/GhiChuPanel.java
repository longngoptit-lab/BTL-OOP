package qlht.ui;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import qlht.model.GhiChu;
import qlht.util.DateUtil;

public class GhiChuPanel extends TabPanel {
    private static final long serialVersionUID = 1L;
    private final GhiChuForm form = new GhiChuForm(2, 3, 70);
    private final RowTable table = new RowTable(
            new String[]{"ID", "Môn học", "Tiêu đề", "Nội dung", "Ngày tạo"},
            new int[]{40, 150, 180, 280, 100});

    public GhiChuPanel(AppContext ctx) {
        super(ctx);
        JButton btnLuu = new JButton("Lưu ghi chú");
        btnLuu.addActionListener(e -> UiUtil.safe(this, this::them));
        add(addBox("Thêm ghi chú bài giảng", form, btnLuu), BorderLayout.NORTH);

        add(tableBox("Danh sách ghi chú (nhấp đúp để xem đầy đủ / sửa)", table), BorderLayout.CENTER);

        JButton btnXem = new JButton("Chỉnh sửa");
        JButton btnXoa = AppTheme.danger(new JButton("Xóa"));
        btnXem.addActionListener(e -> UiUtil.safe(this, this::xemSua));
        btnXoa.addActionListener(e -> UiUtil.safe(this, this::xoa));
        add(buttonsRight(btnXem, btnXoa), BorderLayout.SOUTH);
        table.onDoubleClick(() -> UiUtil.safe(this, this::xemSua));
    }

    @Override
    public void refresh() {
        form.reloadMon(ctx.monHocDao.layTatCa());
        List<RowTable.Row> rows = new ArrayList<>();
        for (GhiChu g : ctx.ghiChuDao.layTatCa()) {
            String gon = g.getNoiDung() == null ? "" : g.getNoiDung().trim().replaceAll("\\s+", " ");
            if (gon.length() > 60) gon = gon.substring(0, 60) + "...";
            rows.add(new RowTable.Row(g.getId(), RowTable.Style.NONE, g.getId(),
                    g.getTenMon() == null ? "(Không gắn môn)" : g.getTenMon(),
                    g.getTieuDe(), gon, DateUtil.formatVn(g.getNgayTao())));
        }
        table.setRows(rows);
    }

    private void them() {
        GhiChu g = form.read(this);
        if (g == null) return;
        ctx.ghiChuDao.them(new GhiChu(g.getMonHocId(), null, g.getTieuDe(), g.getNoiDung(), LocalDate.now()));
        form.clear();
        ctx.refreshAll();
    }

    private void xemSua() {
        Integer id = table.selectedId();
        if (id == null) {
            UiUtil.warn(this, "Chưa chọn", "Vui lòng chọn ghi chú cần xem.");
            return;
        }
        GhiChu cu = ctx.ghiChuDao.layTheoId(id);
        if (cu == null) {
            UiUtil.error(this, "Lỗi", "Không tìm thấy ghi chú này.");
            ctx.refreshAll();
            return;
        }
        GhiChuForm f = new GhiChuForm(1, 14, 60);
        f.reloadMon(ctx.monHocDao.layTatCa());
        f.fill(cu);
        new EditDialog<>(window(), "Ghi chú", f, (g, dlg) -> {
            g.setId(id);
            ctx.ghiChuDao.sua(g);
            ctx.refreshAll();
            return true;
        }).setVisible(true);
    }

    private void xoa() {
        Integer id = table.selectedId();
        if (id == null) {
            UiUtil.warn(this, "Chưa chọn", "Vui lòng chọn ghi chú cần xóa.");
            return;
        }
        GhiChu g = ctx.ghiChuDao.layTheoId(id);
        String ten = g == null ? "" : g.getTieuDe();
        if (UiUtil.confirm(this, "Xác nhận", "Xóa ghi chú '" + ten + "'?")) {
            ctx.ghiChuDao.xoa(id);
            ctx.refreshAll();
        }
    }
}
