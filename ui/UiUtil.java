package qlht.ui;

import java.awt.Component;
import javax.swing.JOptionPane;
import qlht.db.DataAccessException;
import qlht.db.DbErrorMessages;

public final class UiUtil {
    private UiUtil() {}

    public static void info(Component parent, String title, String msg) {
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(Component parent, String title, String msg) {
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component parent, String title, String msg) {
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String title, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static boolean confirmWarn(Component parent, String title, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void dbError(Component parent, DataAccessException e) {
        String[] d = DbErrorMessages.describe(e);
        error(parent, d[0], d[1]);
    }

    /** Chạy một thao tác; lỗi CSDL được báo bằng hộp thoại thay vì làm văng ứng dụng. */
    public static void safe(Component parent, Runnable action) {
        try {
            action.run();
        } catch (DataAccessException e) {
            dbError(parent, e);
        }
    }
}
