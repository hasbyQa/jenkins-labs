package com.swaglabs.utils;

// Central config — every constant lives here. Change once, reflected everywhere.
public final class TestConfig {

    private TestConfig() {}

    // App
    public static final String BASE_URL = "https://www.saucedemo.com";

    // Reads SELENIUM_TIMEOUT from environment if set (Docker/CI sets it to 30).
    // Falls back to 20s for local runs. Avoids hardcoding two different values.
    public static final int TIMEOUT_SECONDS = resolveTimeout();

    private static int resolveTimeout() {
        String env = System.getenv("SELENIUM_TIMEOUT");
        if (env != null && !env.isBlank()) {
            try { return Integer.parseInt(env.trim()); }
            catch (NumberFormatException ignored) {}
        }
        return 20;
    }

    // Valid credentials
    public static final String VALID_USER     = "standard_user";
    public static final String VALID_PASSWORD = "secret_sauce";

    // Invalid credentials (negative tests)
    public static final String INVALID_USER     = "bad_user";
    public static final String INVALID_PASSWORD = "bad_password";

    // Locked-out user (Swag Labs built-in test account)
    public static final String LOCKED_USER = "locked_out_user";

    // Checkout form data
    public static final String FIRST_NAME  = "Test";
    public static final String LAST_NAME   = "User";
    public static final String POSTAL_CODE = "12345";

    // Expected UI text
    public static final String LOGIN_ERROR_MESSAGE   = "Epic sadface:";
    public static final String CHECKOUT_COMPLETE_MSG = "Thank you for your order!";
    public static final String PRODUCTS_TITLE        = "Products";
}