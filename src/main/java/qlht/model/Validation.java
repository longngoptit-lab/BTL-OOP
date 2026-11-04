package qlht.model;

import java.time.LocalDate;

/** Quy tắc dùng chung, không phụ thuộc giao diện hoặc database. */
final class Validation {
    private Validation() { }
    static void required(String value, int max, String label) {
        if (value == null || value.isBlank()) throw new ValidationException("Vui lòng nhập " + label + ".");
        optional(value, max, label);
    }
    static void optional(String value, int max, String label) {
        if (value != null && value.length() > max)
            throw new ValidationException(label + " không được vượt quá " + max + " ký tự.");
    }
    static void subject(Integer id, boolean required) {
        if ((required && id == null) || (id != null && id <= 0))
            throw new ValidationException("Vui lòng chọn môn học hợp lệ.");
    }
    static void date(LocalDate value, String label) {
        if (value == null) throw new ValidationException("Vui lòng nhập " + label + " hợp lệ.");
    }
}
