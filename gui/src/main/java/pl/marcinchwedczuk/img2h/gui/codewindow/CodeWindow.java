package pl.marcinchwedczuk.img2h.gui.codewindow;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class CodeWindow {

    public static CodeWindow show(Window owner, String code) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CodeWindow.class.getResource("CodeWindow.fxml"));

            Stage childWindow = new Stage();
            childWindow.initOwner(owner);
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.setTitle("Please wait...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.setResizable(true);
            childWindow.sizeToScene();

            CodeWindow controller = loader.getController();
            controller.setCode(code);

            childWindow.show();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private TextArea codeArea;

    private void setCode(String code) {
        codeArea.setText("");
        codeArea.appendText(code);
    }

    @FXML
    private void guiCopyClose() {
        codeArea.selectAll();
        codeArea.copy();
        guiClose();
    }

    @FXML
    private void guiClose() {
        ((Stage)codeArea.getScene().getWindow()).close();
    }
}
