package qlht;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import qlht.db.DataAccessException;
import qlht.db.Database;
import qlht.db.DbErrorMessages;
import qlht.ui.MainFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // dùng giao diện mặc định
            }
            UIManager.put("OptionPane.yesButtonText", "Có");
            UIManager.put("OptionPane.noButtonText", "Không");
            UIManager.put("OptionPane.cancelButtonText", "Hủy");

            Database db;
            try {
                db = new Database(Database.defaultPath());
            } catch (DataAccessException e) {
                String[] d = DbErrorMessages.describe(e);
                JOptionPane.showMessageDialog(null, d[1] + "\n\nỨng dụng sẽ đóng lại.", d[0],
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }
            new MainFrame(db).setVisible(true);
        });
    }
}
