package pl.marcinchwedczuk.img2h.gui.mainwindow;

import com.google.common.io.Files;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.color.ColorConversions;
import org.apache.commons.imaging.palette.Palette;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

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
    private ImageView originalImageView;

    @FXML
    private ImageView lcdImage;

    @FXML
    private ScrollPane imageContainer;

    @FXML
    private Label originalWidthLabel;

    @FXML
    private Label originalHeightLabel;

    @FXML
    private Label formatLabel;

    @FXML
    private TextField cropDownText;

    @FXML
    private TextField cropTopText;

    @FXML
    private TextField cropLeftText;

    @FXML
    private TextField cropRightText;

    @FXML
    private TextField resizeNewWidthText;

    @FXML
    private TextField resizeNewHeightText;

    @FXML
    private ChoiceBox<BlackWhiteAlgorithm> bwAlgoChoice;

    @FXML
    public ChoiceBox<ExportFormat> exportFormatChoice;

    private FileChooser openImageDialog = setupOpenImageDialog();
    private BufferedImage originalImage = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bwAlgoChoice.getItems().addAll(BlackWhiteAlgorithm.values());
        bwAlgoChoice.setValue(BlackWhiteAlgorithm.DITHERING);

        exportFormatChoice.getItems().addAll(ExportFormat.values());
        exportFormatChoice.setValue(ExportFormat.C_BITS_LITERAL);
    }

    @FXML
    private void guiOpenImage() {
        File file = openImageDialog.showOpenDialog(mainWindow.getScene().getWindow());
        if (file != null) {
            loadImage(file);
            runTransformation();
        }
    }

    @FXML
    private void guiSaveHeader() {

    }

    public void guiDragOverImageContainer(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            List<File> files = event.getDragboard().getFiles();
            if (files.size() == 1 && files.get(0).isFile()) {
                String extension = Files.getFileExtension(files.get(0).getName());
                if (Set.of("bmp", "ico", "jpg", "jpeg", "png").contains(extension)) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
            }
        }

        event.consume();
    }

    public void guiDragDroppedOnImageContainer(DragEvent event) {
        boolean success = false;
        if (event.getDragboard().hasFiles()) {
            File file = event.getDragboard().getFiles().get(0);
            loadImage(file);
            runTransformation();
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void loadImage(File imageFile) {
        try {
            ImageFormat format = Imaging.guessFormat(imageFile);
            originalImage = Imaging.getBufferedImage(imageFile);

            formatLabel.setText(format.toString());
            originalWidthLabel.setText(originalImage.getWidth() + " px");
            originalHeightLabel.setText(originalImage.getHeight() + " px");

            Image image = SwingFXUtils.toFXImage(originalImage, null);
            this.originalImageView.setImage(image);
            this.originalImageView.setFitWidth(image.getWidth());
            this.originalImageView.setFitHeight(image.getHeight());

            cropDownText.setText("0");
            cropTopText.setText("0");
            cropLeftText.setText("0");
            cropRightText.setText("0");

            resizeNewWidthText.setText(Integer.toString(originalImage.getWidth()));
            resizeNewHeightText.setText(Integer.toString(originalImage.getHeight()));

            bwAlgoChoice.setValue(BlackWhiteAlgorithm.DITHERING);
            exportFormatChoice.setValue(ExportFormat.C_BITS_LITERAL);


        } catch (Exception e) {
            UiService.errorDialog("Failure: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void runTransformation() {
        if (originalImage == null) {
            UiService.infoDialog("No image is loaded!");
            return;
        }

        try {
            BufferedImage workingCopy = clone(originalImage);

            Image transformedFxImage = SwingFXUtils.toFXImage(workingCopy, null);
            this.lcdImage.setImage(transformedFxImage);
            this.lcdImage.setFitWidth(transformedFxImage.getWidth());
            this.lcdImage.setFitHeight(transformedFxImage.getHeight());
        } catch (Exception e) {
            UiService.errorDialog("Failure: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static BufferedImage clone(BufferedImage bufferImage) {
        ColorModel colorModel = bufferImage.getColorModel();
        WritableRaster raster = bufferImage.copyData(null);
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
    }

    private void convertToBlackAndWhite(BufferedImage workingCopy) throws Exception {
        org.apache.commons.imaging.palette.Dithering.applyFloydSteinbergDithering(workingCopy,
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
                        return index == 0 ? 0x000000 : 0xffffff;
                    }

                    @Override
                    public int length() {
                        return 2;
                    }
                });
    }

    public void guiCrop(ActionEvent actionEvent) {

    }

    public void guiResize(ActionEvent actionEvent) {

    }

    public void guiExport(ActionEvent actionEvent) {

    }

    public void guiNokia5110SizePreset(ActionEvent actionEvent) {

    }

    private static FileChooser setupOpenImageDialog() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Select image...");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.ico"),
                new FileChooser.ExtensionFilter("JPEG Images", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("PNG Images", "*.png"),
                new FileChooser.ExtensionFilter("BMP Images", "*.bmp"),
                new FileChooser.ExtensionFilter("ICO Images", "*.ico")
        );

        return fileChooser;
    }
}
