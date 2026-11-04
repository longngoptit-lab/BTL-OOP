# Thư viện và công cụ ngoài

| Thành phần | Phiên bản | Mục đích | Giấy phép / nguồn |
|---|---|---|---|
| FlatLaf | 3.7.2 | Look and Feel Swing | Apache-2.0, https://github.com/JFormDesigner/FlatLaf |
| Microsoft JDBC Driver for SQL Server | 13.4.0.jre11 | Kết nối SQL Server | MIT, https://github.com/microsoft/mssql-jdbc |
| JUnit Jupiter | 5.11.4 | Kiểm thử khi build Maven; không nằm trong JAR ứng dụng | EPL-2.0, https://junit.org/junit5/ |
| Apache Maven Wrapper | 3.3.2 | Tải/chạy Maven 3.9.9 | Apache-2.0, https://maven.apache.org/tools/wrapper/ |
| Maven Shade / Assembly / Compiler / Surefire | Theo pom.xml | Build và đóng gói | Apache-2.0, https://maven.apache.org/plugins/ |

Maven Wrapper được giữ phần đầu bản quyền Apache. Script Unix có bổ sung fallback sang `shasum` khi `sha256sum` trên macOS không nhận cú pháp GNU đọc stdin; checksum của bản Maven tải về vẫn được kiểm tra.

Gói JAR giữ metadata giấy phép/notice có trong thư viện. Không có tuyên bố cấp phép lại mã nguồn bài tập trong tài liệu này; nhóm cần quyết định nếu công khai repository.
