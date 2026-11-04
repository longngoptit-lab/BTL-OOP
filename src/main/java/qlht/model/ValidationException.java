package qlht.model;

/** Dữ liệu nghiệp vụ không hợp lệ; thông báo có thể hiển thị cho người dùng. */
public final class ValidationException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    public ValidationException(String message) { super(message); }
}
