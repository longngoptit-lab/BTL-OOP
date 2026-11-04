package qlht.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qlht.db.DataAccessException;
import qlht.db.Database;
import qlht.model.User;
import qlht.service.AuthException;
import qlht.service.AuthService;

/** Màn hình đăng nhập / đăng ký. Tác vụ JDBC và băm mật khẩu chạy ngoài EDT. */
public final class AuthFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final AuthService auth;
    private final Consumer<User> onAuthenticated;
    private final JTextField displayName = new JTextField();
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JPasswordField confirmation = new JPasswordField();
    private final JLabel heading = AppTheme.label("", 27, Font.BOLD, AppTheme.TEXT);
    private final JLabel subtitle = AppTheme.label("", 12, Font.PLAIN, AppTheme.MUTED);
    private final JLabel feedback = AppTheme.label(" ", 12, Font.PLAIN, AppTheme.DANGER);
    private final JButton submit = AppTheme.primary(new JButton());
    private final JButton switchMode = new JButton();
    private final JCheckBox showPassword = new JCheckBox("Hiện mật khẩu");
    private final JPanel nameBlock;
    private final JPanel confirmationBlock;
    private boolean registering;
    private boolean busy;
    private final char echo;

    public AuthFrame(Database db, Consumer<User> onAuthenticated) {
        super("StudySpace · Đăng nhập");
        this.auth = new AuthService(db);
        this.onAuthenticated = onAuthenticated;
        echo = password.getEchoChar();
        username.setName("auth.username");
        displayName.setName("auth.displayName");
        password.setName("auth.password");
        confirmation.setName("auth.confirmation");
        username.putClientProperty("JTextField.placeholderText", "Tên đăng nhập của bạn");
        displayName.putClientProperty("JTextField.placeholderText", "Bạn muốn được gọi là gì?");
        password.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu");
        confirmation.putClientProperty("JTextField.placeholderText", "Nhập lại mật khẩu");
        displayName.putClientProperty("JTextField.selectAllOnFocusPolicy", "never");
        JPanel root = new JPanel(new BorderLayout());
        root.add(introduction(), BorderLayout.WEST);
        JPanel right = new JPanel(new GridBagLayout());
        right.setBorder(new EmptyBorder(24, 34, 24, 34));
        AppTheme.Card card = new AppTheme.Card(new BorderLayout(0, 24));
        card.setBorder(new EmptyBorder(28, 28, 24, 28));
        card.setPreferredSize(new Dimension(440, 620));
        JPanel titles = AppTheme.transparent(new BorderLayout(0, 10));
        titles.add(heading, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.SOUTH);
        card.add(titles, BorderLayout.NORTH);
        JPanel form = AppTheme.transparent(null);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        nameBlock = field("Tên hiển thị", displayName);
        confirmationBlock = field("Nhập lại mật khẩu", confirmation);
        form.add(nameBlock);
        form.add(field("Tên đăng nhập", username));
        form.add(field("Mật khẩu", password));
        form.add(confirmationBlock);
        showPassword.setOpaque(false);
        showPassword.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        showPassword.setForeground(AppTheme.MUTED);
        showPassword.setAlignmentX(LEFT_ALIGNMENT);
        showPassword.addActionListener(e -> {
            password.setEchoChar(showPassword.isSelected() ? (char) 0 : echo);
            confirmation.setEchoChar(showPassword.isSelected() ? (char) 0 : echo);
        });
        form.add(showPassword);
        form.add(Box.createVerticalGlue());
        feedback.setAlignmentX(LEFT_ALIGNMENT);
        feedback.setPreferredSize(new Dimension(370, 52));
        feedback.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        form.add(feedback);
        submit.setAlignmentX(LEFT_ALIGNMENT);
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        submit.setPreferredSize(new Dimension(370, 42));
        submit.addActionListener(e -> authenticate());
        form.add(submit);
        card.add(form, BorderLayout.CENTER);
        JPanel bottom = AppTheme.transparent(new BorderLayout(0, 12));
        JLabel help = AppTheme.label("Mật khẩu khi đăng ký: từ 8 đến 128 ký tự.", 11, Font.PLAIN, AppTheme.MUTED);
        bottom.add(help, BorderLayout.NORTH);
        switchMode.addActionListener(e -> setRegistering(!registering));
        bottom.add(switchMode, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);
        right.add(card);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);
        getRootPane().setDefaultButton(submit);
        setRegistering(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (!busy) { clearSecrets(); db.close(); dispose(); }
            }
        });
        setMinimumSize(new Dimension(980, 740));
        setSize(1040, 780);
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(username::requestFocusInWindow);
    }

    private JPanel introduction() {
        JPanel panel = new JPanel(new BorderLayout(0, 40));
        panel.setBackground(AppTheme.NAVY);
        panel.setPreferredSize(new Dimension(420, 0));
        panel.setBorder(new EmptyBorder(42, 36, 38, 36));
        panel.add(AppTheme.label("StudySpace", 28, Font.BOLD, Color.WHITE), BorderLayout.NORTH);
        JPanel copy = AppTheme.transparent(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 24, 0);
        c.gridy = 0;
        copy.add(AppTheme.label("KHÔNG GIAN HỌC TẬP CÁ NHÂN", 11, Font.BOLD, new Color(0xA5DDBE)), c);
        c.gridy++;
        copy.add(AppTheme.label("<html>Kế hoạch rõ ràng.<br>Học tập chủ động.</html>", 32, Font.BOLD, Color.WHITE), c);
        c.gridy++;
        copy.add(AppTheme.label("<html>Môn học, lịch thi và những ý tưởng của bạn.<br>Tất cả gọn gàng trong một không gian riêng.</html>",
                13, Font.PLAIN, new Color(0xACC2C2)), c);
        String[] features = {"Theo dõi lịch học & deadline", "Ghi lại kiến thức mỗi ngày", "Dữ liệu riêng cho từng tài khoản"};
        for (String text : features) {
            c.gridy++; c.insets = new Insets(0, 0, 18, 0);
            JLabel item = AppTheme.label(text, 13, Font.PLAIN, new Color(0xD9E8E2));
            item.setIcon(AppTheme.icon(6, new Color(0xA5DDBE))); item.setIconTextGap(10);
            copy.add(item, c);
        }
        panel.add(copy, BorderLayout.CENTER);
        panel.add(AppTheme.label("Mỗi ngày, tiến thêm một bước.", 12, Font.PLAIN, new Color(0x8EADAE)), BorderLayout.SOUTH);
        return panel;
    }

    private static JPanel field(String text, JTextField input) {
        JPanel block = AppTheme.transparent(new BorderLayout(0, 7));
        JLabel label = AppTheme.label(text, 12, Font.BOLD, AppTheme.TEXT);
        label.setLabelFor(input);
        block.add(label, BorderLayout.NORTH);
        input.setPreferredSize(new Dimension(360, 38));
        block.add(input, BorderLayout.CENTER);
        block.setBorder(new EmptyBorder(0, 0, 16, 0));
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        block.setAlignmentX(LEFT_ALIGNMENT);
        return block;
    }

    private void setRegistering(boolean value) {
        registering = value;
        heading.setText(value ? "Tạo tài khoản mới" : "Chào mừng trở lại.");
        subtitle.setText(value ? "Bắt đầu kế hoạch học tập của riêng bạn." : "Đăng nhập để tiếp tục kế hoạch hôm nay.");
        nameBlock.setVisible(value);
        confirmationBlock.setVisible(value);
        submit.setText(value ? "Tạo tài khoản" : "Đăng nhập");
        switchMode.setText(value ? "Đã có tài khoản? Đăng nhập" : "Chưa có tài khoản? Đăng ký");
        clearSecrets();
        feedback.setText(" ");
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void clearSecrets() {
        password.setText(""); confirmation.setText("");
        showPassword.setSelected(false);
        password.setEchoChar(echo); confirmation.setEchoChar(echo);
    }

    private void setBusy(boolean value) {
        busy = value;
        for (JComponent c : new JComponent[]{displayName, username, password, confirmation, showPassword, submit, switchMode})
            c.setEnabled(!value);
        submit.setText(value ? "Đang xử lý…" : registering ? "Tạo tài khoản" : "Đăng nhập");
        setCursor(Cursor.getPredefinedCursor(value ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private void authenticate() {
        if (busy) return;
        char[] secret = password.getPassword();
        char[] repeat = confirmation.getPassword();
        String login = username.getText();
        String name = displayName.getText();
        boolean create = registering;
        feedback.setText(" "); setBusy(true);
        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() {
                try { return create ? auth.register(name, login, secret, repeat) : auth.login(login, secret); }
                finally { Arrays.fill(secret, '\0'); Arrays.fill(repeat, '\0'); }
            }
            @Override protected void done() {
                setBusy(false);
                clearSecrets();
                try {
                    User user = get();
                    onAuthenticated.accept(user);
                    dispose();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof AuthException) showFeedback(cause.getMessage());
                    else if (cause instanceof DataAccessException error) UiUtil.dbError(AuthFrame.this, error);
                    else showFeedback("Không hoàn tất được thao tác. Vui lòng thử lại.");
                    password.requestFocusInWindow();
                }
            }
        }.execute();
    }

    private void showFeedback(String message) {
        feedback.setText("<html><body style='width:270px'>" + message + "</body></html>");
    }
}
