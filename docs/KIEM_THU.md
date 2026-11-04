# Kết quả kiểm chứng

Ngày kiểm chứng: **04/11/2026**. Môi trường chạy trực tiếp: macOS, Temurin JDK 21, mã biên dịch với `--release 17`, Maven 3.9.9 qua Wrapper và SQL Server local trong Docker.

| Hạng mục | Kết quả |
|---|---|
| Maven Wrapper tải Maven và kiểm tra SHA-256 | Đạt; có fallback `shasum` cho macOS |
| `mvnw test`, JUnit Jupiter | 2 test case đạt: 38 kiểm tra nghiệp vụ model và 24 kiểm tra xếp lịch |
| `test.sh --ui` | 42 kiểm tra model/form và 24 kiểm tra lịch đạt; nhập tín chỉ `abc`, `0`, `11` dừng gửi form, `10` được giữ |
| `test-auth.sh --ui` | 68 kiểm tra SQL đạt; đăng ký/đăng nhập/đổi mật khẩu/đăng xuất và 6 màn hình đạt |
| Nâng cấp schema cũ | Giữ các nhóm dữ liệu cũ, gắn cho tài khoản đầu tiên; tài khoản thứ hai bắt đầu trống |
| Quyền dữ liệu | Chặn đọc/sửa/xóa và gắn môn của tài khoản khác ở cả 5 DAO |
| MonHocService | Chặn tên môn trùng sau chuẩn hóa trong tài khoản; cho phép hai tài khoản có môn cùng tên |
| Dữ liệu demo | 5 môn, 7 buổi, 2 lịch thi, 5 bài tập, 3 ghi chú; chặn seed lại vào tài khoản có dữ liệu |
| CLI `--seed-demo` của JAR | Đã chạy trong terminal thật trên database demo tạm riêng |
| JAR giải nén ở đường dẫn có dấu cách | Kết nối SQL Server thành công, không cần source/classpath ngoài |
| Giao diện từ JAR | Khởi động FlatLaf, đăng nhập tài khoản demo và đọc 7 buổi học thành công |
| Metadata JAR | Có Main-Class, JDBC service loader, FlatLaf/JDBC; không chứa JUnit |
| ZIP app/source | Kiểm tra không có `db.properties`, database cá nhân hoặc project con `BTL--OOP` |
| Package Java | Mọi khai báo package khớp đường dẫn `src/main/java` hoặc `src/test/java` |
| Build lại từ ZIP source | Đạt: giải nén source vào thư mục mới có dấu cách, chạy Wrapper/JUnit thành công |

Các kiểm thử tích hợp và kiểm tra gói dùng database tạm, đã xóa sau khi kết thúc. Không tạo tài khoản demo hoặc ghi bộ minh họa vào database cá nhân `QLHT`.

## Giới hạn kiểm chứng

- Launcher Windows/CMD có trong gói nhưng **chưa chạy trực tiếp trên Windows** ở phiên này. Cần chạy thử trên máy Windows đích trước khi sử dụng.
- Chưa kiểm chứng môi trường mạng nhiều người dùng đồng thời, dữ liệu rất lớn hoặc truy vấn SQL có độ trễ cao. Một số CRUD vẫn chạy trên EDT.
- Bộ quy đổi tiết học và ngưỡng giờ thi vẫn theo quy ước hiện tại của ứng dụng; không phải cấu hình chung cho mọi trường.

Tài liệu này là log kiểm tra kỹ thuật, không phải báo cáo môn học. Khi thay đổi mã nguồn, hãy chạy lại các nhóm kiểm tra liên quan.
