package qlht.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** PBKDF2-HMAC-SHA256, salt ngẫu nhiên riêng cho từng mật khẩu. */
public final class PasswordUtil {
    private static final int ITERATIONS = 600_000;
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final String DUMMY_HASH = "pbkdf2-sha256$600000$"
            + Base64.getEncoder().encodeToString(new byte[16]) + "$"
            + Base64.getEncoder().encodeToString(new byte[32]);

    private PasswordUtil() { }

    public static String hash(char[] password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] key = derive(password, salt, ITERATIONS);
        try {
            return "pbkdf2-sha256$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                    + "$" + Base64.getEncoder().encodeToString(key);
        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }

    public static boolean verify(char[] password, String encoded) {
        if (password == null || encoded == null) return false;
        try {
            String[] parts = encoded.split("\\$");
            if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) return false;
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < ITERATIONS || iterations > 2_000_000) return false;
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            if (salt.length != 16 || expected.length != 32) return false;
            byte[] actual = derive(password, salt, iterations);
            try { return MessageDigest.isEqual(expected, actual); }
            finally { Arrays.fill(actual, (byte) 0); }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Không khởi tạo được chức năng bảo vệ mật khẩu.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
