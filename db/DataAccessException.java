package qlht.db;

/** Lỗi khi làm việc với CSDL (bọc SQLException/IOException thành lỗi không bắt buộc khai báo). */
public class DataAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataAccessException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
