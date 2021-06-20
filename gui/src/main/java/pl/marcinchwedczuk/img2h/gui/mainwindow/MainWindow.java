package pl.marcinchwedczuk.img2h.gui.mainwindow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import pl.marcinchwedczuk.img2h.domain.Util;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {
    public static MainWindow showOn(Stage window) {
        try {
            FXMLLoader loader = new FXMLLoader(MainWindow.class.getResource("MainWindow.fxml"));

            Scene scene = new Scene(loader.load());
            MainWindow controller = (MainWindow) loader.getController();

            window.setTitle("Main Window");
            window.setScene(scene);
            window.setResizable(false);

            window.show();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private Label label;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        label.setText(Util.quote("Hello, world!"));
    }
}
