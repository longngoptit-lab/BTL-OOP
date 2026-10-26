# Quản lí học tập cá nhân (Java Swing + SQLite)

Yêu cầu: JDK 17 trở lên và Maven.

## Chạy
    mvn compile exec:java          # chạy ngay
    mvn package && java -jar target/qlht.jar   # đóng gói rồi chạy

File dữ liệu `qlht.db` nằm trong thư mục đang chạy lệnh. Muốn dùng lại dữ liệu cũ từ bản
Python, chép file `qlht.db` cũ vào thư mục đó (cùng cấu trúc bảng; ngày cũ dd/mm/yyyy được
tự đổi sang yyyy-mm-dd, có sao lưu `qlht.db.bak`).

## Cấu trúc
    qlht/Main.java              điểm vào
    qlht/model/                 Entity (lớp cha) -> MonHoc, LichHoc, LichThi, BaiTap, GhiChu
    qlht/db/                    Database (kết nối, tạo bảng) + BaseDao -> các DAO
    qlht/service/               ConflictService (phát hiện trùng lịch)
    qlht/util/                  Const, DateUtil, TimeUtil, TextUtil
    qlht/ui/                    TabPanel (lớp cha) -> 6 tab; EntityForm<T> -> 5 form; EditDialog<T>
