package qlht.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import qlht.model.BaiTap;
import qlht.util.DateUtil;

public class DashboardPanel extends TabPanel {
    private static final long serialVersionUID = 1L;
    private final JLabel cardMon = bigLabel();
    private final JLabel cardBaiTap = bigLabel();
    private final JLabel cardThi = bigLabel();
    private final JLabel cardGhiChu = bigLabel();
    private final RowTable table = new RowTable(
            new String[]{"Môn học", "Tiêu đề", "Hạn nộp", "Ưu tiên"}, new int[]{180, 280, 210, 100});

    public DashboardPanel(AppContext ctx) {
        super(ctx);

        JLabel header = new JLabel("Tổng quan học tập");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));

        JPanel cards = new JPanel(new GridLayout(1, 4, 10, 0));
        cards.add(card(cardMon, "Số môn học"));
        cards.add(card(cardBaiTap, "Bài tập chưa xong"));
        cards.add(card(cardThi, "Lịch thi sắp tới"));
        cards.add(card(cardGhiChu, "Ghi chú"));

        JLabel sub = new JLabel("⏰ Deadline: quá hạn và 7 ngày tới");
        sub.setFont(sub.getFont().deriveFont(Font.BOLD, 15f));
        sub.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        header.setAlignmentX(LEFT_ALIGNMENT);
        cards.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        top.add(header);
        top.add(javax.swing.Box.createVerticalStrut(8));
        top.add(cards);
        top.add(sub);
        add(top, BorderLayout.NORTH);

        add(new javax.swing.JScrollPane(table), BorderLayout.CENTER);

        JButton btn = new JButton("Làm mới");
        btn.addActionListener(e -> ctx.refreshAll());
        add(buttonsRight(btn), BorderLayout.SOUTH);
    }

    private static JLabel bigLabel() {
        JLabel l = new JLabel("0", SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 26f));
        return l;
    }

    private static JPanel card(JLabel value, String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), new EmptyBorder(12, 12, 12, 12)));
        p.add(value, BorderLayout.CENTER);
        p.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.SOUTH);
        return p;
    }

    @Override
    public void refresh() {
        LocalDate today = LocalDate.now();
        List<BaiTap> chuaXong = ctx.baiTapDao.layTatCa(true);

        long sapToi = ctx.lichThiDao.layTatCa().stream()
                .filter(l -> l.getNgayThi() != null && !l.getNgayThi().isBefore(today)).count();

        cardMon.setText(String.valueOf(ctx.monHocDao.layTatCa().size()));
        cardBaiTap.setText(String.valueOf(chuaXong.size()));
        cardThi.setText(String.valueOf(sapToi));
        cardGhiChu.setText(String.valueOf(ctx.ghiChuDao.layTatCa().size()));

        List<RowTable.Row> rows = new ArrayList<>();
        for (BaiTap b : chuaXong) {
            if (b.getHanNop() == null) continue;
            long soNgay = ChronoUnit.DAYS.between(today, b.getHanNop());
            if (soNgay > 7) continue;
            String han = DateUtil.formatVn(b.getHanNop());
            RowTable.Style s = RowTable.Style.NONE;
            if (soNgay < 0) {
                han += " (quá hạn " + (-soNgay) + " ngày)";
                s = RowTable.Style.CONFLICT;
            }
            rows.add(new RowTable.Row(b.getId(), s,
                    b.getTenMon() == null ? "(Không gắn môn)" : b.getTenMon(),
                    b.getTieuDe(), han, b.getMucDoUuTien()));
        }
        table.setRows(rows);
    }
}
