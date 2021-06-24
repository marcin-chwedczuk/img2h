package pl.marcinchwedczuk.img2h.gui.aboutdialog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class AboutDialog {
    public static AboutDialog show(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    AboutDialog.class.getResource("AboutDialog.fxml"));

            Stage childWindow = new Stage();
            childWindow.initOwner(owner);
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.setTitle("About...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.setResizable(false);
            childWindow.sizeToScene();

            AboutDialog controller = loader.getController();
            childWindow.show();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private GridPane rootElement;

    @FXML
    private void guiClose() {
        ((Stage)rootElement.getScene().getWindow()).close();
    }
}
