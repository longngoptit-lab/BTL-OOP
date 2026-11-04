package qlht.service;

import java.util.Objects;
import qlht.db.MonHocDao;
import qlht.model.MonHoc;
import qlht.model.ValidationException;

/** Quy tắc quản lý môn học, dùng được từ Swing, kiểm thử hoặc công cụ demo. */
public final class MonHocService {
    private final MonHocDao dao;
    public MonHocService(MonHocDao dao) { this.dao = Objects.requireNonNull(dao); }

    public void save(MonHoc subject) {
        Objects.requireNonNull(subject).validate();
        MonHoc duplicate = dao.timTrungTen(subject.getTenMon(), subject.isNew() ? null : subject.getId());
        if (duplicate != null) throw new ValidationException("Môn học '" + duplicate.getTenMon()
                + "' đã tồn tại trong tài khoản. Hãy dùng tên khác hoặc sửa môn đã có.");
        if (subject.isNew()) dao.them(subject); else dao.sua(subject);
    }

    public void delete(int id) {
        if (id <= 0) throw new ValidationException("Vui lòng chọn môn học cần xóa.");
        dao.xoa(id);
    }
}
