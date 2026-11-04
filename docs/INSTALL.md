# Cài đặt và chạy StudySpace

## 1. Yêu cầu

- Java 17 trở lên. Kiểm tra bằng `java -version`. Cần **JDK** nếu build từ mã nguồn.
- Microsoft SQL Server và SQL Server Authentication. DBeaver hoặc SSMS dùng để tạo database/kiểm tra dữ liệu.
- Lần build đầu cần Internet để Maven Wrapper tải Maven và các thư viện. Gói ứng dụng đã build chứa sẵn thư viện.

Không cần cài Maven riêng. `mvnw` và `mvnw.cmd` được giữ trong mã nguồn để dùng cùng phiên bản Maven 3.9.9.

## 2. Database

### SQL Server đã cài trên Windows hoặc server khác

Bật SQL Server, bật TCP/IP và xác định host/port. Trong DBeaver hoặc SSMS, chạy `sql/create-database.sql`; script tạo `QLHT` và database tùy chọn `QLHT_DEMO` nếu chưa có. Có thể chỉ chạy phần tạo `QLHT`.

Ứng dụng tự tạo bảng và bổ sung cột/chỉ mục cần thiết. Tài khoản kết nối cần quyền tạo/sửa bảng, tạo chỉ mục và đọc/ghi dữ liệu trong database được chọn.

### SQL Server có sẵn trong Docker trên máy phát triển

Mở Docker Desktop rồi chạy:

```sh
docker start sqlserver
```

Chờ SQL Server hoàn tất khởi động rồi kiểm tra kết nối. Hướng dẫn này dùng container `sqlserver` đã có sẵn; nếu cài mới cần tự chuẩn bị một SQL Server tương thích với máy của bạn.

## 3. Cấu hình kết nối

Đặt file `db.properties` **cạnh file chạy hoặc tại thư mục đang chạy lệnh**. Chép từ `db.properties.example` rồi sửa:

```properties
db.host=localhost
db.port=1433
db.name=QLHT
db.user=YOUR_SQL_LOGIN
db.password=YOUR_SQL_PASSWORD
db.trust_certificate=true
```

`true` dành cho server local với chứng chỉ tự ký. Server có chứng chỉ hợp lệ nên đặt `false`. File cấu hình được đọc UTF-8 theo cú pháp Java Properties: nếu mật khẩu có dấu `\`, ghi `\\` hoặc dùng biến môi trường `QLHT_DB_PASSWORD`.

Các biến `QLHT_DB_HOST`, `QLHT_DB_PORT`, `QLHT_DB_NAME`, `QLHT_DB_USER`, `QLHT_DB_PASSWORD`, `QLHT_DB_TRUST_CERTIFICATE` ghi đè từng giá trị trong file. Tài khoản kết nối SQL Server và tài khoản đăng nhập ứng dụng là hai loại độc lập.

## 4. Chạy gói ứng dụng đã đóng sẵn

Giải nén `StudySpace-1.0.0-app.zip`, tạo `db.properties` trong thư mục `StudySpace` rồi:

**Windows:** nhấp đúp `run.cmd` hoặc mở terminal trong thư mục đó:

```bat
java -jar qlht.jar
java -jar qlht.jar --check-db
```

**macOS/Linux:**

```sh
sh run.sh
java -jar qlht.jar --check-db
```

Gói JAR chứa JDBC Driver và FlatLaf. Máy chạy vẫn cần Java 17+ và kết nối được SQL Server.

## 5. Chạy từ mã nguồn

Mở đúng thư mục có `pom.xml` bằng IntelliJ IDEA, VS Code có Java Extension Pack, hoặc Eclipse với Maven support. Mã nguồn nằm đúng đường dẫn package: `src/main/java/qlht/...`. Nếu IDE còn lưu chẩn đoán từ đường dẫn cũ, đóng tab cũ rồi reload/import lại Maven project.

**Windows:**

```bat
mvnw.cmd test
mvnw.cmd package
run.cmd
```

**macOS/Linux:**

```sh
./mvnw test
./mvnw package
./run.sh
```

Có thể chạy bằng `./mvnw compile exec:java` hoặc `mvnw.cmd compile exec:java`. Trên macOS/Linux, `run.sh` là đường chạy nhanh dùng `javac`; script tự tải hai thư viện vào `.deps` nếu thiếu.

Kết quả `package`:

- `target/qlht.jar`: ứng dụng và thư viện.
- `target/StudySpace-1.0.0-app.zip`: bản chạy, cấu hình mẫu và tài liệu.
- `target/StudySpace-1.0.0-source.zip`: mã nguồn, Wrapper, test và tài liệu để nộp.

Các gói không chứa mật khẩu local, database cá nhân hoặc thư mục project khác `BTL--OOP`.

## 6. Tạo tài khoản lần đầu

Chọn **Chưa có tài khoản? Đăng ký**, nhập tên hiển thị, tên đăng nhập và mật khẩu 8–128 ký tự. Không có tài khoản mặc định của ứng dụng. Tài khoản đầu tiên đăng ký thành công nhận dữ liệu cũ chưa có chủ; những tài khoản sau có dữ liệu riêng.

## 7. Kiểm thử

```sh
./mvnw test                 # nghiệp vụ + xếp lịch, không cần database
./test.sh --ui              # thêm hồi quy form tín chỉ, cần desktop
./test-week.sh --ui         # kiểm tra tương tác lịch, không cần database
./test-auth.sh --ui         # SQL Server thật + tài khoản + giao diện
```

Hai lệnh cuối `.sh` dành cho macOS/Linux. Trên Windows có thể chạy kiểm thử tích hợp bằng terminal:

```bat
mvnw.cmd test-compile
set QLHT_DB_NAME=QLHT_AUTH_TEST_20261104_1
java -cp "target/classes;target/test-classes;target/qlht.jar" qlht.tests.AuthIntegrationTest --ui
```

Cần build `package` trước lệnh Java Windows để có `target/qlht.jar`. Kiểm thử tích hợp tự tạo rồi xóa database `QLHT_AUTH_TEST_*`; login SQL cần quyền CREATE/DROP DATABASE. Nếu tên đã tồn tại, kiểm thử dừng thay vì ghi đè. Đổi phần số khi cần. Sau kiểm thử, đóng terminal đó hoặc chạy `set QLHT_DB_NAME=` trước khi chạy ứng dụng thật.

## 8. Xử lý lỗi thường gặp

| Hiện tượng | Cách xử lý |
|---|---|
| `UnsupportedClassVersionError` | Dùng Java 17 trở lên. |
| `Connection refused`, lỗi TCP/IP | Bật SQL Server, chờ khởi động; kiểm tra host/port và TCP/IP. |
| `Login failed` | Kiểm tra tài khoản SQL trong file hoặc biến môi trường. |
| `Cannot open database` | Tạo database, kiểm tra `db.name` và quyền truy cập. |
| Lỗi chứng chỉ local | Dùng `db.trust_certificate=true` cho server local có chứng chỉ tự ký. |
| Thiếu driver/giao diện khi chạy từ IDE | Import Maven project; hoặc chạy JAR đã đóng gói. |
| IDE báo đỏ `package qlht.ui` | Mở file mới `src/main/java/qlht/ui/...`; reload Maven project. |

Windows launcher đã được chuẩn bị theo cú pháp CMD; phiên phát triển hiện tại chỉ kiểm chứng trực tiếp trên macOS. Nên chạy thử gói ZIP trên máy Windows thật trước khi sử dụng.
