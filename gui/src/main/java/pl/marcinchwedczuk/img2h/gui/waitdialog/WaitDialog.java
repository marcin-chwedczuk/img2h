package pl.marcinchwedczuk.img2h.gui.waitdialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class WaitDialog implements Initializable {
    public static WaitDialog show(Window owner,
                                  String text,
                                  Runnable onCancel) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    WaitDialog.class.getResource("WaitDialog.fxml"));

            Stage childWindow = new Stage();
            childWindow.initOwner(owner);
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.setTitle("Please wait...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.setResizable(false);
            childWindow.sizeToScene();

            WaitDialog controller = loader.getController();
            controller.setText(text);
            controller.setOnCancel(onCancel);

            childWindow.show();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label text;

    private Runnable onCancel = () -> { };

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void close() {
        ((Stage)progressBar.getScene().getWindow()).close();
    }

    private void setText(String s) {
        text.setText(s);
    }

    private void setOnCancel(Runnable r) {
        onCancel = Objects.requireNonNull(r);
    }

    @FXML
    private void guiCancel() {
        onCancel.run();
    }
}
