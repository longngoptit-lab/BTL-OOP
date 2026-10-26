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
        JButton cancel = new JButton("Hủy");
        save.addActionListener(e -> {
            T entity = form.read(this);
            if (entity == null) return;
            try {
                if (saver.save(entity, this)) dispose();
            } catch (DataAccessException ex) {
                UiUtil.dbError(this, ex);
            }
        });
        cancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        buttons.add(save);
        buttons.add(cancel);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(12, 12, 4, 12));
        content.add(form.component(), BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);

        getRootPane().setDefaultButton(save);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setLocationRelativeTo(owner);
    }
}
