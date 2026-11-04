package qlht.ui;

import qlht.db.BaiTapDao;
import qlht.db.Database;
import qlht.db.GhiChuDao;
import qlht.db.LichHocDao;
import qlht.db.LichThiDao;
import qlht.db.MonHocDao;
import qlht.service.ConflictService;

/** Gom các DAO và service để truyền cho các panel (thay vì mỗi panel tự tạo). */
public class AppContext {
    public final MonHocDao monHocDao;
    public final qlht.service.MonHocService monHocService;
    public final LichHocDao lichHocDao;
    public final LichThiDao lichThiDao;
    public final BaiTapDao baiTapDao;
    public final GhiChuDao ghiChuDao;
    public final ConflictService conflicts;
    private Runnable refreshAll = () -> { };

    public AppContext(Database db, qlht.model.User user) {
        monHocDao = new MonHocDao(db, user);
        monHocService = new qlht.service.MonHocService(monHocDao);
        lichHocDao = new LichHocDao(db, user);
        lichThiDao = new LichThiDao(db, user);
        baiTapDao = new BaiTapDao(db, user);
        ghiChuDao = new GhiChuDao(db, user);
        conflicts = new ConflictService(lichHocDao, lichThiDao);
    }

    public void setRefreshAll(Runnable r) { this.refreshAll = r; }

    /** Làm mới tất cả các tab (gọi sau khi dữ liệu thay đổi). */
    public void refreshAll() { refreshAll.run(); }
}
