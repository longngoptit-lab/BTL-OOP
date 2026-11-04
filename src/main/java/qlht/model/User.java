package qlht.model;

/** Thông tin phiên đăng nhập; không chứa mật khẩu hoặc mã băm. */
public record User(int id, String username, String displayName) {
    public User {
        if (id <= 0) throw new IllegalArgumentException("Tài khoản không hợp lệ.");
        java.util.Objects.requireNonNull(username);
        java.util.Objects.requireNonNull(displayName);
    }
}
