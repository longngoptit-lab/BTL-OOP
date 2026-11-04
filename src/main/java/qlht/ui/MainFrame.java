package qlht.ui;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qlht.db.Database;

/** Cửa sổ chính với sidebar và sáu màn hình học tập. */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final transient List<TabPanel> panels = new ArrayList<>();
    private final List<JToggleButton> navigation = new ArrayList<>();
    private final CardLayout pages = new CardLayout();
    private final JPanel content = new JPanel(pages);
    private final JLabel pageTitle = AppTheme.label("Tổng quan", 24, Font.BOLD, AppTheme.TEXT);
    private final JLabel pageDescription = AppTheme.label("", 12, Font.PLAIN, AppTheme.MUTED);
    private static final String[] TITLES = {"Tổng quan", "Môn học", "Thời khóa biểu", "Lịch thi", "Bài tập", "Ghi chú"};
    private static final String[] DESCRIPTIONS = {
        "Một góc nhìn rõ ràng cho hành trình học tập của bạn.",
        "Sắp xếp môn học, tín chỉ và thông tin giảng viên.",
        "Chủ động từng buổi học, dễ dàng nhận biết lịch trùng.",
        "Theo dõi kỳ thi để luôn sẵn sàng trước ngày quan trọng.",
        "Quản lý deadline và tiến độ, hoàn thành từng mục tiêu nhỏ.",
        "Lưu lại ý tưởng và những điều bạn muốn ghi nhớ."
    };

    private final qlht.model.User user;
    private final Database db;
    private final Runnable onLogout;

    public MainFrame(Database db, qlht.model.User user, Runnable onLogout) {
        super("StudySpace · Quản lý học tập cá nhân");
        this.db = java.util.Objects.requireNonNull(db);
        this.user = java.util.Objects.requireNonNull(user);
        this.onLogout = java.util.Objects.requireNonNull(onLogout);
        AppContext ctx = new AppContext(db, user);
        ctx.setRefreshAll(this::refreshAll);
        JPanel root = new JPanel(new BorderLayout());
        root.add(sidebar(), BorderLayout.WEST);
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.add(header(), BorderLayout.NORTH);
        addPage(new DashboardPanel(ctx));
        addPage(new MonHocPanel(ctx));
        addPage(new ThoiKhoaBieuPanel(ctx));
        addPage(new LichThiPanel(ctx));
        addPage(new BaiTapPanel(ctx));
        addPage(new GhiChuPanel(ctx));
        workspace.add(content, BorderLayout.CENTER);
        root.add(workspace, BorderLayout.CENTER);
        setContentPane(root);
        selectPage(0);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { db.close(); }
        });
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(1100, 780));
        setSize(Math.min(1380, screen.width - 40), Math.min(920, screen.height - 60));
        setLocationRelativeTo(null);
        refreshAll();
    }

    private JPanel sidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(AppTheme.NAVY);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(30, 18, 24, 18));
        JPanel upper = AppTheme.transparent(new BorderLayout(0, 28));
        JPanel brand = AppTheme.transparent(new BorderLayout(10, 5));
        JLabel logo = AppTheme.label("S", 23, Font.BOLD, new Color(0xB9E2CE));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(38, 40));
        brand.add(logo, BorderLayout.WEST);
        JPanel brandText = AppTheme.transparent(new GridLayout(2, 1));
        brandText.add(AppTheme.label("StudySpace", 21, Font.BOLD, Color.WHITE));
        brandText.add(AppTheme.label("Không gian học tập của bạn", 10, Font.PLAIN, new Color(0x9CB3B6)));
        brand.add(brandText, BorderLayout.CENTER);
        upper.add(brand, BorderLayout.NORTH);
        JPanel menu = AppTheme.transparent(null);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        JLabel caption = AppTheme.label("KHÔNG GIAN CỦA BẠN", 10, Font.BOLD, new Color(0x8DA9AB));
        caption.setBorder(new EmptyBorder(0, 12, 4, 0));
        caption.setAlignmentX(Component.LEFT_ALIGNMENT);
        caption.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        menu.add(caption);
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < TITLES.length; i++) {
            final int index = i;
            JToggleButton button = new JToggleButton(TITLES[i]) {
                private static final long serialVersionUID = 1L;
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D p = (Graphics2D) g.create();
                    p.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isSelected() || getModel().isRollover()) {
                        p.setColor(isSelected() ? new Color(0x2C494C) : new Color(0x203940));
                        p.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    }
                    if (isSelected()) {
                        p.setColor(new Color(0xA5DDBE));
                        p.fillRoundRect(0, 14, 3, getHeight() - 28, 3, 3);
                    }
                    p.dispose();
                    super.paintComponent(g);
                }
            };
            button.setName("navigation." + i);
            button.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI());
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorder(new EmptyBorder(15, 14, 15, 10));
            button.setFocusPainted(false);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setIconTextGap(14);
            button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            button.setRolloverEnabled(true);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> selectPage(index));
            // Giữ dấu focus cho điều hướng bằng Tab.
            button.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xA5DDBE)), new EmptyBorder(14, 13, 14, 9))); }
                @Override public void focusLost(FocusEvent e) { button.setBorder(new EmptyBorder(15, 14, 15, 10)); }
            });
            group.add(button);
            navigation.add(button);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setPreferredSize(new Dimension(184, 44));
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            menu.add(button);
            if (i < TITLES.length - 1) menu.add(Box.createVerticalStrut(6));
        }
        upper.add(menu, BorderLayout.CENTER);
        sidebar.add(upper, BorderLayout.NORTH);
        JPanel footer = AppTheme.transparent(new BorderLayout(0, 12));
        JPanel account = AppTheme.transparent(new GridLayout(0, 1, 0, 7));
        account.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                new Color(0x30484D)), new EmptyBorder(16, 4, 0, 4)));
        JLabel name = AppTheme.label(user.displayName(), 13, Font.BOLD, Color.WHITE);
        name.putClientProperty("html.disable", true);
        name.setToolTipText(user.username());
        account.add(name);
        JLabel username = AppTheme.label("@" + user.username(), 11, Font.PLAIN, new Color(0x9CB3B6));
        account.add(username);
        JButton change = new JButton("Đổi mật khẩu");
        change.addActionListener(e -> new ChangePasswordDialog(this, db, user).setVisible(true));
        account.add(change);
        JButton logout = new JButton("Đăng xuất");
        logout.addActionListener(e -> { onLogout.run(); dispose(); });
        account.add(logout);
        footer.add(account, BorderLayout.SOUTH);
        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel header() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(AppTheme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.LINE), new EmptyBorder(22, 28, 22, 28)));
        JPanel text = AppTheme.transparent(new BorderLayout(0, 6));
        text.add(pageTitle, BorderLayout.NORTH);
        text.add(pageDescription, BorderLayout.SOUTH);
        header.add(text, BorderLayout.CENTER);
        JPanel right = AppTheme.transparent(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        JLabel date = AppTheme.label(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM, yyyy", Locale.forLanguageTag("vi-VN"))),
                12, Font.PLAIN, AppTheme.MUTED);
        date.setIcon(AppTheme.icon(2, AppTheme.MUTED));
        date.setIconTextGap(8);
        right.add(date);
        JButton refresh = new JButton("Làm mới");
        refresh.setToolTipText("Cập nhật dữ liệu ở tất cả màn hình");
        refresh.addActionListener(e -> refreshAll());
        right.add(refresh);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void addPage(TabPanel panel) {
        content.add(panel, String.valueOf(panels.size()));
        panels.add(panel);
    }

    private void selectPage(int index) {
        pages.show(content, String.valueOf(index));
        pageTitle.setText(TITLES[index]);
        pageDescription.setText(DESCRIPTIONS[index]);
        for (int i = 0; i < navigation.size(); i++) {
            JToggleButton button = navigation.get(i);
            boolean selected = i == index;
            button.setSelected(selected);
            button.setForeground(selected ? Color.WHITE : new Color(0xA9BEC0));
            button.setIcon(AppTheme.icon(i, selected ? new Color(0xB5E5CB) : new Color(0x94B0B3)));
        }
    }

    public void refreshAll() {
        UiUtil.safe(this, () -> panels.forEach(TabPanel::refresh));
    }
}
