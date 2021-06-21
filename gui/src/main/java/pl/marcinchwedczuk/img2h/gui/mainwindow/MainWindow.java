package pl.marcinchwedczuk.img2h.gui.mainwindow;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.imaging.*;
import org.apache.commons.imaging.color.ColorConversions;
import org.apache.commons.imaging.color.ColorHsl;
import org.apache.commons.imaging.palette.Palette;
import pl.marcinchwedczuk.img2h.gui.waitdialog.WaitDialog;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {
    public static MainWindow showOn(Stage window) {
        try {
            FXMLLoader loader = new FXMLLoader(MainWindow.class.getResource("MainWindow.fxml"));

            Scene scene = new Scene(loader.load());
            MainWindow controller = (MainWindow) loader.getController();

            window.setTitle("Main Window");
            window.setScene(scene);
            window.setResizable(true);

            window.show();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private BorderPane mainWindow;

    @FXML
    private ImageView originalImage;

    @FXML
    private ImageView lcdImage;

    @FXML
    private ScrollPane imageContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void guiOpenImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image...");

        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home")));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.ico"),
                new FileChooser.ExtensionFilter("JPEG Images", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG Images", "*.png"),
                new FileChooser.ExtensionFilter("BMP Images", "*.bmp"),
                new FileChooser.ExtensionFilter("ICO Images", "*.ico")
        );

        File file = fileChooser.showOpenDialog(mainWindow.getScene().getWindow());
        if (file != null) {

        }
    }

    @FXML
    private void guiSaveHeader() {

    }

    public void guiDragOverImageContainer(DragEvent event) {
        // On drag over if the DragBoard has files
        if (event.getDragboard().hasFiles()) {
            // All files on the dragboard must have an accepted extension
            /*if (!validExtensions.containsAll(
                    event.getDragboard().getFiles().stream()
                            .map(file -> getExtension(file.getName()))
                            .collect(Collectors.toList()))) { */

            // Allow for both copying and moving
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    public void guiDragDroppedOnImageContainer(DragEvent event) {
        boolean success = false;
        if (event.getDragboard().hasFiles()) {
            // Print files
            File file = event.getDragboard().getFiles().get(0);
            loadImage(file);
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void loadImage(File imageFile) {
        try {
            BufferedImage bufferedImage = Imaging.getBufferedImage(imageFile);

            /*
            org.apache.commons.imaging.palette.Dithering.applyFloydSteinbergDithering(bufferedImage,
                    new Palette() {
                        @Override
                        public int getPaletteIndex(int rgb) throws ImageWriteException {
                            if (ColorConversions.convertRGBtoHSL(rgb).L < 0.50) {
                                return 0;
                            }
                            return 1;
                        }

                        @Override
                        public int getEntry(int index) {
                            return index == 0
                                    ? ColorConversions.convertHSLtoRGB(ColorHsl.BLACK)
                                    : ColorConversions.convertHSLtoRGB(ColorHsl.WHITE);
                        }

                        @Override
                        public int length() {
                            return 2;
                        }
                    });
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Imaging.writeImage(bufferedImage, os, ImageFormats.BMP, new HashMap<>());
            os.close();

            Image imageFile = new Image(new ByteArrayInputStream(os.toByteArray()));*/

            Image image = SwingFXUtils.toFXImage(bufferedImage, null);

            originalImage.setImage(image);
            originalImage.setFitWidth(image.getWidth());
            originalImage.setFitHeight(image.getHeight());
        } catch (Exception e) {
            UiService.errorDialog("Failure: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void guiCrop(ActionEvent actionEvent) {

    }

    public void guiResize(ActionEvent actionEvent) {

    }

    public void guiExport(ActionEvent actionEvent) {

    }

    public void guiNokia5110SizePreset(ActionEvent actionEvent) {

    }
}
