package pl.marcinchwedczuk.img2h.gui;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;
import pl.marcinchwedczuk.img2h.gui.mainwindow.MainWindow;

import java.util.concurrent.Executors;

/**
 * JavaFX App
 */
public class App extends Application {
    private static HostServices hostServices = null;
    public static HostServices hostServices() {
        if (hostServices == null) {
            throw new IllegalStateException();
        }
        return hostServices;
    }

    @Override
    public void start(Stage stage) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            showExceptionDialog(e);
        });

        App.hostServices = this.getHostServices();
        MainWindow.showOn(stage);
    }

    private void showExceptionDialog(Throwable e) {
        StringBuilder msg = new StringBuilder();
        msg.append("Unhandled exception:\n");

        Throwable curr = e;
        while (curr != null) {
            if (curr.getMessage() != null && !curr.getMessage().isBlank()) {
                msg.append(curr.getClass().getSimpleName()).append(": ")
                        .append(curr.getMessage())
                        .append("\n");
            }
            curr = curr.getCause();
        }

        UiService.errorDialog(msg.toString());
    }

    public static void main(String[] args) {
        launch();
    }
}