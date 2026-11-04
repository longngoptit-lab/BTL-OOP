package qlht.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import qlht.db.DataAccessException;

/** Hộp thoại sửa một đối tượng, dùng lại EntityForm. */
public class EditDialog<T> extends JDialog {
    private static final long serialVersionUID = 1L;

    /** Lưu đối tượng. Trả về true để đóng hộp thoại, false để giữ lại (ví dụ người dùng từ chối cảnh báo). */
    @FunctionalInterface
    public interface Saver<T> {
        boolean save(T entity, Component dialog);
    }

    public EditDialog(Window owner, String title, EntityForm<T> form, Saver<T> saver) {
        super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);

        JButton save = new JButton("Lưu thay đổi");
        AppTheme.primary(save);
        JButton cancel = new JButton("Hủy");
        save.addActionListener(e -> {
            try {
                T entity = form.read(this);
                if (entity == null) return;
                if (saver.save(entity, this)) dispose();
            } catch (qlht.model.ValidationException ex) {
                UiUtil.warn(this, "Dữ liệu chưa hợp lệ", ex.getMessage());
            } catch (DataAccessException ex) {
                UiUtil.dbError(this, ex);
            }
        });
        cancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        buttons.add(save);
        buttons.add(cancel);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(22, 24, 8, 24));
        JPanel card = new AppTheme.Card(new BorderLayout(0, 16));
        card.add(AppTheme.label(title, 19, java.awt.Font.BOLD, AppTheme.TEXT), BorderLayout.NORTH);
        card.add(form.component(), BorderLayout.CENTER);
        content.add(card, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);

        getRootPane().setDefaultButton(save);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setLocationRelativeTo(owner);
    }
}
