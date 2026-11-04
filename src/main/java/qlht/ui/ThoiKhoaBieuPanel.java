package qlht.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qlht.model.LichHoc;
import qlht.model.MonHoc;
import qlht.util.TimeUtil;

/** Lịch tuần trực quan, kèm chế độ danh sách và bộ lọc môn học. */
public class ThoiKhoaBieuPanel extends TabPanel {
    private static final long serialVersionUID = 1L;
    private final WeekScheduleView week = new WeekScheduleView();
    private final RowTable table = new RowTable(
            new String[]{"Môn học", "Thứ", "Tiết học", "Khung giờ", "Phòng", "Lưu ý"},
            new int[]{220, 90, 90, 140, 100, 130});
    private final JComboBox<SubjectFilter> subject = new JComboBox<>();
    private final CardLayout views = new CardLayout();
    private final JPanel content = AppTheme.transparent(views);
    private final JLabel summary = AppTheme.label("", 12, Font.PLAIN, AppTheme.MUTED);
    private final JLabel warning = AppTheme.label("", 12, Font.BOLD, AppTheme.DANGER);
    private final JLabel selection = AppTheme.label("Chọn một buổi học để xem chi tiết.", 12, Font.PLAIN, AppTheme.MUTED);
    private final JButton edit = new JButton("Chỉnh sửa");
    private final JButton delete = AppTheme.danger(new JButton("Xóa lịch"));
    private List<LichHoc> schedules = List.of();
    private Set<Integer> conflicts = Set.of();
    private Integer selectedId;
    private boolean listView;
    private boolean refreshing;

    private record SubjectFilter(Integer id, String title) {
        @Override public String toString() { return title; }
    }

