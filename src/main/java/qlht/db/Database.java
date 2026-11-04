package qlht.db;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/** Kết nối SQL Server; cấu hình từ db.properties hoặc biến môi trường QLHT_DB_*. */
public class Database implements AutoCloseable {
    private final Connection conn;

    public Database() {
        Connection opened = null;
        try {
            Properties config = new Properties();
            Path file = Path.of("db.properties");
            if (Files.exists(file)) {
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    config.load(reader);
                }
            }
            Properties jdbc = new Properties();
            jdbc.setProperty("serverName", setting(config, "host", "localhost"));
            jdbc.setProperty("portNumber", setting(config, "port", "1433"));
            jdbc.setProperty("databaseName", setting(config, "name", "QLHT"));
            jdbc.setProperty("user", setting(config, "user", "sa"));
            jdbc.setProperty("password", setting(config, "password", ""));
            jdbc.setProperty("encrypt", "true");
            jdbc.setProperty("trustServerCertificate", setting(config, "trust_certificate", "false"));
            jdbc.setProperty("loginTimeout", "10");
            jdbc.setProperty("sendStringParametersAsUnicode", "true");
            opened = DriverManager.getConnection("jdbc:sqlserver://", jdbc);
            conn = opened;
            createTables();
        } catch (SQLException | IOException e) {
            if (opened != null) {
                try { opened.close(); } catch (SQLException closeError) { e.addSuppressed(closeError); }
            }
            throw new DataAccessException(e);
        }
    }

    private static String setting(Properties config, String key, String fallback) {
        String env = System.getenv("QLHT_DB_" + key.toUpperCase(java.util.Locale.ROOT));
        return env != null ? env : config.getProperty("db." + key, fallback);
    }

    public Connection connection() { return conn; }

    private void createTables() throws SQLException {
        String[] tables = {
            """
            IF OBJECT_ID(N'dbo.mon_hoc', N'U') IS NULL
            CREATE TABLE dbo.mon_hoc (
                id INT IDENTITY(1,1) PRIMARY KEY,
                ten_mon NVARCHAR(255) NOT NULL,
                so_tin_chi INT DEFAULT 0,
                giang_vien NVARCHAR(255),
                ghi_chu NVARCHAR(MAX)
            )
            """,
            """
            IF OBJECT_ID(N'dbo.lich_hoc', N'U') IS NULL
            CREATE TABLE dbo.lich_hoc (
                id INT IDENTITY(1,1) PRIMARY KEY,
                mon_hoc_id INT NOT NULL,
                thu NVARCHAR(20) NOT NULL,
                tiet_bat_dau INT NOT NULL,
                tiet_ket_thuc INT NOT NULL,
                phong_hoc NVARCHAR(255),
                FOREIGN KEY (mon_hoc_id) REFERENCES dbo.mon_hoc(id) ON DELETE CASCADE
            )
            """,
            """
            IF OBJECT_ID(N'dbo.lich_thi', N'U') IS NULL
            CREATE TABLE dbo.lich_thi (
                id INT IDENTITY(1,1) PRIMARY KEY,
                mon_hoc_id INT NOT NULL,
                ngay_thi DATE NOT NULL,
                gio_thi NVARCHAR(50),
                phong_thi NVARCHAR(255),
                hinh_thuc NVARCHAR(255),
                FOREIGN KEY (mon_hoc_id) REFERENCES dbo.mon_hoc(id) ON DELETE CASCADE
            )
            """,
            """
            IF OBJECT_ID(N'dbo.bai_tap', N'U') IS NULL
            CREATE TABLE dbo.bai_tap (
                id INT IDENTITY(1,1) PRIMARY KEY,
                mon_hoc_id INT,
                tieu_de NVARCHAR(255) NOT NULL,
                mo_ta NVARCHAR(MAX),
                han_nop DATE NOT NULL,
                muc_do_uu_tien NVARCHAR(50) DEFAULT N'Trung bình',
                trang_thai NVARCHAR(50) DEFAULT N'Chưa hoàn thành',
                FOREIGN KEY (mon_hoc_id) REFERENCES dbo.mon_hoc(id) ON DELETE SET NULL
            )
            """,
            """
            IF OBJECT_ID(N'dbo.ghi_chu', N'U') IS NULL
            CREATE TABLE dbo.ghi_chu (
                id INT IDENTITY(1,1) PRIMARY KEY,
                mon_hoc_id INT,
                tieu_de NVARCHAR(255) NOT NULL,
                noi_dung NVARCHAR(MAX),
                ngay_tao DATE NOT NULL,
                FOREIGN KEY (mon_hoc_id) REFERENCES dbo.mon_hoc(id) ON DELETE SET NULL
            )
            """
        };
        conn.setAutoCommit(false);
        try (Statement st = conn.createStatement()) {
            for (String sql : tables) st.execute(sql);
            migrateAccounts(st);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /** Nâng cấp schema cũ; dữ liệu chưa có chủ được giữ tới lần đăng ký đầu tiên. */
    private void migrateAccounts(Statement st) throws SQLException {
        st.execute("""
            IF OBJECT_ID(N'dbo.tai_khoan', N'U') IS NULL
            CREATE TABLE dbo.tai_khoan (
                id INT IDENTITY(1,1) PRIMARY KEY,
                username VARCHAR(40) COLLATE Latin1_General_100_CI_AS NOT NULL UNIQUE,
                display_name NVARCHAR(80) NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
            )
            """);
        for (String table : new String[]{"mon_hoc", "lich_hoc", "lich_thi", "bai_tap", "ghi_chu"}) {
            st.execute("IF COL_LENGTH(N'dbo." + table + "', N'user_id') IS NULL "
                    + "ALTER TABLE dbo." + table + " ADD user_id INT NULL");
            st.execute("IF OBJECT_ID(N'dbo.fk_" + table + "_user', N'F') IS NULL "
                    + "ALTER TABLE dbo." + table + " ADD CONSTRAINT fk_" + table + "_user "
                    + "FOREIGN KEY (user_id) REFERENCES dbo.tai_khoan(id)");
            st.execute("IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo."
                    + table + "') AND name = N'ix_" + table + "_user') "
                    + "CREATE INDEX ix_" + table + "_user ON dbo." + table + "(user_id)");
        }
    }

    @Override
    public void close() {
        try { conn.close(); } catch (SQLException ignored) { }
    }
}
