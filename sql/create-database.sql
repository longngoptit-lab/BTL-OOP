-- SQL Server: chạy từ DBeaver hoặc SSMS bằng tài khoản có quyền CREATE DATABASE.
-- Ứng dụng tự tạo/nâng cấp bảng trong database này ở lần kết nối đầu tiên.
IF DB_ID(N'QLHT') IS NULL
    EXEC(N'CREATE DATABASE [QLHT]');
GO
-- Database riêng, tùy chọn, để demo hoặc thực hành mà không dùng dữ liệu cá nhân.
IF DB_ID(N'QLHT_DEMO') IS NULL
    EXEC(N'CREATE DATABASE [QLHT_DEMO]');
GO
