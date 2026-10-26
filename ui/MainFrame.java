package qlht.ui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import qlht.db.Database;

/** Cửa sổ chính: 6 tab, mỗi tab là một TabPanel. */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final transient List<TabPanel> panels = new ArrayList<>();

    public MainFrame(Database db) {
        super("Phần mềm quản lí học tập cá nhân");
        AppContext ctx = new AppContext(db);
        ctx.setRefreshAll(this::refreshAll);

        JTabbedPane tabs = new JTabbedPane();
        addTab(tabs, "  Tổng quan  ", new DashboardPanel(ctx));
        addTab(tabs, "  Môn học  ", new MonHocPanel(ctx));
        addTab(tabs, "  Thời khóa biểu  ", new ThoiKhoaBieuPanel(ctx));
        addTab(tabs, "  Lịch thi  ", new LichThiPanel(ctx));
        addTab(tabs, "  Bài tập / Deadline  ", new BaiTapPanel(ctx));
        addTab(tabs, "  Ghi chú  ", new GhiChuPanel(ctx));
        getContentPane().add(tabs);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                db.close();
            }
        });
        setSize(1000, 700);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);
        refreshAll();
    }

    private void addTab(JTabbedPane tabs, String title, TabPanel panel) {
        tabs.addTab(title, panel);
        panels.add(panel);
    }

    /** Nạp lại dữ liệu của tất cả các tab. Lỗi CSDL được báo bằng hộp thoại. */
    public void refreshAll() {
        UiUtil.safe(this, () -> panels.forEach(TabPanel::refresh));
    }
}