    public ThoiKhoaBieuPanel(AppContext ctx) {
        super(ctx);
        JPanel top = AppTheme.transparent(new BorderLayout(12, 12));
        JPanel text = AppTheme.transparent(new BorderLayout(0, 7));
        text.add(AppTheme.label("Nhịp học trong tuần", 21, Font.BOLD, AppTheme.TEXT), BorderLayout.NORTH);
        text.add(summary, BorderLayout.SOUTH);
        top.add(text, BorderLayout.CENTER);
        JButton add = AppTheme.primary(new JButton("+ Thêm lịch học"));
        add.addActionListener(e -> UiUtil.safe(this, () -> them(null, 1)));
        top.add(buttonsRight(add), BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        AppTheme.Card card = new AppTheme.Card(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(16, 16, 12, 16));
        JPanel toolbar = AppTheme.transparent(new BorderLayout(16, 10));
        JPanel modes = AppTheme.transparent(new FlowLayout(FlowLayout.LEFT, 6, 0));
        ButtonGroup group = new ButtonGroup();
        JToggleButton calendar = new JToggleButton("Lịch tuần", true), list = new JToggleButton("Danh sách");
        for (JToggleButton button : new JToggleButton[]{calendar, list}) {
            button.setMargin(new Insets(8, 14, 8, 14)); group.add(button); modes.add(button);
        }
        calendar.addActionListener(e -> switchView(false)); list.addActionListener(e -> switchView(true));
        toolbar.add(modes, BorderLayout.WEST);
        JPanel filters = AppTheme.transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filters.add(AppTheme.label("Môn học", 12, Font.PLAIN, AppTheme.MUTED));
        subject.setName("schedule.subjectFilter"); subject.setPreferredSize(new Dimension(230, 34));
        subject.addActionListener(e -> { if (!refreshing) render(); });
        filters.add(subject); toolbar.add(filters, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);
        content.add(week, "week");
        JScrollPane rows = new JScrollPane(table);
        rows.setBorder(BorderFactory.createEmptyBorder()); rows.getViewport().setBackground(Color.WHITE);
        content.add(rows, "list");
        card.add(content, BorderLayout.CENTER);
        JPanel legend = AppTheme.transparent(new BorderLayout(10, 4));
        legend.add(AppTheme.label("Màu theo môn · Viền đỏ: trùng lịch · Nền xanh: hôm nay", 10, Font.PLAIN, AppTheme.MUTED), BorderLayout.WEST);
        legend.add(AppTheme.label("Nhấp đúp để sửa / thêm vào ô trống", 10, Font.PLAIN, AppTheme.MUTED), BorderLayout.EAST);
        legend.add(warning, BorderLayout.SOUTH);
        card.add(legend, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);

        JPanel actions = AppTheme.transparent(new BorderLayout(12, 0));
        selection.putClientProperty("html.disable", true);
        selection.setToolTipText("Bấm vào một buổi học để xem môn, giờ và phòng học; Enter để chỉnh sửa.");
        actions.add(selection, BorderLayout.CENTER);
        edit.setEnabled(false); delete.setEnabled(false);
        edit.addActionListener(e -> UiUtil.safe(this, this::sua));
        delete.addActionListener(e -> UiUtil.safe(this, this::xoa));
        actions.add(buttonsRight(edit, delete), BorderLayout.EAST);
        add(actions, BorderLayout.SOUTH);
        week.onSelect(lesson -> select(lesson == null ? null : lesson.getId()));
        week.onEdit(lesson -> { select(lesson.getId()); UiUtil.safe(this, this::sua); });
        week.onCreate((day, period) -> UiUtil.safe(this, () -> them(day, period)));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listView && !refreshing) select(table.selectedId());
        });
        table.onDoubleClick(() -> UiUtil.safe(this, this::sua));
        table.setEmptyMessage("Chưa có lịch học phù hợp", "Thêm lịch học hoặc chọn môn khác để xem.");
    }

    @Override public void refresh() {
        Integer keep = subject.getSelectedItem() instanceof SubjectFilter item ? item.id() : null;
        refreshing = true;
        try {
            subject.removeAllItems(); subject.addItem(new SubjectFilter(null, "Tất cả môn học"));
            for (MonHoc mon : ctx.monHocDao.layTatCa()) {
                SubjectFilter option = new SubjectFilter(mon.getId(), mon.getTenMon());
                subject.addItem(option);
                if (Objects.equals(keep, mon.getId())) subject.setSelectedItem(option);
            }
            schedules = ctx.lichHocDao.layThoiKhoaBieu();
            conflicts = ctx.conflicts.idLichHocXungDot();
        } finally { refreshing = false; }
        render();
    }

    private List<LichHoc> filtered() {
        Integer id = subject.getSelectedItem() instanceof SubjectFilter item ? item.id() : null;
        return schedules.stream().filter(l -> id == null || Objects.equals(l.getMonHocId(), id)).toList();
    }

    private void render() {
        List<LichHoc> lessons = filtered();
        int periods = lessons.stream().mapToInt(l -> Math.max(0, l.getTietKt() - l.getTietBd() + 1)).sum();
        long days = lessons.stream().map(LichHoc::getThu).distinct().count();
        long collisions = lessons.stream().filter(l -> conflicts.contains(l.getId())).count();
        summary.setText(lessons.size() + " buổi / tuần     ·     " + periods + " tiết học     ·     " + days + " ngày có lịch"
                + (collisions == 0 ? "" : "     ·     " + collisions + " buổi trùng lịch"));
        week.setSchedules(lessons, conflicts);
        List<RowTable.Row> rows = new ArrayList<>();
        for (LichHoc l : lessons) rows.add(new RowTable.Row(l.getId(), conflicts.contains(l.getId()) ? RowTable.Style.CONFLICT : RowTable.Style.NONE,
                l.getTenMon(), l.getThu(), l.getTietBd() + "–" + l.getTietKt(), TimeUtil.moTaGioTiet(l.getTietBd(), l.getTietKt()),
                l.getPhong() == null || l.getPhong().isBlank() ? "Chưa có phòng" : l.getPhong(), conflicts.contains(l.getId()) ? "Trùng lịch" : ""));
        refreshing = true;
        try { table.setRows(rows); } finally { refreshing = false; }
        long invalid = lessons.size() - week.displayedCount();
        warning.setText(invalid == 0 ? "" : invalid + " buổi có thứ/tiết chưa hợp lệ; xem tại Danh sách để chỉnh sửa.");
        warning.setVisible(invalid > 0);
        if (lessons.stream().noneMatch(l -> Objects.equals(l.getId(), selectedId))) selectedId = null;
        refreshing = true;
        try {
            table.clearSelection();
            for (int i = 0; i < lessons.size(); i++) if (Objects.equals(lessons.get(i).getId(), selectedId))
                table.setRowSelectionInterval(i, i);
            if (!listView) week.select(selectedId);
        } finally { refreshing = false; }
        updateSelection();
    }

    private void switchView(boolean list) {
        listView = list; views.show(content, list ? "list" : "week");
        refreshing = true;
        try {
            if (list) {
                table.clearSelection();
                List<LichHoc> lessons = filtered();
                for (int i = 0; i < lessons.size(); i++) if (Objects.equals(lessons.get(i).getId(), selectedId)) table.setRowSelectionInterval(i, i);
            } else week.select(selectedId);
        } finally { refreshing = false; }
        updateSelection();
    }

    private void select(Integer id) { selectedId = id; updateSelection(); }

    private void updateSelection() {
        LichHoc lesson = schedules.stream().filter(l -> Objects.equals(l.getId(), selectedId)).findFirst().orElse(null);
        edit.setEnabled(lesson != null); delete.setEnabled(lesson != null);
        selection.setForeground(lesson != null && conflicts.contains(lesson.getId()) ? AppTheme.DANGER : AppTheme.MUTED);
        selection.setText(lesson == null ? "Lịch lặp lại hằng tuần · Giờ hiển thị theo quy đổi tiết học." :
                lesson.getTenMon() + " · " + lesson.getThu() + " · Tiết " + lesson.getTietBd() + "–" + lesson.getTietKt()
                        + " · " + TimeUtil.moTaGioTiet(lesson.getTietBd(), lesson.getTietKt())
                        + (lesson.getPhong() == null || lesson.getPhong().isBlank() ? "" : " · Phòng " + lesson.getPhong())
                        + (conflicts.contains(lesson.getId()) ? " · Trùng lịch" : ""));
        selection.setToolTipText(selection.getText());
    }

    private boolean xacNhan(LichHoc l, Component parent) {
        List<LichHoc> trung = ctx.conflicts.trungLichHoc(l.getThu(), l.getTietBd(), l.getTietKt(), l.isNew() ? null : l.getId());
        if (trung.isEmpty()) return true;
        String ds = trung.stream().map(LichHoc::moTa).collect(Collectors.joining("\n"));
        return UiUtil.confirmWarn(parent, "Cảnh báo trùng lịch học", "Lịch học (" + l.getThu() + ", tiết " + l.getTietBd() + "–" + l.getTietKt()
                + ") trùng với:\n" + ds + "\n\nBạn vẫn muốn lưu?");
    }

    private boolean luu(LichHoc l, Component parent) {
        if (!xacNhan(l, parent)) return false;
        if (l.isNew()) ctx.lichHocDao.them(l); else ctx.lichHocDao.sua(l);
        return true;
    }

    private void them(String day, int period) {
        List<MonHoc> subjects = ctx.monHocDao.layTatCa();
        if (subjects.isEmpty()) { UiUtil.warn(this, "Chưa có môn học", "Hãy thêm môn học ở mục Môn học trước khi xếp lịch."); return; }
        LichHocForm form = new LichHocForm(1); form.reloadMon(subjects);
        if (day != null) form.suggest(day, period);
        if (subject.getSelectedItem() instanceof SubjectFilter item && item.id() != null) form.selectSubject(item.id());
        new EditDialog<>(window(), "Thêm lịch học", form, (l, dialog) -> {
            if (!luu(l, dialog)) return false;
            selectedId = l.getId(); ctx.refreshAll(); return true;
        }).setVisible(true);
    }

    private void sua() {
        if (selectedId == null) return;
        final int id = selectedId;
        LichHoc lesson = ctx.lichHocDao.layTheoId(id);
        if (lesson == null) { ctx.refreshAll(); return; }
        LichHocForm form = new LichHocForm(1); form.reloadMon(ctx.monHocDao.layTatCa()); form.fill(lesson);
        new EditDialog<>(window(), "Sửa lịch học", form, (l, dialog) -> {
            l.setId(id); if (!luu(l, dialog)) return false;
            ctx.refreshAll(); return true;
        }).setVisible(true);
    }

    private void xoa() {
        if (selectedId == null) return;
        LichHoc lesson = ctx.lichHocDao.layTheoId(selectedId);
        if (lesson == null) { ctx.refreshAll(); return; }
        if (UiUtil.confirm(this, "Xóa lịch học", "Xóa lịch học '" + lesson.getTenMon() + "' (" + lesson.getThu() + ", tiết "
                + lesson.getTietBd() + "–" + lesson.getTietKt() + ")?")) {
            ctx.lichHocDao.xoa(lesson.getId()); selectedId = null; ctx.refreshAll();
        }
    }
}
