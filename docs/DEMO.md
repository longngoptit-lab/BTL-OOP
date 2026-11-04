# Tạo dữ liệu minh họa

Cần SQL Server đã tạo database riêng `QLHT_DEMO` bằng `sql/create-database.sql`. Cấu hình kết nối theo [INSTALL.md](INSTALL.md).

### Tạo dữ liệu minh họa tự động

Build/gỡ nén gói ứng dụng, cấu hình kết nối như bình thường. Chạy ở terminal:

**macOS/Linux:**

```sh
QLHT_DB_NAME=QLHT_DEMO java -jar qlht.jar --seed-demo
QLHT_DB_NAME=QLHT_DEMO java -jar qlht.jar
```

**Windows CMD:**

```bat
set QLHT_DB_NAME=QLHT_DEMO
java -jar qlht.jar --seed-demo
java -jar qlht.jar
```

Nếu chạy từ mã nguồn, đổi `qlht.jar` thành `target/qlht.jar` sau khi build `package`. Công cụ hỏi tên đăng nhập **mới** và mật khẩu; không có mật khẩu demo viết cứng. Tên database phải kết thúc `_DEMO`. Nếu tên tài khoản trùng, công cụ dừng và không sửa tài khoản có sẵn. Không chạy lại lệnh seed với tài khoản vừa tạo; muốn bộ dữ liệu khác, dùng tài khoản demo mới.

Bộ minh họa có 5 môn, 7 buổi học (cố ý có một cặp trùng), 2 lịch thi sắp tới, 5 bài tập với hạn tương đối theo ngày chạy và 3 ghi chú. Các bản ghi minh họa được ghi cùng transaction; nếu ghi lỗi thì rollback bộ dữ liệu. Việc đăng ký tài khoản diễn ra trước, nên nếu seed thất bại tài khoản vẫn có thể tồn tại và đăng nhập được.
