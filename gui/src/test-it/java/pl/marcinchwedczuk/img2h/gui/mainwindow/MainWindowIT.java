package pl.marcinchwedczuk.img2h.gui.mainwindow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import pl.marcinchwedczuk.img2h.gui.App;

import java.util.concurrent.TimeoutException;

import static org.testfx.assertions.api.Assertions.assertThat;

public class MainWindowIT {
    private FxRobot robot = new FxRobot();

    @BeforeEach
    public void setup() throws Exception {
        ApplicationTest.launch(App.class);
    }

    @AfterEach
    public void cleanup() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    @Test
    @Disabled("problem with loading image from jar")
    public void application_starts_properly() {
        // Visit https://github.com/TestFX/TestFX
        // to learn how to test.
        //assertThat(robot.lookup(".button").queryButton())
        //        .hasText("Click me!");
    }
}