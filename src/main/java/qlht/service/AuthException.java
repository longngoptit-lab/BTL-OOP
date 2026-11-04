package qlht.service;

/** Lỗi nhập liệu hoặc xác thực được hiển thị trực tiếp trên form. */
public class AuthException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public AuthException(String message) { super(message); }
}
