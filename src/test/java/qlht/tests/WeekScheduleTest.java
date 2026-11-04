package qlht.tests;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;
import qlht.model.LichHoc;
import qlht.ui.*;
import qlht.util.Const;

/** Kiểm tra cách xếp lịch và tương tác, không cần database. */
public final class WeekScheduleTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++; if (!condition) throw new AssertionError(message);
    }
    private static LichHoc lesson(int id, int subject, String name, String day, int start, int end, String room) {
        LichHoc l = new LichHoc(subject, name, day, start, end, room); l.setId(id); return l;
    }
    private static WeekScheduleLayout.Slot slot(List<WeekScheduleLayout.Slot> slots, int id) {
        return slots.stream().filter(s -> s.lesson().getId() == id).findFirst().orElseThrow();
    }
    public static void main(String[] args) throws Exception {
        List<LichHoc> overlap = List.of(
            lesson(1, 1, "A", "Thứ 2", 1, 3, ""), lesson(2, 2, "B", "Thứ 2", 2, 4, ""),
            lesson(3, 3, "C", "Thứ 2", 4, 5, ""), lesson(4, 4, "D", "Thứ 2", 6, 7, ""));
        List<WeekScheduleLayout.Slot> slots = WeekScheduleLayout.arrange(overlap);
        check(slots.size() == 4, "No overlapping lesson lost");
        check(slot(slots, 1).laneCount() == 2 && slot(slots, 2).laneCount() == 2 && slot(slots, 3).laneCount() == 2, "Transitive overlap cluster width");
        check(slot(slots, 1).lane() != slot(slots, 2).lane(), "Overlapping lessons use separate lanes");
        check(slot(slots, 1).lane() == slot(slots, 3).lane(), "Released lane reused");
        check(slot(slots, 4).laneCount() == 1, "Independent cluster fills day width");
        for (var a : slots) for (var b : slots) {
            if (a.lesson().getId() != b.lesson().getId() && a.day() == b.day() && a.start() < b.end() && b.start() < a.end())
                check(a.lane() != b.lane(), "No pair overlaps geometrically");
        }
        List<LichHoc> week = new ArrayList<>();
        for (int d = 0; d < 7; d++) week.add(lesson(10 + d, d + 1, "Ngày " + d, Const.DAYS.get(d), 1, 1, "A1"));
        List<WeekScheduleLayout.Slot> days = WeekScheduleLayout.arrange(week);
        for (int d = 0; d < 7; d++) check(slot(days, d + 10).day() == d && slot(days, d + 10).laneCount() == 1, "Correct weekday column");
        var late = WeekScheduleLayout.arrange(List.of(lesson(50, 1, "Tối", "Chủ nhật", 12, 12, ""))).get(0);
        check(late.start() == 19 * 60 && late.end() == 19 * 60 + 50, "Last period maps correctly");
        var lunch = WeekScheduleLayout.arrange(List.of(lesson(51, 1, "Trưa", "Thứ 3", 4, 5, ""))).get(0);
        check(lunch.start() == 600 && lunch.end() == 770, "Real time scale preserves hour 11 gap");
        check(WeekScheduleView.periodAtMinute(7 * 60) == 1, "First period suggestion");
        check(WeekScheduleView.periodAtMinute(7 * 60 + 55) == 2, "Break suggests next period");
        check(WeekScheduleView.periodAtMinute(11 * 60 + 30) == 5, "Lunch gap suggests noon period");
        check(WeekScheduleView.periodAtMinute(20 * 60) == 12, "End boundary clamped");
        check(WeekScheduleLayout.arrange(List.of(lesson(60, 1, "X", "Invalid", 1, 2, ""),
                lesson(61, 1, "X", "Thứ 2", 5, 1, ""), lesson(62, 1, "X", "Thứ 2", 0, 13, ""))).isEmpty(), "Malformed legacy entries don't crash layout");
        check(WeekScheduleLayout.arrange(List.of()).isEmpty(), "Empty data");
        if (Arrays.asList(args).contains("--ui")) SwingUtilities.invokeAndWait(WeekScheduleTest::reviewUi);
        System.out.println("PASS: " + checks + " weekly calendar checks.");
    }

    private static void reviewUi() {
        JFrame frame = new JFrame("Lịch tuần · Dữ liệu minh họa");
        try {
            AppTheme.install();
            WeekScheduleView view = new WeekScheduleView();
            List<LichHoc> sample = List.of(
                lesson(101, 1, "Lập trình hướng đối tượng", "Thứ 2", 1, 3, "A2-301"),
                lesson(102, 2, "Cơ sở dữ liệu", "Thứ 2", 5, 7, "B1-204"),
                lesson(103, 3, "Toán rời rạc", "Thứ 3", 2, 4, "C2-105"),
                lesson(104, 1, "Lập trình hướng đối tượng", "Thứ 4", 1, 3, "A2-301"),
                lesson(105, 4, "Mạng máy tính", "Thứ 4", 2, 4, "D1-201"),
                lesson(106, 5, "Tiếng Anh", "Thứ 5", 3, 4, "E2-406"),
                lesson(107, 2, "Cơ sở dữ liệu", "Thứ 6", 1, 3, "B1-204"),
                lesson(108, 6, "Kỹ năng thuyết trình", "Thứ 7", 5, 6, "A1-102"),
                lesson(109, 7, "Học nhóm", "Chủ nhật", 12, 12, "Thư viện"));
            view.setSchedules(sample, Set.of(104, 105));
            AtomicReference<Integer> selected = new AtomicReference<>();
            AtomicReference<Integer> edited = new AtomicReference<>();
            AtomicReference<String> createDay = new AtomicReference<>();
            AtomicReference<Integer> createPeriod = new AtomicReference<>();
            view.onSelect(l -> selected.set(l == null ? null : l.getId()));
            view.onEdit(l -> edited.set(l.getId()));
            view.onCreate((day, period) -> { createDay.set(day); createPeriod.set(period); });
            JPanel root = new JPanel(new BorderLayout(0, 16)); root.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
            JLabel title = AppTheme.label("Lịch học hằng tuần  ·  9 buổi  ·  2 buổi trùng lịch", 20, Font.BOLD, AppTheme.TEXT);
            root.add(title, BorderLayout.NORTH);
            JPanel card = new AppTheme.Card(new BorderLayout()); card.add(view); root.add(card);
            root.add(AppTheme.label("Dữ liệu minh họa · Màu theo môn · Viền đỏ: trùng lịch · Nhấp đúp ô trống để thêm", 12, Font.PLAIN, AppTheme.MUTED), BorderLayout.SOUTH);
            frame.setContentPane(root); frame.setSize(1300, 820); frame.setVisible(true); frame.validate();
            check(view.displayedCount() == sample.size(), "All cards present");
            view.select(101); check(Objects.equals(selected.get(), 101), "Selection callback");
            view.setSchedules(sample, Set.of(104, 105)); check(Objects.equals(view.selectedId(), 101), "Selection preserved on reload");
            JComponent grid = (JComponent) findNamed(view, "schedule.weekGrid");
            grid.getActionMap().get("edit").actionPerformed(new ActionEvent(grid, 0, "edit"));
            check(Objects.equals(edited.get(), 101), "Enter edits selected lesson");
            grid.getActionMap().get("next").actionPerformed(new ActionEvent(grid, 0, "next"));
            check(Objects.equals(view.selectedId(), 102), "Keyboard advances selection");
            grid.getActionMap().get("clear").actionPerformed(new ActionEvent(grid, 0, "clear"));
            check(view.selectedId() == null, "Escape clears selection");
            // Nhấp đúp thứ 5, 12:00 -> tiết 5, không cần ghi dữ liệu vào DB.
            int x = (int) (3.5 * grid.getWidth() / 7.0), y = (int) (5 * 60 * 1.15);
            grid.dispatchEvent(new MouseEvent(grid, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, x, y, 2, false, MouseEvent.BUTTON1));
            check("Thứ 5".equals(createDay.get()) && Objects.equals(createPeriod.get(), 5), "Blank slot prefill callback");
            JScrollPane scroll = (JScrollPane) view.getComponent(0);
            scroll.getViewport().setViewPosition(new Point(0, 0));
            Path folder = Files.createTempDirectory("qlht-week-");
            save(root, folder.resolve("weekly.png"));
            frame.setSize(900, 740); frame.validate(); save(root, folder.resolve("narrow.png"));
            check(scroll.getHorizontalScrollBar().isVisible(), "Narrow window keeps readable columns and horizontal scrolling");
            view.setSchedules(List.of(), Set.of()); check(view.selectedId() == null, "Removed selection cleared");
            save(root, folder.resolve("empty.png"));
            System.out.println("UI previews: " + folder);
        } catch (Exception e) { throw new AssertionError(e); }
        finally { frame.dispose(); }
    }

    private static Component findNamed(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName())) return c;
            if (c instanceof Container nested) { Component result = findNamed(nested, name); if (result != null) return result; }
        }
        return null;
    }
    private static void save(Component c, Path path) throws Exception {
        BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics(); c.printAll(g); g.dispose(); ImageIO.write(image, "png", path.toFile());
    }
}
