package qlht.ui;

import java.util.List;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import qlht.model.MonHoc;

/** Ô chọn môn học. allowNone = cho phép chọn "(Không gắn môn)". */
public class MonCombo extends JComboBox<MonCombo.Item> {
    private static final long serialVersionUID = 1L;

    public static final class Item {
        private final Integer id;
        private final String text;

        Item(Integer id, String text) {
            this.id = id;
            this.text = text;
        }

        @Override
        public String toString() { return text; }
    }

    private final boolean allowNone;

    public MonCombo(boolean allowNone) {
        this.allowNone = allowNone;
        setPrototypeDisplayValue(new Item(0, "0000 - Tên môn học khá dài ở đây"));
        reload(List.of());
    }

    /** Nạp lại danh sách môn, cố giữ môn đang chọn (môn đã bị xóa thì bỏ chọn). */
    public void reload(List<MonHoc> list) {
        Integer keep = getMonId();
        DefaultComboBoxModel<Item> m = new DefaultComboBoxModel<>();
        if (allowNone) m.addElement(new Item(null, "(Không gắn môn)"));
        for (MonHoc mh : list) m.addElement(new Item(mh.getId(), mh.toString()));
        setModel(m);
        setMonId(keep);
    }

    /** ID môn đang chọn; null nếu chưa chọn (hoặc chọn "không gắn môn"). */
    public Integer getMonId() {
        Item it = (Item) getSelectedItem();
        return it == null ? null : it.id;
    }

    public void setMonId(Integer id) {
        for (int i = 0; i < getItemCount(); i++) {
            if (Objects.equals(getItemAt(i).id, id)) {
                setSelectedIndex(i);
                return;
            }
        }
        setSelectedIndex(allowNone ? 0 : -1);
    }
}
