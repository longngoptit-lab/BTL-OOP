package qlht.ui;

import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qlht.model.BaiTap;
import qlht.util.Const;
import qlht.util.DateUtil;

public class DashboardPanel extends TabPanel {
    private static final long serialVersionUID = 1L;
    private final JLabel cardMon = valueLabel();
    private final JLabel cardBaiTap = valueLabel();
    private final JLabel cardThi = valueLabel();
    private final JLabel cardGhiChu = valueLabel();
    private final JLabel deadlineCount = AppTheme.label("", 12, Font.PLAIN, AppTheme.MUTED);
    private final ProgressRing progress = new ProgressRing();
    private final RowTable table = new RowTable(
            new String[]{"Môn học", "Bài tập cần hoàn thành", "Hạn nộp", "Ưu tiên"}, new int[]{180, 280, 210, 100});

    public DashboardPanel(AppContext ctx) {
        super(ctx);
        JPanel top = AppTheme.transparent(new BorderLayout(0, 18));
        top.add(hero(), BorderLayout.NORTH);
        JPanel cards = AppTheme.transparent(new GridLayout(1, 4, 14, 0));
        cards.add(card(cardMon, "Môn học", "trong kế hoạch của bạn", 1, AppTheme.PRIMARY));
        cards.add(card(cardBaiTap, "Bài tập đang chờ", "từng bước hoàn thành", 4, new Color(0xBD8440)));
        cards.add(card(cardThi, "Kỳ thi sắp tới", "chuẩn bị thật tự tin", 3, new Color(0x6A79B7)));
        cards.add(card(cardGhiChu, "Ghi chú đã lưu", "ý tưởng luôn ở đây", 5, new Color(0xAD7185)));
        top.add(cards, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        JPanel list = tableBox("Deadline cần chú ý", table);
        JPanel title = AppTheme.transparent(new BorderLayout());
        title.add(AppTheme.label("Deadline cần chú ý", 15, Font.BOLD, AppTheme.TEXT), BorderLayout.WEST);
        title.add(deadlineCount, BorderLayout.EAST);
        list.remove(0);
        list.add(title, BorderLayout.NORTH);
        JLabel hint = AppTheme.label("Theo dõi và cập nhật tiến độ tại mục Bài tập.", 11, Font.PLAIN, AppTheme.MUTED);
        hint.setBorder(new EmptyBorder(8, 0, 0, 0));
        list.add(hint, BorderLayout.SOUTH);
        add(list, BorderLayout.CENTER);
    }

    private JPanel hero() {
        AppTheme.Card hero = new AppTheme.Card(new BorderLayout(20, 0));
        hero.setBackground(new Color(0xE9F1EC));
        hero.setBorder(new EmptyBorder(22, 24, 22, 24));
        JPanel copy = AppTheme.transparent(new BorderLayout(0, 10));
        copy.add(AppTheme.label("HỌC TẬP CÓ KẾ HOẠCH", 10, Font.BOLD, AppTheme.PRIMARY), BorderLayout.NORTH);
        copy.add(AppTheme.label("Mỗi ngày, tiến thêm một bước.", 25, Font.BOLD, new Color(0x244C43)), BorderLayout.CENTER);
        copy.add(AppTheme.label("Sắp xếp hôm nay để tự tin hơn vào ngày mai.", 13, Font.PLAIN, new Color(0x617D71)), BorderLayout.SOUTH);
        hero.add(copy, BorderLayout.CENTER);
        hero.add(progress, BorderLayout.EAST);
        return hero;
    }

    private static JLabel valueLabel() { return AppTheme.label("0", 32, Font.BOLD, AppTheme.TEXT); }

    private static JPanel card(JLabel value, String title, String subtitle, int icon, Color accent) {
        AppTheme.Card card = new AppTheme.Card(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        JPanel top = AppTheme.transparent(new BorderLayout(10, 0));
        top.add(AppTheme.label(title, 12, Font.BOLD, AppTheme.MUTED), BorderLayout.CENTER);
        top.add(new JLabel(AppTheme.icon(icon, accent)), BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(AppTheme.label(subtitle, 10, Font.PLAIN, AppTheme.MUTED), BorderLayout.SOUTH);
        return card;
    }

    @Override public void refresh() {
        LocalDate today = LocalDate.now();
        List<BaiTap> all = ctx.baiTapDao.layTatCa(false);
        List<BaiTap> pending = all.stream().filter(b -> !Const.STATUS_DONE.equals(b.getTrangThai())).toList();
        long upcoming = ctx.lichThiDao.layTatCa().stream()
                .filter(l -> l.getNgayThi() != null && !l.getNgayThi().isBefore(today)).count();
        cardMon.setText(String.valueOf(ctx.monHocDao.layTatCa().size()));
        cardBaiTap.setText(String.valueOf(pending.size()));
        cardThi.setText(String.valueOf(upcoming));
        cardGhiChu.setText(String.valueOf(ctx.ghiChuDao.layTatCa().size()));
        progress.completed = all.size() - pending.size();
        progress.total = all.size();
        progress.repaint();
        List<RowTable.Row> rows = new ArrayList<>();
        for (BaiTap b : pending) {
            if (b.getHanNop() == null) continue;
            long days = ChronoUnit.DAYS.between(today, b.getHanNop());
            if (days > 7) continue;
            String due = DateUtil.formatVn(b.getHanNop());
            RowTable.Style style = RowTable.Style.NONE;
            if (days < 0) {
                due += " · Quá hạn " + (-days) + " ngày";
                style = RowTable.Style.CONFLICT;
            } else if (days == 0) {
                due += " · Hôm nay";
                style = RowTable.Style.URGENT;
            }
            rows.add(new RowTable.Row(b.getId(), style,
                    b.getTenMon() == null ? "Chưa gắn môn học" : b.getTenMon(), b.getTieuDe(), due, b.getMucDoUuTien()));
        }
        table.setRows(rows);
        deadlineCount.setText("Quá hạn & 7 ngày tới · " + rows.size() + " bài tập");
        table.setEmptyMessage("Bạn đã bắt kịp kế hoạch", "Chưa có bài tập quá hạn hoặc đến hạn trong 7 ngày tới.");
    }

    /** Vòng tiến độ được tính từ bài tập thật trong database. */
    private static final class ProgressRing extends JComponent {
        private static final long serialVersionUID = 1L;
        private int completed;
        private int total;
        ProgressRing() { setPreferredSize(new Dimension(182, 112)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D p = (Graphics2D) g.create();
            p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int diameter = 80, x = (getWidth() - diameter) / 2, y = 0;
            p.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            p.setColor(new Color(0xD5E3DA)); p.drawOval(x, y + 5, diameter, diameter);
            int percent = total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);
            p.setColor(AppTheme.PRIMARY); p.drawArc(x, y + 5, diameter, diameter, 90, -(int) Math.round(percent * 3.6));
            p.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
            String text = percent + "%";
            p.drawString(text, (getWidth() - p.getFontMetrics().stringWidth(text)) / 2, 52);
            p.setColor(new Color(0x617D71)); p.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            String caption = total == 0 ? "Chưa có bài tập" : completed + "/" + total + " bài tập hoàn thành";
            p.drawString(caption, (getWidth() - p.getFontMetrics().stringWidth(caption)) / 2, 109);
            p.dispose();
        }
    }
}
