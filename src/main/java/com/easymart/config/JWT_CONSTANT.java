package com.easymart.config;

public class JWT_CONSTANT {
    public static final String SECRET_KEY = System.getenv("JWT_SECRET_KEY") != null
            ? System.getenv("JWT_SECRET_KEY")
            : "fallback-dev-only-key-min-32chars!";
    public static final String JWT_HEADER="Authorization";
}
