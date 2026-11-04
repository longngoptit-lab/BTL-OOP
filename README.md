# StudySpace – Quản lý học tập cá nhân

Bài tập lớn Java OOP: Swing + FlatLaf, SQL Server/JDBC. Có đăng ký/đăng nhập, môn học, lịch tuần, lịch thi, bài tập, ghi chú và dashboard. Mỗi tài khoản có dữ liệu riêng trong ứng dụng.

## Chạy nhanh

Yêu cầu **JDK 17+** khi build và SQL Server đang chạy. Không cần cài Maven riêng. Lần build đầu cần Internet.

1. Dùng DBeaver/SSMS chạy `sql/create-database.sql` để tạo database.
2. Chép `db.properties.example` thành `db.properties`, điền host/port/database và tài khoản SQL. File local này được Git và các gói nộp bỏ qua.
3. Chạy từ thư mục project:

**macOS/Linux:**

```sh
./run.sh --check-db
./run.sh
```

**Windows CMD:**

```bat
run.cmd --check-db
run.cmd
```

Đăng ký tài khoản ứng dụng ở màn hình đầu. Tài khoản SQL trong `db.properties` dùng để kết nối database, độc lập với tài khoản ứng dụng. Tài khoản ứng dụng đầu tiên nhận dữ liệu cũ chưa có chủ; các tài khoản sau bắt đầu trống.

Nếu máy dùng container `sqlserver` đã có sẵn: mở Docker Desktop và chạy `docker start sqlserver`, chờ khởi động hoàn tất rồi kiểm tra kết nối.

## Build và đóng gói

```sh
./mvnw clean package       # Windows: mvnw.cmd clean package
```

Maven chạy JUnit cho quy tắc model và xếp lịch, rồi tạo:

- [target/qlht.jar](target/qlht.jar): JAR chạy được, chứa JDBC Driver và FlatLaf.
- [target/StudySpace-1.0.0-app.zip](target/StudySpace-1.0.0-app.zip): gói chạy với launcher Windows/macOS/Linux, cấu hình mẫu và tài liệu.
- [target/StudySpace-1.0.0-source.zip](target/StudySpace-1.0.0-source.zip): source, test, Maven Wrapper và tài liệu để nộp.

Giải nén gói app, tạo `db.properties` bên trong rồi chạy `run.cmd`, `sh run.sh` hoặc `java -jar qlht.jar`. Máy chạy cần Java 17+ và SQL Server; gói không kèm Java runtime/database cá nhân.

## Cấu trúc mã nguồn

```text
src/main/java/qlht/
  Main.java       Điểm vào
  model/          Entity, 5 model nghiệp vụ, User, validation
  db/             Database, BaseDao và DAO theo tài khoản
  service/        AuthService, MonHocService, ConflictService, DemoDataService
  ui/             Swing, EntityForm<T>, EditDialog<T>, lịch tuần
  util/           Ngày/giờ, văn bản, băm mật khẩu
  tools/          CLI tạo bộ demo
src/test/java/qlht/tests/
docs/             Báo cáo, UML, cài đặt, demo và kiểm thử
sql/              Script tạo database
```

Mở **thư mục có `pom.xml`** trong IDE và import Maven project. File bảng hiện nằm ở `src/main/java/qlht/ui/RowTable.java`, khớp `package qlht.ui`; đóng tab ở đường dẫn cũ nếu IDE còn giữ.

## Thiết kế OOP

- `Entity.validate()` là hợp đồng chung; 5 thực thể triển khai quy tắc riêng. DAO kiểm tra trước khi ghi, kể cả khi không đi qua Swing.
- `BaseDao` tái sử dụng JDBC và kiểm tra chủ sở hữu môn.
- `TabPanel.refresh()` thể hiện đa hình trên danh sách màn hình.
- `EntityForm<T>` và `EditDialog<T>` tái sử dụng form/hộp thoại với kiểu dữ liệu cụ thể.
- `MonHocService` giữ quy tắc trùng tên và điều phối lưu; panel tập trung vào nhập liệu và hiển thị.

## Kiểm thử

```sh
./mvnw test               # Windows: mvnw.cmd test; không cần SQL Server
./test.sh                # Java thuần, không cần Maven
./test.sh --ui           # hồi quy ô tín chỉ, cần desktop
./test-week.sh --ui      # lịch tuần với dữ liệu minh họa, không ghi database
./test-auth.sh --ui      # kiểm thử tích hợp bằng database tạm riêng, tự xóa
```

Các script `.sh` dành cho macOS/Linux; cách chạy tích hợp trên Windows có trong [INSTALL.md](docs/INSTALL.md). Kiểm thử tích hợp cần login SQL có quyền tạo/xóa database. Kết quả kiểm chứng xem [KIEM_THU.md](docs/KIEM_THU.md).

## Thời khóa biểu và tài khoản

Lịch tuần có 7 cột, trục giờ/ngày cố định khi cuộn, màu theo môn, viền đỏ cho lịch trùng và bộ lọc môn. Nhấp đúp buổi học để sửa hoặc ô trống để thêm. Chế độ danh sách giữ lựa chọn khi chuyển qua lại. Giờ lấy từ bảng tiết tại `src/main/java/qlht/util/TimeUtil.java`; lịch lặp theo thứ, chưa có phạm vi học kỳ.

Tài khoản hỗ trợ đổi mật khẩu và đăng xuất ở sidebar. Tên đăng nhập 3–40 ký tự không dấu, mật khẩu 8–128 ký tự; lưu PBKDF2-HMAC-SHA256 với salt ngẫu nhiên. Dữ liệu được lọc theo chủ sở hữu tại DAO.

Có thể dùng biến môi trường `QLHT_DB_HOST`, `QLHT_DB_PORT`, `QLHT_DB_NAME`, `QLHT_DB_USER`, `QLHT_DB_PASSWORD`, `QLHT_DB_TRUST_CERTIFICATE` để ghi đè cấu hình. Chi tiết TLS và quyền SQL xem hướng dẫn cài đặt.

## Hướng dẫn

- [Cài đặt Windows/macOS/Linux](docs/INSTALL.md)
- [Tạo dữ liệu minh họa](docs/DEMO.md)
- [Kết quả kiểm thử và giới hạn kiểm chứng](docs/KIEM_THU.md)
- [Thư viện ngoài](docs/THIRD_PARTY.md)

Thư mục `BTL--OOP` là project khác trong workspace, không được đưa vào gói StudySpace. File SQLite cũ nếu có vẫn được giữ trên máy; không tự chuyển dữ liệu SQLite sang SQL Server.
