package qlht.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import qlht.model.BaiTap;
import qlht.util.Const;
import qlht.util.DateUtil;

public class BaiTapPanel extends TabPanel {
    private static final long serialVersionUID = 1L;
    private final BaiTapForm form = new BaiTapForm(2, false);
    private final RowTable table = new RowTable(
            new String[]{"ID", "Môn học", "Tiêu đề", "Hạn nộp", "Ưu tiên", "Trạng thái"},
            new int[]{40, 160, 220, 100, 90, 120});
    private final JComboBox<String> trangThaiNhanh = new JComboBox<>(Const.STATUSES.toArray(new String[0]));

    public BaiTapPanel(AppContext ctx) {
        super(ctx);
        trangThaiNhanh.setSelectedIndex(-1);

        JButton btnThem = new JButton("Thêm bài tập");
        btnThem.addActionListener(e -> UiUtil.safe(this, this::them));
        add(addBox("Thêm bài tập / deadline", form, btnThem), BorderLayout.NORTH);

        add(tableBox("Danh sách bài tập (xanh: xong; cam: còn ≤ 3 ngày; đỏ: quá hạn; nhấp đúp để sửa)", table),
                BorderLayout.CENTER);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        left.add(new JLabel("Đổi trạng thái nhanh:"));
        left.add(trangThaiNhanh);
        JButton btnCapNhat = new JButton("Cập nhật");
        btnCapNhat.addActionListener(e -> UiUtil.safe(this, this::capNhatTrangThai));
        left.add(btnCapNhat);

        JButton btnSua = new JButton("Sửa bài tập đã chọn");
        JButton btnXoa = new JButton("Xóa bài tập đã chọn");
        btnSua.addActionListener(e -> UiUtil.safe(this, this::sua));
        btnXoa.addActionListener(e -> UiUtil.safe(this, this::xoa));

        JPanel south = new JPanel(new BorderLayout());
        south.add(left, BorderLayout.WEST);
        south.add(buttonsRight(btnSua, btnXoa), BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);
        table.onDoubleClick(() -> UiUtil.safe(this, this::sua));
    }

    @Override
    public void refresh() {
        form.reloadMon(ctx.monHocDao.layTatCa());
        LocalDate today = LocalDate.now();
        List<RowTable.Row> rows = new ArrayList<>();
        for (BaiTap b : ctx.baiTapDao.layTatCa(false)) {
            RowTable.Style s = RowTable.Style.NONE;
            if (b.getHanNop() != null) {
                long conLai = ChronoUnit.DAYS.between(today, b.getHanNop());
                if (Const.STATUS_DONE.equals(b.getTrangThai())) s = RowTable.Style.DONE;
                else if (conLai < 0) s = RowTable.Style.OVERDUE;
                else if (conLai <= 3) s = RowTable.Style.URGENT;
            }
            rows.add(new RowTable.Row(b.getId(), s, b.getId(),
                    b.getTenMon() == null ? "(Không gắn môn)" : b.getTenMon(),
                    b.getTieuDe(), DateUtil.formatVn(b.getHanNop()), b.getMucDoUuTien(), b.getTrangThai()));
        }
        table.setRows(rows);
    }

    private void them() {
        BaiTap b = form.read(this);
        if (b == null) return;
        ctx.baiTapDao.them(b);
        form.clear();
        ctx.refreshAll();
    }

    private void sua() {
        Integer id = table.selectedId();
        if (id == null) {
            UiUtil.warn(this, "Chưa chọn", "Vui lòng chọn bài tập cần sửa.");
            return;
        }
        BaiTap cu = ctx.baiTapDao.layTheoId(id);
        if (cu == null) {
            UiUtil.error(this, "Lỗi", "Không tìm thấy bài tập này.");
            ctx.refreshAll();
            return;
        }
        BaiTapForm f = new BaiTapForm(1, true);
        f.reloadMon(ctx.monHocDao.layTatCa());
        f.fill(cu);
        new EditDialog<>(window(), "Sửa bài tập / deadline", f, (b, dlg) -> {
            b.setId(id);
            ctx.baiTapDao.sua(b);
            ctx.refreshAll();
            UiUtil.info(dlg, "Thành công", "Đã cập nhật bài tập.");
            return true;
        }).setVisible(true);
    }

    private void capNhatTrangThai() {
        String tt = (String) trangThaiNhanh.getSelectedItem();
        Integer id = table.selectedId();
        if (id == null || tt == null) {
            UiUtil.warn(this, "Chưa chọn", "Chọn bài tập và trạng thái cần cập nhật.");
            return;
        }
        ctx.baiTapDao.capNhatTrangThai(id, tt);
        ctx.refreshAll();
    }

    private void xoa() {
        Integer id = table.selectedId();
        if (id == null) {
            UiUtil.warn(this, "Chưa chọn", "Vui lòng chọn bài tập cần xóa.");
            return;
        }
        BaiTap b = ctx.baiTapDao.layTheoId(id);
        String ten = b == null ? "" : b.getTieuDe();
        if (UiUtil.confirm(this, "Xác nhận", "Xóa bài tập '" + ten + "'?")) {
            ctx.baiTapDao.xoa(id);
            ctx.refreshAll();
        }
    }
}
