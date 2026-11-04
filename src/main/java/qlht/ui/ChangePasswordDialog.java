package qlht.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import qlht.db.DataAccessException;
import qlht.db.Database;
import qlht.model.User;
import qlht.service.AuthException;
import qlht.service.AuthService;

/** Đổi mật khẩu khi đã đăng nhập, cần xác nhận mật khẩu hiện tại. */
public final class ChangePasswordDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final JPasswordField current = new JPasswordField(22);
    private final JPasswordField password = new JPasswordField(22);
    private final JPasswordField confirmation = new JPasswordField(22);
    private final JLabel feedback = AppTheme.label(" ", 12, Font.PLAIN, AppTheme.DANGER);
    private final JButton save = AppTheme.primary(new JButton("Lưu mật khẩu"));
    private final JButton cancel = new JButton("Hủy");
    private boolean busy;

    public ChangePasswordDialog(Window owner, Database db, User user) {
        super(owner, "Đổi mật khẩu", ModalityType.APPLICATION_MODAL);
        FormPanel form = new FormPanel(1);
        current.setName("password.current"); password.setName("password.new"); confirmation.setName("password.confirmation");
        form.addField("Mật khẩu hiện tại", current);
        form.addField("Mật khẩu mới", password);
        form.addField("Nhập lại mật khẩu mới", confirmation);
        AppTheme.Card card = new AppTheme.Card(new BorderLayout(0, 14));
        card.add(AppTheme.label("Đổi mật khẩu", 21, Font.BOLD, AppTheme.TEXT), BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        JPanel bottom = AppTheme.transparent(new BorderLayout(0, 12));
        feedback.setPreferredSize(new Dimension(410, 40));
        bottom.add(feedback, BorderLayout.CENTER);
        JPanel buttons = AppTheme.transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(cancel); buttons.add(save);
        bottom.add(buttons, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(20, 20, 20, 20)); root.add(card);
        setContentPane(root);
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> change(new AuthService(db), user));
        getRootPane().setDefaultButton(save);
        getRootPane().registerKeyboardAction(e -> { if (!busy) dispose(); }, KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { if (!busy) dispose(); }
        });
        pack(); setResizable(false); setLocationRelativeTo(owner);
    }

    private void change(AuthService auth, User user) {
        if (busy) return;
        char[] old = current.getPassword(), secret = password.getPassword(), repeat = confirmation.getPassword();
        setBusy(true); feedback.setText(" ");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try { auth.changePassword(user, old, secret, repeat); return null; }
                finally { Arrays.fill(old, '\0'); Arrays.fill(secret, '\0'); Arrays.fill(repeat, '\0'); }
            }
            @Override protected void done() {
                setBusy(false);
                current.setText(""); password.setText(""); confirmation.setText("");
                try {
                    get(); dispose(); UiUtil.info(getOwner(), "Đổi mật khẩu", "Đã cập nhật mật khẩu của bạn.");
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                catch (ExecutionException e) {
                    if (e.getCause() instanceof AuthException error)
                        feedback.setText("<html>" + error.getMessage() + "</html>");
                    else if (e.getCause() instanceof DataAccessException error) UiUtil.dbError(ChangePasswordDialog.this, error);
                    else feedback.setText("Không đổi được mật khẩu. Vui lòng thử lại.");
                }
            }
        }.execute();
    }

    private void setBusy(boolean value) {
        busy = value;
        for (JComponent c : new JComponent[]{current, password, confirmation, save, cancel}) c.setEnabled(!value);
        save.setText(value ? "Đang lưu…" : "Lưu mật khẩu");
    }

    @Override public void dispose() {
        current.setText(""); password.setText(""); confirmation.setText(""); super.dispose();
    }
}
