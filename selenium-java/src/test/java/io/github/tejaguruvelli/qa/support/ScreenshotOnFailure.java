package io.github.tejaguruvelli.qa.support;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Saves a PNG to target/screenshots/ when a test method throws.
 * Runs as AfterTestExecutionCallback so it fires before @AfterEach quits the driver.
 */
public class ScreenshotOnFailure implements AfterTestExecutionCallback {

    static final Path SCREENSHOT_DIR = Paths.get("target", "screenshots");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /** Implemented by the test base class so the extension can reach the live driver. */
    public interface DriverHolder {
        WebDriver driver();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        if (!(context.getRequiredTestInstance() instanceof DriverHolder holder)) {
            return;
        }
        WebDriver driver = holder.driver();
        if (!(driver instanceof TakesScreenshot camera)) {
            return;
        }
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            String name = context.getRequiredTestClass().getSimpleName()
                + "_" + context.getRequiredTestMethod().getName()
                + "_" + LocalDateTime.now().format(STAMP) + ".png";
            Path target = SCREENSHOT_DIR.resolve(name);
            Files.write(target, camera.getScreenshotAs(OutputType.BYTES));
            System.err.println("[screenshot] " + target.toAbsolutePath());
        } catch (IOException | RuntimeException e) {
            // Never mask the original test failure with a screenshot problem.
            System.err.println("[screenshot] could not capture: " + e.getMessage());
        }
    }
}
