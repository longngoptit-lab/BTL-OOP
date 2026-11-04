package qlht.tests;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javax.swing.*;
import qlht.db.Database;
import qlht.ui.*;

/** Kiểm thử giao diện tùy chọn: ./test-auth.sh --ui (cần desktop). */
final class AuthUiTest {
    private static final AtomicReference<AuthFrame> LOGIN = new AtomicReference<>();
    private static final AtomicReference<MainFrame> MAIN = new AtomicReference<>();
    private static Path screenshots;

    static void run(Database db) throws Exception {
        screenshots = Files.createTempDirectory("qlht-auth-ui-");
        try {
            edt(() -> { AppTheme.install(); openLogin(db); });
            edt(() -> {
                LOGIN.get().setSize(980, 740); LOGIN.get().validate();
                save(LOGIN.get(), "login.png");
                field(LOGIN.get(), "auth.username").setText("alice");
                field(LOGIN.get(), "auth.password").setText("WrongPassword2026!");
                button(LOGIN.get(), "Đăng nhập").doClick();
            });
            await(() -> find(LOGIN.get(), JLabel.class).stream().anyMatch(l -> String.valueOf(l.getText()).contains("không đúng")));
            edt(() -> {
                if (MAIN.get() != null) throw new AssertionError("Bad credentials opened workspace");
                button(LOGIN.get(), "Chưa có tài khoản? Đăng ký").doClick(); LOGIN.get().validate();
                save(LOGIN.get(), "register.png");
                field(LOGIN.get(), "auth.displayName").setText("Nguyễn Minh");
                field(LOGIN.get(), "auth.username").setText("ui_user");
                field(LOGIN.get(), "auth.password").setText("UiPassword2026!");
                field(LOGIN.get(), "auth.confirmation").setText("UiPassword2026!");
                button(LOGIN.get(), "Tạo tài khoản").doClick();
            });
            await(() -> MAIN.get() != null && MAIN.get().isShowing() && !LOGIN.get().isShowing());
            MainFrame first = MAIN.get();
            edt(() -> {
                if (find(first, JLabel.class).stream().noneMatch(l -> "Nguyễn Minh".equals(l.getText())))
                    throw new AssertionError("Missing account display name");
                save(first, "workspace.png");
                java.util.List<JToggleButton> nav = find(first, JToggleButton.class).stream()
                        .filter(b -> b.getName() != null && b.getName().startsWith("navigation.")).toList();
                if (nav.size() != 6) throw new AssertionError("Navigation");
                for (JToggleButton item : nav) {
                    item.doClick(); first.validate();
                    for (RowTable table : find(first, RowTable.class)) {
                        if (table.getRowCount() != 0) throw new AssertionError("New account sees other account data");
                    }
                }
                button(first, "Đăng xuất").doClick();
                if (first.isShowing() || !LOGIN.get().isShowing()) throw new AssertionError("Logout did not replace workspace");
                if (((JPasswordField) field(LOGIN.get(), "auth.password")).getPassword().length != 0)
                    throw new AssertionError("Password retained after logout");
                signIn("UiPassword2026!");
            });
            await(() -> MAIN.get() != first && MAIN.get().isShowing() && !LOGIN.get().isShowing());
            SwingUtilities.invokeLater(() -> button(MAIN.get(), "Đổi mật khẩu").doClick());
            await(() -> visibleChangeDialog() != null);
            edt(() -> {
                ChangePasswordDialog dialog = visibleChangeDialog();
                save(dialog, "change-password.png");
                field(dialog, "password.current").setText("UiPassword2026!");
                field(dialog, "password.new").setText("UiChanged2026!");
                field(dialog, "password.confirmation").setText("UiChanged2026!");
                button(dialog, "Lưu mật khẩu").doClick();
            });
            await(() -> visibleChangeDialog() == null);
            await(() -> {
                for (Window w : Window.getWindows()) if (w.isShowing() && !find(w, JOptionPane.class).isEmpty()) return true;
                return false;
            });
            edt(() -> {
                for (Window w : Window.getWindows()) if (w.isShowing())
                    for (JOptionPane option : find(w, JOptionPane.class)) option.setValue(JOptionPane.OK_OPTION);
            });
            MainFrame second = MAIN.get();
            edt(() -> { button(second, "Đăng xuất").doClick(); signIn("UiChanged2026!"); });
            await(() -> MAIN.get() != second && MAIN.get().isShowing() && !LOGIN.get().isShowing());
            reviewSchedule(db);
            System.out.println("PASS: UI wrong login, registration, 6 pages, logout, re-login and password change; previews: " + screenshots);
        } finally {
            edt(() -> { for (Window w : Window.getWindows()) w.dispose(); });
        }
    }

