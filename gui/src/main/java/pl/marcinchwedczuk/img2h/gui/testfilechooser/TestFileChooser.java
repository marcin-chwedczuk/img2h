package pl.marcinchwedczuk.img2h.gui.testfilechooser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import pl.marcinchwedczuk.img2h.gui.aboutdialog.AboutDialog;

import java.io.File;
import java.io.IOException;

public class TestFileChooser {

    public static File showSelectFilePathDialog(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    TestFileChooser.class.getResource("TestFileChooser.fxml"));

            Stage childWindow = new Stage();
            childWindow.initOwner(owner);
            childWindow.initModality(Modality.WINDOW_MODAL);
            childWindow.setTitle("Select file...");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.setResizable(false);
            childWindow.sizeToScene();

            TestFileChooser controller = loader.getController();
            childWindow.showAndWait();
            return controller.getSelectedFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private TextField filePath;

    private File selectedFile = null;

    @FXML
    private void selectFile() {
        selectedFile = new File(filePath.getText());
        thisWindow().close();
    }

    @FXML
    private void cancel() {
        selectedFile = null;
        thisWindow().close();
    }

    public File getSelectedFile() { return selectedFile; }

    private Stage thisWindow() {
        return (Stage)this.filePath.getScene().getWindow();
    }
}
