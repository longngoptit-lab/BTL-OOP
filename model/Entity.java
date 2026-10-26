package qlht.model;

/** Lớp cha của mọi đối tượng lưu trong CSDL: chỉ chứa khóa chính. 0 nghĩa là chưa lưu. */
public abstract class Entity {
    private int id;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public boolean isNew() { return id == 0; }
}
