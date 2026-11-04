package qlht;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import qlht.db.DataAccessException;
import qlht.db.Database;
import qlht.db.DbErrorMessages;
import qlht.ui.MainFrame;

public class Main {
    public static void main(String[] args) {
        if (args.length == 1 && "--seed-demo".equals(args[0])) {
            try { qlht.tools.DemoData.run(); }
            catch (Exception e) { System.err.println(e.getMessage()); System.exit(1); }
            return;
        }
        if (args.length == 1 && "--check-db".equals(args[0])) {
            try (Database db = new Database();
                 var st = db.connection().createStatement();
                 var rs = st.executeQuery("SELECT DB_NAME()")) {
                rs.next();
                System.out.println("Kết nối SQL Server thành công: " + rs.getString(1));
            } catch (Exception e) {
                System.err.println(DbErrorMessages.describe(e)[1]);
                System.exit(1);
            }
            return;
        }
        SwingUtilities.invokeLater(() -> {
            qlht.ui.AppTheme.install();

            Database db;
            try {
                db = new Database();
            } catch (DataAccessException e) {
                String[] d = DbErrorMessages.describe(e);
                JOptionPane.showMessageDialog(null, d[1] + "\n\nỨng dụng sẽ đóng lại.", d[0],
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }
            showLogin(db);
        });
    }

    private static void showLogin(Database db) {
        new qlht.ui.AuthFrame(db, user ->
                new MainFrame(db, user, () -> showLogin(db)).setVisible(true)).setVisible(true);
    }
}