    private static void reviewSchedule(Database db) throws Exception {
        qlht.model.User user = new qlht.service.AuthService(db).login("ui_user", "UiChanged2026!".toCharArray());
        AppContext ctx = new AppContext(db, user);
        int java = ctx.monHocDao.them(new qlht.model.MonHoc("Lập trình hướng đối tượng", 3, "Giảng viên A", ""));
        int sql = ctx.monHocDao.them(new qlht.model.MonHoc("Cơ sở dữ liệu", 3, "Giảng viên B", ""));
        int english = ctx.monHocDao.them(new qlht.model.MonHoc("Tiếng Anh", 2, "Giảng viên C", ""));
        int selected = ctx.lichHocDao.them(new qlht.model.LichHoc(java, null, "Thứ 2", 1, 3, "A2-301"));
        ctx.lichHocDao.them(new qlht.model.LichHoc(java, null, "Thứ 4", 2, 4, "A2-301"));
        ctx.lichHocDao.them(new qlht.model.LichHoc(sql, null, "Thứ 4", 1, 3, "B1-204"));
        ctx.lichHocDao.them(new qlht.model.LichHoc(sql, null, "Thứ 6", 5, 7, "B1-204"));
        ctx.lichHocDao.them(new qlht.model.LichHoc(english, null, "Thứ 3", 3, 4, "C1-105"));
        ctx.lichHocDao.them(new qlht.model.LichHoc(english, null, "Chủ nhật", 12, 12, "Thư viện"));
        edt(() -> {
            MainFrame main = MAIN.get(); main.refreshAll();
            find(main, JToggleButton.class).stream().filter(b -> "navigation.2".equals(b.getName())).findFirst().orElseThrow().doClick();
            main.validate();
            WeekScheduleView calendar = find(main, WeekScheduleView.class).get(0);
            if (calendar.displayedCount() != 6) throw new AssertionError("Weekly schedule did not load user data");
            save(main, "weekly-populated.png");
            calendar.select(selected);
            find(main, JToggleButton.class).stream().filter(b -> "Danh sách".equals(b.getText())).findFirst().orElseThrow().doClick();
            RowTable table = find(main, RowTable.class).stream().filter(Component::isShowing).findFirst().orElseThrow();
            if (!Integer.valueOf(selected).equals(table.selectedId())) throw new AssertionError("Selection lost switching to list");
            table.setRowSelectionInterval(1, 1); Integer other = table.selectedId();
            find(main, JToggleButton.class).stream().filter(b -> "Lịch tuần".equals(b.getText())).findFirst().orElseThrow().doClick();
            if (!other.equals(calendar.selectedId())) throw new AssertionError("Selection lost switching back to week");
            JComboBox<?> filter = find(main, JComboBox.class).stream().filter(c -> "schedule.subjectFilter".equals(c.getName())).findFirst().orElseThrow();
            filter.setSelectedIndex(1);
            if (calendar.displayedCount() != 2 || calendar.selectedId() != null) throw new AssertionError("Subject filter or hidden selection incorrect");
            filter.setSelectedIndex(0);
            if (calendar.displayedCount() != 6) throw new AssertionError("Reset subject filter");
            main.setSize(1100, 780); main.validate(); save(main, "weekly-minimum.png");
            calendar.select(selected);
        });
        SwingUtilities.invokeLater(() -> button(MAIN.get(), "Chỉnh sửa").doClick());
        await(() -> {
            for (Window w : Window.getWindows()) if (w instanceof EditDialog<?> && w.isShowing()) return true;
            return false;
        });
        edt(() -> {
            for (Window w : Window.getWindows()) if (w instanceof EditDialog<?> && w.isShowing()) {
                save((RootPaneContainer) w, "schedule-edit.png"); button(w, "Hủy").doClick();
            }
        });
        System.out.println("PASS: schedule weekly/list views, selection sync, subject filter, minimum window, editing dialog.");
    }

    private static void openLogin(Database db) {
        AuthFrame login = new AuthFrame(db, user -> {
            MainFrame main = new MainFrame(db, user, () -> openLogin(db));
            MAIN.set(main); main.setVisible(true);
        });
        LOGIN.set(login); login.setVisible(true);
    }

    private static void signIn(String password) {
        field(LOGIN.get(), "auth.username").setText("ui_user");
        field(LOGIN.get(), "auth.password").setText(password);
        button(LOGIN.get(), "Đăng nhập").doClick();
    }

    private static ChangePasswordDialog visibleChangeDialog() {
        for (Window w : Window.getWindows()) if (w instanceof ChangePasswordDialog d && d.isShowing()) return d;
        return null;
    }

    private static JTextField field(Container root, String name) {
        return find(root, JTextField.class).stream().filter(f -> name.equals(f.getName())).findFirst().orElseThrow();
    }

    private static JButton button(Container root, String text) {
        return find(root, JButton.class).stream().filter(b -> text.equals(b.getText()) && b.isShowing()).findFirst().orElseThrow();
    }

    private static <T> java.util.List<T> find(Container root, Class<T> type) {
        java.util.List<T> result = new ArrayList<>();
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) result.add(type.cast(c));
            if (c instanceof Container nested) result.addAll(find(nested, type));
        }
        return result;
    }

    private static void edt(Runnable action) throws Exception { SwingUtilities.invokeAndWait(action); }

    private static void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + 20_000_000_000L;
        while (System.nanoTime() < deadline) {
            AtomicReference<Boolean> ready = new AtomicReference<>(false);
            edt(() -> ready.set(condition.getAsBoolean()));
            if (ready.get()) return;
            Thread.sleep(50);
        }
        throw new AssertionError("Timed out waiting for UI");
    }

    private static void save(RootPaneContainer window, String name) {
        try {
            Container c = window.getContentPane();
            BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics(); c.printAll(g); g.dispose();
            ImageIO.write(image, "png", screenshots.resolve(name).toFile());
        } catch (Exception e) { throw new AssertionError(e); }
    }
}
