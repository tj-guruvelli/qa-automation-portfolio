package io.github.tejaguruvelli.qa.config;

import java.time.Duration;

/**
 * Immutable view of the runtime configuration. Every value is read from an
 * environment variable with a sane default so the suite runs identically on a
 * laptop, in GitHub Actions and in Jenkins without code changes.
 */
public final class TestConfig {

    private static final String DEFAULT_BASE_URL = "https://the-internet.herokuapp.com";
    private static final Duration DEFAULT_EXPLICIT_WAIT = Duration.ofSeconds(15);

    private TestConfig() {
    }

    /** Root of the application under test. Override with BASE_URL. */
    public static String baseUrl() {
        return stripTrailingSlash(env("BASE_URL", DEFAULT_BASE_URL));
    }

    /** Browser to launch: chrome (default), edge or firefox. Override with BROWSER. */
    public static String browser() {
        return env("BROWSER", "chrome").trim().toLowerCase();
    }

    /** Headless unless HEADLESS=false is set explicitly. */
    public static boolean headless() {
        return !"false".equalsIgnoreCase(env("HEADLESS", "true").trim());
    }

    /** Upper bound for every explicit wait in the suite. Override with EXPLICIT_WAIT_SECONDS. */
    public static Duration explicitWait() {
        String raw = env("EXPLICIT_WAIT_SECONDS", "");
        if (raw.isBlank()) {
            return DEFAULT_EXPLICIT_WAIT;
        }
        try {
            return Duration.ofSeconds(Long.parseLong(raw.trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "EXPLICIT_WAIT_SECONDS must be an integer number of seconds, got: " + raw, e);
        }
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
