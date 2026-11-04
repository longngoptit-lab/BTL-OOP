package qlht.model;

/** Lớp cha của các thực thể nghiệp vụ: chỉ chứa khóa chính. 0 nghĩa là chưa lưu. */
public abstract class Entity {
    private int id;

    public int getId() { return id; }
    public void setId(int id) {
        if (id < 0) throw new ValidationException("Mã đối tượng không được âm.");
        this.id = id;
    }
    /** Kiểm tra trước khi ghi; vẫn cho phép đọc dữ liệu cũ để người dùng chỉnh sửa. */
    public abstract void validate();

    public boolean isNew() { return id == 0; }
}
