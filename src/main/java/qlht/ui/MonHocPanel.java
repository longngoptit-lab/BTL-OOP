package qlht.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import qlht.model.MonHoc;

public class MonHocPanel extends TabPanel {
    private static final long serialVersionUID = 1L;
    private final MonHocForm form = new MonHocForm(2);
    private final JTextField timKiem = new JTextField(24);
    private final RowTable table = new RowTable(
            new String[]{"ID", "Tên môn", "Tín chỉ", "Giảng viên", "Ghi chú"},
            new int[]{40, 220, 70, 180, 200});

    public MonHocPanel(AppContext ctx) {
        super(ctx);

        JButton btnThem = new JButton("Thêm môn học");
        btnThem.addActionListener(e -> UiUtil.safe(this, this::them));

        JPanel search = AppTheme.transparent(new FlowLayout(FlowLayout.LEFT, 8, 12));
        search.add(new JLabel("Tìm môn học"));
        timKiem.setToolTipText("Tìm theo tên, hỗ trợ tìm không dấu");
        search.add(timKiem);
        JButton btnXoaTim = new JButton("Xóa tìm kiếm");
        btnXoaTim.addActionListener(e -> timKiem.setText(""));
        search.add(btnXoaTim);
        timKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { UiUtil.safe(MonHocPanel.this, MonHocPanel.this::reloadTable); }
            @Override public void removeUpdate(DocumentEvent e) { UiUtil.safe(MonHocPanel.this, MonHocPanel.this::reloadTable); }
            @Override public void changedUpdate(DocumentEvent e) { UiUtil.safe(MonHocPanel.this, MonHocPanel.this::reloadTable); }
        });

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(addBox("Thêm môn học mới", form, btnThem));
        north.add(search);
        add(north, BorderLayout.NORTH);

        add(tableBox("Danh sách môn học (nhấp đúp để sửa)", table), BorderLayout.CENTER);

        JButton btnSua = new JButton("Chỉnh sửa");
        JButton btnXoa = AppTheme.danger(new JButton("Xóa"));
        btnSua.addActionListener(e -> UiUtil.safe(this, this::sua));
        btnXoa.addActionListener(e -> UiUtil.safe(this, this::xoa));
        add(buttonsRight(btnSua, btnXoa), BorderLayout.SOUTH);
        table.onDoubleClick(() -> UiUtil.safe(this, this::sua));
    }

    @Override
    public void refresh() {
        reloadTable();
    }

    private void reloadTable() {
        List<RowTable.Row> rows = new ArrayList<>();
        for (MonHoc m : ctx.monHocDao.timKiem(timKiem.getText())) {
            rows.add(new RowTable.Row(m.getId(), RowTable.Style.NONE,
                    m.getId(), m.getTenMon(), m.getSoTinChi(), m.getGiangVien(), m.getGhiChu()));
        }
        table.setRows(rows);
    }

    private void them() {
        MonHoc m = form.read(this);
        if (m == null) return;
        ctx.monHocService.save(m);
        form.clear();
        ctx.refreshAll();
        UiUtil.info(this, "Thành công", "Đã thêm môn học '" + m.getTenMon() + "'.");
    }

    private void sua() {
        Integer id = table.selectedId();
        if (id == null) {
            UiUtil.warn(this, "Chưa chọn", "Vui lòng chọn môn học cần sửa.");
            return;
        }
        MonHoc mh = ctx.monHocDao.layTheoId(id);
        if (mh == null) {
            UiUtil.error(this, "Lỗi", "Không tìm thấy môn học này.");
            ctx.refreshAll();
            return;
        }
        MonHocForm f = new MonHocForm(1);
        f.fill(mh);
        new EditDialog<>(window(), "Sửa môn học", f, (m, dlg) -> {
            m.setId(id);
            ctx.monHocService.save(m);
            ctx.refreshAll();
            UiUtil.info(dlg, "Thành công", "Đã cập nhật môn học '" + m.getTenMon() + "'.");
            return true;
        }).setVisible(true);
    }

    private void xoa() {
        Integer id = table.selectedId();
        if (id == null) {
            UiUtil.warn(this, "Chưa chọn", "Vui lòng chọn môn học cần xóa.");
            return;
        }
        MonHoc mh = ctx.monHocDao.layTheoId(id);
        String ten = mh == null ? "" : mh.getTenMon();
        if (UiUtil.confirm(this, "Xác nhận", "Xóa môn học '" + ten + "'?\n"
                + "(Lịch học và lịch thi liên quan sẽ bị xóa; bài tập và ghi chú sẽ được giữ lại nhưng không gắn môn)")) {
            ctx.monHocService.delete(id);
            ctx.refreshAll();
        }
    }
}
