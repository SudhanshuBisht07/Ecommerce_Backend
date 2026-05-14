package com.easymart.config;
public class JWT_CONSTANT {
    public static final String SECRET_KEY;
    static {
        String key = System.getenv("JWT_SECRET_KEY");
        if (key == null || key.isBlank()) {
            // Fallback for local dev — replace with a real secret in production
            key = "EasyMart@SecretKey#2024$MultiVendor%JWT&Auth!";
        }
        SECRET_KEY = key;
    }
    public static final String JWT_HEADER = "Authorization";
}
