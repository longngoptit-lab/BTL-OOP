package qlht.tests;

import java.awt.*;
import java.time.LocalDate;
import java.util.Arrays;
import javax.swing.*;
import qlht.model.*;
import qlht.ui.MonHocForm;
import qlht.util.Const;

/** Kiểm tra quy tắc nghiệp vụ không cần SQL Server. */
public final class DomainValidationTest {
    private static int checks;
    private static final LocalDate DATE = LocalDate.of(2026, 11, 20);
    private static void check(boolean valid, String message) {
        checks++; if (!valid) throw new AssertionError(message);
    }
    private static void reject(Entity entity) {
        try { entity.validate(); }
        catch (ValidationException expected) { check(!expected.getMessage().isBlank(), "Useful validation message"); return; }
        throw new AssertionError("Accepted invalid " + entity.getClass().getSimpleName());
    }
    public static void main(String[] args) throws Exception {
        Entity[] valid = {
            new MonHoc("Lập trình Java", 3, "Nguyễn Văn A", ""),
            new LichHoc(1, null, "Thứ 2", 1, 12, "A1"),
            new LichThi(1, null, DATE, "23:59", "", "Tự luận"),
            new BaiTap(null, null, "Bài tập", "", DATE, "Cao", Const.STATUS_TODO),
            new GhiChu(null, null, "Ghi chú", "", DATE)
        };
        for (Entity entity : valid) { entity.validate(); check(entity.isNew(), "Polymorphic validation and new entity"); }
        reject(new MonHoc("", 3, "", "")); reject(new MonHoc(null, 3, "", ""));
        reject(new MonHoc(" ", 3, "", ""));
        for (int credits : new int[]{-1, 0, 11}) reject(new MonHoc("A", credits, "", ""));
        reject(new MonHoc("a".repeat(256), 3, "", ""));
        reject(new MonHoc("A", 3, "a".repeat(256), ""));
        new MonHoc("a".repeat(255), 1, "", "").validate(); check(true, "255-character name and minimum credits accepted");
        new MonHoc("A", 10, "", "").validate(); check(true, "Maximum credits accepted");
        reject(new LichHoc(0, null, "Thứ 2", 1, 3, ""));
        reject(new LichHoc(1, null, null, 1, 3, ""));
        reject(new LichHoc(1, null, "Unknown", 1, 3, ""));
        reject(new LichHoc(1, null, "Thứ 2", 0, 3, ""));
        reject(new LichHoc(1, null, "Thứ 2", 4, 3, ""));
        reject(new LichHoc(1, null, "Thứ 2", 1, 13, ""));
        reject(new LichHoc(1, null, "Thứ 2", 1, 3, "a".repeat(256)));
        reject(new LichThi(0, null, DATE, "08:00", "", ""));
        reject(new LichThi(1, null, null, "08:00", "", ""));
        reject(new LichThi(1, null, DATE, "24:00", "", ""));
        reject(new LichThi(1, null, DATE, "12:60", "", ""));
        reject(new LichThi(1, null, DATE, "old text", "", ""));
        reject(new LichThi(1, null, DATE, "08:00", "", "a".repeat(256)));
        reject(new BaiTap(null, null, " ", "", DATE, "Cao", Const.STATUS_TODO));
        reject(new BaiTap(-1, null, "A", "", DATE, "Cao", Const.STATUS_TODO));
        reject(new BaiTap(null, null, "A", "", null, "Cao", Const.STATUS_TODO));
        reject(new BaiTap(null, null, "A", "", DATE, "Unknown", Const.STATUS_TODO));
        reject(new BaiTap(null, null, "A", "", DATE, "Cao", "Unknown"));
        new BaiTap(null, null, "A", "", DATE, null, null).validate(); check(true, "Legacy null defaults normalized");
        reject(new GhiChu(null, null, "", "", DATE));
        reject(new GhiChu(0, null, "A", "", DATE));
        reject(new GhiChu(null, null, "a".repeat(256), "", DATE));
        try { valid[0].setId(-1); throw new AssertionError("Negative ID accepted"); }
        catch (ValidationException expected) { check(true, "Negative ID rejected"); }
        if (Arrays.asList(args).contains("--ui")) SwingUtilities.invokeAndWait(DomainValidationTest::reviewForm);
        System.out.println("PASS: " + checks + " domain validation checks.");
    }
    private static void reviewForm() {
        MonHocForm form = new MonHocForm(1);
        form.fill(new MonHoc("Java", 3, "", ""));
        JSpinner spinner = (JSpinner) named(form, "subject.credits");
        JFormattedTextField text = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        for (String invalid : new String[]{"abc", "0", "11"}) {
            text.setText(invalid);
            Timer closeMessage = new Timer(80, e -> {
                for (Window w : Window.getWindows()) if (w.isShowing()) closeOptions(w);
            });
            closeMessage.start();
            try { check(form.read(null) == null, "Invalid credits stop form submission: " + invalid); }
            finally { closeMessage.stop(); }
        }
        text.setText("10"); check(form.read(null).getSoTinChi() == 10, "Valid credits are preserved");
    }
    private static Component named(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName())) return c;
            if (c instanceof Container nested) { Component found = named(nested, name); if (found != null) return found; }
        }
        return null;
    }
    private static void closeOptions(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JOptionPane pane) pane.setValue(JOptionPane.OK_OPTION);
            if (c instanceof Container nested) closeOptions(nested);
        }
    }
}
