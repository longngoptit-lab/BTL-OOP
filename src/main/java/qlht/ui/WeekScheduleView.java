package qlht.ui;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.*;
import qlht.model.LichHoc;
import qlht.util.Const;
import qlht.util.TimeUtil;

/** Lịch tuần có trục giờ và tiêu đề cố định khi cuộn. Không thay đổi dữ liệu nghiệp vụ. */
public final class WeekScheduleView extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int START = 7 * 60, END = 20 * 60, DAY_WIDTH = 124, AXIS_WIDTH = 62;
    private static final double SCALE = 1.15;
    private static final int GRID_HEIGHT = (int) ((END - START) * SCALE) + 18;
    private static final Color[] ACCENTS = {
        new Color(0x287A69), new Color(0x5C6BB2), new Color(0xAD7840), new Color(0xA35C7B),
        new Color(0x377D9B), new Color(0x7A68A4), new Color(0x758246), new Color(0xBA6851)
    };
    private static final Color[] FILLS = {
        new Color(0xE8F3EE), new Color(0xECEDF9), new Color(0xFBF1E3), new Color(0xF9EBF1),
        new Color(0xE8F3F9), new Color(0xF1EDF8), new Color(0xF1F4E5), new Color(0xFBEDE7)
    };
    private final Grid grid = new Grid();
    private final Header header = new Header();
    private final JScrollPane scroll = new JScrollPane(grid);
    private List<WeekScheduleLayout.Slot> slots = List.of();
    private Set<Integer> conflicts = Set.of();
    private Integer selectedId;
    private Consumer<LichHoc> onSelect = lesson -> { };
    private Consumer<LichHoc> onEdit = lesson -> { };
    private BiConsumer<String, Integer> onCreate = (day, period) -> { };

    public WeekScheduleView() {
        super(new BorderLayout());
        setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setColumnHeaderView(header);
        Axis axis = new Axis();
        scroll.setRowHeaderView(axis);
        JLabel corner = AppTheme.label("GIỜ", 10, Font.BOLD, AppTheme.MUTED);
        corner.setHorizontalAlignment(SwingConstants.CENTER);
        corner.setOpaque(true); corner.setBackground(Color.WHITE);
        scroll.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, corner);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(40);
        scroll.getHorizontalScrollBar().setUnitIncrement(DAY_WIDTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void onSelect(Consumer<LichHoc> action) { onSelect = Objects.requireNonNull(action); }
    public void onEdit(Consumer<LichHoc> action) { onEdit = Objects.requireNonNull(action); }
    public void onCreate(BiConsumer<String, Integer> action) { onCreate = Objects.requireNonNull(action); }
    public int displayedCount() { return slots.size(); }
    public Integer selectedId() { return selectedId; }

    public void setSchedules(List<LichHoc> lessons, Set<Integer> conflictingIds) {
        slots = WeekScheduleLayout.arrange(lessons);
        conflicts = Set.copyOf(conflictingIds);
        if (selectedId != null && slots.stream().noneMatch(s -> s.lesson().getId() == selectedId)) selectedId = null;
        grid.repaint(); header.repaint();
    }

    public void select(Integer id) {
        LichHoc lesson = slots.stream().map(WeekScheduleLayout.Slot::lesson)
                .filter(l -> Objects.equals(l.getId(), id)).findFirst().orElse(null);
        selectedId = lesson == null ? null : lesson.getId();
        onSelect.accept(lesson);
        grid.getAccessibleContext().setAccessibleDescription(lesson == null ? "Chưa chọn buổi học" : describe(lesson));
        grid.repaint();
    }

    /** Khung giờ trống giữa hai tiết sẽ gợi ý tiết tiếp theo. */
    public static int periodAtMinute(int minute) {
        for (int i = 1; i <= 12; i++) {
            int[] time = TimeUtil.khoangGioTiet(i, i);
            if (minute < time[1]) return i;
        }
        return 12;
    }

    private int today() { return LocalDate.now().getDayOfWeek().getValue() - 1; }
    private static int colorIndex(LichHoc lesson) { return Math.floorMod(lesson.getMonHocId() - 1, ACCENTS.length); }
    private double dayWidth() { return grid.getWidth() / 7.0; }
    private static String clock(int minute) { return String.format("%02d:%02d", minute / 60, minute % 60); }

    private Rectangle lessonBounds(WeekScheduleLayout.Slot slot) {
        double day = dayWidth();
        int gap = slot.laneCount() == 1 ? 6 : 3;
        double laneWidth = (day - 10) / slot.laneCount();
        int left = (int) Math.round(slot.day() * day + 5 + slot.lane() * laneWidth);
        return new Rectangle(left + gap / 2, (int) ((slot.start() - START) * SCALE) + 3,
                Math.max(8, (int) laneWidth - gap), Math.max(18, (int) ((slot.end() - slot.start()) * SCALE) - 6));
    }

    private WeekScheduleLayout.Slot hit(Point point) {
        for (WeekScheduleLayout.Slot slot : slots) if (lessonBounds(slot).contains(point)) return slot;
        return null;
    }

    private static String describe(LichHoc lesson) {
        return (lesson.getTenMon() == null ? "Môn học" : lesson.getTenMon()) + " · " + lesson.getThu()
                + " · Tiết " + lesson.getTietBd() + "–" + lesson.getTietKt() + " · "
                + TimeUtil.moTaGioTiet(lesson.getTietBd(), lesson.getTietKt()) + " · "
                + (lesson.getPhong() == null || lesson.getPhong().isBlank() ? "Chưa có phòng" : "Phòng " + lesson.getPhong());
    }

    private static Graphics2D graphics(Graphics g) {
        Graphics2D p = (Graphics2D) g.create();
        p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        p.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return p;
    }

    private static String ellipsis(FontMetrics metrics, String text, int width) {
        if (metrics.stringWidth(text) <= width) return text;
        int end = text.length();
        while (end > 0 && metrics.stringWidth(text.substring(0, end) + "…") > width) end--;
        return end == 0 ? "" : text.substring(0, end).stripTrailing() + "…";
    }

    private static List<String> wrap(String text, FontMetrics metrics, int width, int maxLines) {
        List<String> lines = new ArrayList<>();
        String rest = text.strip();
        for (int line = 0; line < maxLines && !rest.isEmpty(); line++) {
            if (metrics.stringWidth(rest) <= width) { lines.add(rest); break; }
            if (line == maxLines - 1) { lines.add(ellipsis(metrics, rest, width)); break; }
            int split = rest.length();
            while (split > 0 && metrics.stringWidth(rest.substring(0, split)) > width) split--;
            if (split == 0) break;
            int space = rest.lastIndexOf(' ', split);
            if (space > 0) split = space;
            lines.add(rest.substring(0, split)); rest = rest.substring(split).stripLeading();
        }
        return lines;
    }

    private final class Grid extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;
        Grid() {
            setBackground(Color.WHITE); setFocusable(true);
            setName("schedule.weekGrid");
            getAccessibleContext().setAccessibleName("Lịch học tuần, thứ 2 đến chủ nhật");
            ToolTipManager.sharedInstance().registerComponent(this);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e)) return;
                    requestFocusInWindow();
                    WeekScheduleLayout.Slot slot = hit(e.getPoint());
                    select(slot == null ? null : slot.lesson().getId());
                    if (e.getClickCount() != 2) return;
                    if (slot != null) onEdit.accept(slot.lesson());
                    else {
                        int day = Math.min(6, Math.max(0, (int) (e.getX() / dayWidth())));
                        int minute = START + (int) (e.getY() / SCALE);
                        onCreate.accept(Const.DAYS.get(day), periodAtMinute(minute));
                    }
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    setCursor(Cursor.getPredefinedCursor(hit(e.getPoint()) == null ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
                }
            });
            bind("RIGHT", "next", () -> moveSelection(1)); bind("DOWN", "nextDown", () -> moveSelection(1));
            bind("LEFT", "previous", () -> moveSelection(-1)); bind("UP", "previousUp", () -> moveSelection(-1));
            bind("ESCAPE", "clear", () -> select(null));
            bind("ENTER", "edit", () -> slots.stream().map(WeekScheduleLayout.Slot::lesson)
                    .filter(l -> Objects.equals(l.getId(), selectedId)).findFirst().ifPresent(onEdit));
        }

        private void bind(String key, String name, Runnable action) {
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key), name);
            getActionMap().put(name, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { action.run(); }
            });
        }

        private void moveSelection(int direction) {
            if (slots.isEmpty()) return;
            int index = -1;
            for (int i = 0; i < slots.size(); i++) if (Objects.equals(slots.get(i).lesson().getId(), selectedId)) index = i;
            index = index < 0 ? (direction > 0 ? 0 : slots.size() - 1) : Math.floorMod(index + direction, slots.size());
            WeekScheduleLayout.Slot slot = slots.get(index);
            select(slot.lesson().getId()); scrollRectToVisible(lessonBounds(slot));
        }

        @Override public String getToolTipText(MouseEvent e) {
            WeekScheduleLayout.Slot slot = hit(e.getPoint());
            if (slot == null) return "Nhấp đúp ô trống để thêm lịch học";
            return (conflicts.contains(slot.lesson().getId()) ? "TRÙNG LỊCH · " : "") + describe(slot.lesson());
        }
        @Override public Dimension getPreferredSize() { return new Dimension(7 * DAY_WIDTH, GRID_HEIGHT); }
        @Override public Dimension getPreferredScrollableViewportSize() { return new Dimension(7 * DAY_WIDTH, 480); }
        @Override public boolean getScrollableTracksViewportWidth() { return getParent() != null && getParent().getWidth() >= 7 * DAY_WIDTH; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
        @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return orientation == SwingConstants.VERTICAL ? 40 : DAY_WIDTH; }
        @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) { return orientation == SwingConstants.VERTICAL ? Math.max(40, visible.height - 60) : Math.max(DAY_WIDTH, visible.width - DAY_WIDTH); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D p = graphics(g);
            double width = dayWidth();
            for (int day = 0; day < 7; day++) {
                p.setColor(day == today() ? new Color(0xF4FAF6) : day >= 5 ? new Color(0xFAFBFD) : Color.WHITE);
                int x = (int) Math.round(day * width);
                p.fillRect(x, 0, (int) Math.ceil(width), getHeight());
                p.setColor(AppTheme.LINE); p.drawLine(x, 0, x, getHeight());
            }
            for (int minute = START; minute <= END; minute += 30) {
                int y = (int) ((minute - START) * SCALE);
                p.setColor(minute % 60 == 0 ? AppTheme.LINE : new Color(0xF1F4F7));
                p.drawLine(0, y, getWidth(), y);
            }
            for (WeekScheduleLayout.Slot slot : slots) paintLesson(p, slot);
            if (slots.isEmpty()) {
                Rectangle visible = getVisibleRect();
                int x = visible.x + visible.width / 2, y = visible.y + Math.min(visible.height / 2, 150);
                p.setColor(Color.WHITE); p.fillRoundRect(x - 185, y - 46, 370, 92, 16, 16);
                p.setColor(AppTheme.TEXT); p.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
                String title = "Tuần học đang chờ bạn lên kế hoạch";
                p.drawString(title, x - p.getFontMetrics().stringWidth(title) / 2, y - 7);
                p.setColor(AppTheme.MUTED); p.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                String note = "Thêm lịch học hoặc nhấp đúp vào một ô trống.";
                p.drawString(note, x - p.getFontMetrics().stringWidth(note) / 2, y + 18);
            }
            if (hasFocus()) {
                Rectangle v = getVisibleRect(); p.setColor(new Color(0xB6D9CC));
                p.drawRect(v.x, v.y, Math.max(0, v.width - 1), Math.max(0, v.height - 1));
            }
            p.dispose();
        }

        private void paintLesson(Graphics2D original, WeekScheduleLayout.Slot slot) {
            Rectangle r = lessonBounds(slot);
            if (original.getClipBounds() != null && !original.getClipBounds().intersects(r)) return;
            LichHoc lesson = slot.lesson();
            int palette = colorIndex(lesson);
            boolean conflict = conflicts.contains(lesson.getId()), selected = Objects.equals(selectedId, lesson.getId());
            Color accent = conflict ? AppTheme.DANGER : ACCENTS[palette];
            Graphics2D p = (Graphics2D) original.create();
            p.setColor(FILLS[palette]); p.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            p.setColor(selected ? AppTheme.PRIMARY : conflict ? AppTheme.DANGER : FILLS[palette].darker());
            p.setStroke(new BasicStroke(selected || conflict ? 2 : 0.7f));
            p.drawRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 10, 10);
            p.setColor(accent); p.fillRoundRect(r.x + 1, r.y + 8, 3, Math.max(10, r.height - 16), 3, 3);
            p.clipRect(r.x + 7, r.y + 4, Math.max(1, r.width - 14), r.height - 8);
            int textWidth = Math.max(0, r.width - 20), x = r.x + 10, y = r.y + 17;
            p.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12)); p.setColor(ACCENTS[palette]);
            String name = lesson.getTenMon() == null ? "Môn học" : lesson.getTenMon();
            boolean narrow = textWidth < 85;
            int maxLines = r.height >= 160 && narrow ? 5 : r.height >= 90 ? 2 : 1;
            for (String line : wrap(name, p.getFontMetrics(), textWidth, maxLines)) {
                p.drawString(line, x, y); y += 15;
            }
            p.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            if (narrow && r.height >= 140) {
                p.drawString(ellipsis(p.getFontMetrics(), clock(slot.start()), textWidth), x, y); y += 13;
                p.drawString(ellipsis(p.getFontMetrics(), "– " + clock(slot.end()), textWidth), x, y); y += 14;
            } else {
                p.drawString(ellipsis(p.getFontMetrics(), clock(slot.start()) + "–" + clock(slot.end()), textWidth), x, y); y += 14;
            }
            String room = lesson.getPhong() == null || lesson.getPhong().isBlank() ? "Chưa có phòng" : "Phòng " + lesson.getPhong();
            int roomLines = narrow && r.height >= 160 ? 2 : 1;
            for (String line : wrap(room, p.getFontMetrics(), textWidth, roomLines)) {
                p.drawString(line, x, y); y += 13;
            }
            if (r.height >= 100) {
                p.setColor(conflict ? AppTheme.DANGER : ACCENTS[palette]);
                String caption = conflict ? (narrow ? "! Trùng" : "! Trùng lịch") : "Tiết " + lesson.getTietBd() + "–" + lesson.getTietKt();
                p.drawString(ellipsis(p.getFontMetrics(), caption, textWidth), x, r.y + r.height - 12);
            }
            p.dispose();
        }
    }

    private final class Header extends JPanel {
        private static final long serialVersionUID = 1L;
        @Override public Dimension getPreferredSize() { return new Dimension(7 * DAY_WIDTH, 58); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D p = graphics(g);
            double width = dayWidth();
            for (int day = 0; day < 7; day++) {
                int x = (int) Math.round(day * width), center = (int) ((day + 0.5) * width);
                p.setColor(day == today() ? new Color(0xEAF5EE) : Color.WHITE);
                p.fillRect(x, 0, (int) Math.ceil(width), getHeight());
                p.setColor(day == today() ? AppTheme.PRIMARY : AppTheme.TEXT);
                p.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                String name = Const.DAYS.get(day).toUpperCase(java.util.Locale.forLanguageTag("vi"));
                p.drawString(name, center - p.getFontMetrics().stringWidth(name) / 2, 23);
                final int dayIndex = day;
                long count = slots.stream().filter(s -> s.day() == dayIndex).count();
                String sub = day == today() ? "Hôm nay · " + count + " buổi" : count == 0 ? "Không có lịch" : count + " buổi học";
                p.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10)); p.setColor(AppTheme.MUTED);
                p.drawString(sub, center - p.getFontMetrics().stringWidth(sub) / 2, 43);
                p.setColor(AppTheme.LINE); p.drawLine(x, 0, x, getHeight());
            }
            p.setColor(AppTheme.LINE); p.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1); p.dispose();
        }
    }

    private static final class Axis extends JPanel {
        private static final long serialVersionUID = 1L;
        @Override public Dimension getPreferredSize() { return new Dimension(AXIS_WIDTH, GRID_HEIGHT); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D p = graphics(g);
            p.setColor(Color.WHITE); p.fillRect(0, 0, getWidth(), getHeight());
            for (int minute = START; minute <= END; minute += 60) {
                int y = (int) ((minute - START) * SCALE);
                p.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10)); p.setColor(AppTheme.MUTED);
                p.drawString(clock(minute), 4, Math.max(12, y + 4));
            }
            p.dispose();
        }
    }
}
