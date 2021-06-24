package pl.marcinchwedczuk.img2h.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import pl.marcinchwedczuk.img2h.gui.mainwindow.MainWindow;

import java.util.concurrent.Executors;

/**
 * JavaFX App
 */
public class App extends Application {
    @Override
    public void start(Stage stage) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            showExceptionDialog(e);
        });

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