package pl.marcinchwedczuk.img2h.gui.mainwindow;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.imaging.*;
import org.apache.commons.imaging.color.ColorConversions;
import org.apache.commons.imaging.color.ColorHsl;
import org.apache.commons.imaging.palette.Palette;
import org.apache.commons.imaging.palette.SimplePalette;
import pl.marcinchwedczuk.img2h.domain.Util;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
    private ImageView image;

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
                new FileChooser.ExtensionFilter("All Images", "*.bmp", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("BMP Images", "*.bmp"),
                new FileChooser.ExtensionFilter("JPEG Images", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG Images", "*.png"));

        File file = fileChooser.showOpenDialog(mainWindow.getScene().getWindow());
        if (file != null) {
            try {
                BufferedImage bufferedImage = Imaging.getBufferedImage(file);

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

                Image imageFile = new Image(new ByteArrayInputStream(os.toByteArray()));
                image.setImage(imageFile);
                image.setFitWidth(imageFile.getWidth());
                image.setFitHeight(imageFile.getHeight());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void guiSaveHeader() {

    }
}
