package pl.marcinchwedczuk.img2h.gui.mainwindow;

import org.junit.jupiter.api.*;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import pl.marcinchwedczuk.img2h.gui.App;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.concurrent.TimeoutException;

import static org.testfx.assertions.api.Assertions.assertThat;

public class MainWindowIT {
    private FxRobot robot = new FxRobot();

    @BeforeEach
    public void setup() throws Exception {
        App.testMode = true;
        ApplicationTest.launch(App.class);
    }

    @AfterEach
    public void cleanup() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    @Test
    public void smokeTest() throws IOException {
        // Prepare file on disk
        Path testImage = Paths.get("test-image.png");
        if (Files.exists(testImage)) {
            Files.delete(testImage);
        }
        try (InputStream is = getClass().getResourceAsStream("/test-image.png")) {
            byte[] imageData = is.readAllBytes();
            Files.write(testImage, imageData, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }

        // Open file open dialog
        robot.clickOn("#fileMenu").clickOn("#openMenuItem");

        // Select file
        robot.clickOn("#filePath").write(testImage.toAbsolutePath().toString());
        robot.clickOn("#selectButton");

        // Save code as C header
        robot.clickOn("#fileMenu").clickOn("#saveMenuItem");

        // Select file
        Path headerFile = Paths.get("test-header.h");
        robot.clickOn("#filePath").write(headerFile.toAbsolutePath().toString());
        robot.clickOn("#selectButton");

        // Check file contains valid data
        String createdHeader = Files.readString(headerFile);
        String expectedHeader = "";
        try (InputStream is = getClass().getResourceAsStream("/expected-test-header.h")) {
            expectedHeader = new String(is.readAllBytes());
        }

        // Cleanup
        Files.delete(headerFile);
        Files.delete(testImage);

        // Check if data is correct
        assertThat(createdHeader)
                .isEqualTo(expectedHeader);
    }

}