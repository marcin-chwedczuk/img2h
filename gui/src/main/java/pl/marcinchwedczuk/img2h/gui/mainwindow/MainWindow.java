package pl.marcinchwedczuk.img2h.gui.mainwindow;

import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.Imaging;
import pl.marcinchwedczuk.img2h.gui.UiService;
import pl.marcinchwedczuk.img2h.gui.aboutdialog.AboutDialog;
import pl.marcinchwedczuk.img2h.gui.codewindow.CodeWindow;
import pl.marcinchwedczuk.img2h.gui.logic.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class MainWindow implements Initializable {


    public static MainWindow showOn(Stage window) {
        try {
            FXMLLoader loader = new FXMLLoader(MainWindow.class.getResource("MainWindow.fxml"));

            Scene scene = new Scene(loader.load());
            MainWindow controller = (MainWindow) loader.getController();

            window.setTitle("img2h");
            window.setScene(scene);
            window.setResizable(true);

            window.show();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private ImageView originalImageView;

    @FXML
    private ImageView lcdImage;

    @FXML
    private Label originalWidthLabel;

    @FXML
    private Label originalHeightLabel;

    @FXML
    private Label formatLabel;

    @FXML
    private RadioButton transparentAsBlackRadioButton;

    @FXML
    private TextField resizeNewWidthText;

    @FXML
    private TextField resizeNewHeightText;

    @FXML
    private ChoiceBox<ResizeAlgorithm> resizeAlgorithmChoice;

    @FXML
    private CheckBox preserveAspectCheckBox;

    @FXML
    private ChoiceBox<BlackWhiteConversionAlgorithm> bwAlgoChoice;

    @FXML
    private Slider bwThresholdSlider;

    @FXML
    private ChoiceBox<ExportFormat> exportFormatChoice;

    @FXML
    private TextField variableNameTextField;

    @FXML
    private ColorPicker lcdImageBackgroundColorPicker;

    @FXML
    private Label lcdImageZoomLabel;

    @FXML
    private Slider lcdImageZoom;

    private final FileChooser openImageFileChooser = FileChoosers.newOpenImageFileChooser();
    private final FileChooser saveHeaderFileChooser = FileChoosers.newSaveHeaderFileChooser();

    private final DebouncingEventHandler<ResizeRequestedEvent> resizeLcdImageCrisp =
            new DebouncingEventHandler<>(Duration.ofMillis(500), this::debouncedLcdImageZoom);

    private BufferedImage originalImage = null;
    private BufferedImage transformedImage = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bwAlgoChoice.getItems().addAll(BlackWhiteConversionAlgorithm.values());
        bwAlgoChoice.setValue(BlackWhiteConversionAlgorithm.DITHERING);

        exportFormatChoice.getItems().addAll(ExportFormat.values());
        exportFormatChoice.setValue(ExportFormat.C_BITS_LITERAL);

        resizeAlgorithmChoice.getItems().addAll(ResizeAlgorithm.values());
        resizeAlgorithmChoice.setValue(ResizeAlgorithm.SMOOTH);

        variableNameTextField.setText("image");

        lcdImageZoomLabel.textProperty().bind(Bindings.createStringBinding(
                () -> String.format("%3.0f%%", lcdImageZoom.getValue()),
                lcdImageZoom.valueProperty()));

        lcdImageZoom.valueProperty().addListener((observable, oldZoom, newZoom) -> {
            fastLcdImageZoom((double)newZoom);
            resizeLcdImageCrisp.handle(new ResizeRequestedEvent((double)newZoom));
        });

        // Some values that are not reset while loading an image
        lcdImageZoom.setValue(100);
        lcdImageBackgroundColorPicker.setValue(Color.WHITE);

        exportFormatChoice.setValue(ExportFormat.C_BITS_LITERAL);

        loadImage(new File(getClass().getResource("default-image.png").getFile()));

        guiNokia5110SizePreset();
        resizeAlgorithmChoice.setValue(ResizeAlgorithm.SMOOTH);
    }

    private void fastLcdImageZoom(double zoom) {
        // Zoom like this will result in blurred image
        lcdImage.setFitWidth((int)Math.round(transformedImage.getWidth() * zoom / 100.0));
        lcdImage.setFitHeight((int)Math.round(transformedImage.getHeight() * zoom / 100.0));
    }

    private void debouncedLcdImageZoom(ResizeRequestedEvent event) {
        setLcdImageWithPixelizedZoom(transformedImage, event.zoom);
    }

    /**
     * @param image
     *  Image that should be displayed.
     * @param zoom
     *  Image zoom in percents e.g. 200 means that image should be twice as big (x2).
     */
    private void setLcdImageWithPixelizedZoom(BufferedImage image, double zoom) {
        BufferedImage scaled = BufferedImageUtils.resizeImageWithoutAntialiasing(image,
                (int)Math.round(image.getWidth() * zoom / 100.0),
                (int)Math.round(image.getHeight() * zoom / 100.0));

        Image transformedFxImage = SwingFXUtils.toFXImage(scaled, null);
        this.lcdImage.setImage(transformedFxImage);

        this.lcdImage.setFitWidth(transformedFxImage.getWidth());
        this.lcdImage.setFitHeight(transformedFxImage.getHeight());
    }

    @FXML
    private void guiOpenImage() {
        File file = openImageFileChooser.showOpenDialog(thisWindow());
        if (file != null) {
            loadImage(file);
            runTransformation();
        }
    }

    @FXML
    private void guiSaveHeader() throws IOException {
        File headerFile = saveHeaderFileChooser.showSaveDialog(thisWindow());
        if (headerFile != null) {
            String header = convertImageToHeader();
            Files.writeString(headerFile.toPath(), header,
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        }
    }

    public void guiDragOverImageContainer(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            List<File> files = event.getDragboard().getFiles();
            if (files.size() == 1 && files.get(0).isFile()) {
                String extension = com.google.common.io.Files.getFileExtension(files.get(0).getName());
                if (Set.of("bmp", "ico", "jpg", "jpeg", "png").contains(extension.toLowerCase())) {
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
            originalImage = ImageIO.read(imageFile);

            formatLabel.setText(format.toString());
            originalWidthLabel.setText(originalImage.getWidth() + " px");
            originalHeightLabel.setText(originalImage.getHeight() + " px");

            Image image = SwingFXUtils.toFXImage(originalImage, null);
            this.originalImageView.setImage(image);
            this.originalImageView.setFitWidth(image.getWidth());
            this.originalImageView.setFitHeight(image.getHeight());

            bwAlgoChoice.setValue(BlackWhiteConversionAlgorithm.DITHERING);
            bwThresholdSlider.setValue(50.0);

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
            BufferedImage workingCopy = BufferedImageUtils.copy(originalImage);

            workingCopy = BufferedImageUtils.removeAlpha(workingCopy,
                    this.transparentAsBlackRadioButton.isSelected()
                            ? java.awt.Color.BLACK
                            : java.awt.Color.WHITE);

            workingCopy = resizeImage(workingCopy);

            double threshold = bwThresholdSlider.getValue();
            BlackWhiteConversionAlgorithm bwAlgorithm = bwAlgoChoice.getValue();
            BufferedImageUtils.convertToBlackAndWhiteInPlace(workingCopy, bwAlgorithm, threshold);

            int backgroundColor = BufferedImageUtils.colorToInt(lcdImageBackgroundColorPicker.getValue());
            workingCopy = BufferedImageUtils.replaceWhiteColor(workingCopy, backgroundColor);

            this.transformedImage = workingCopy;

            double zoom = lcdImageZoom.getValue();
            setLcdImageWithPixelizedZoom(transformedImage, zoom);
        } catch (Exception e) {
            UiService.errorDialog("Failure: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private BufferedImage resizeImage(BufferedImage workingCopy) {
        int newW = Integer.parseInt(resizeNewWidthText.getText());
        int newH = Integer.parseInt(resizeNewHeightText.getText());

        if (preserveAspectCheckBox.isSelected()) {
            double srcAspect = (double)workingCopy.getHeight() / workingCopy.getWidth();
            double destAspect = (double)newH / newW;

            if (srcAspect >= destAspect) {
                newW = MathUtils.clamp((int)Math.round(newH / srcAspect), 0, newW);
            }
            else {
                newH = MathUtils.clamp((int)Math.round(newW * srcAspect), 0, newH);
            }
        }

        return BufferedImageUtils.resizedImage(workingCopy,
                newW, newH,
                resizeAlgorithmChoice.getValue());
    }

    private String convertImageToHeader() {
        runTransformation();

        String code = new Img2CodeConverter(
                transformedImage,
                exportFormatChoice.getValue(),
                variableNameTextField.getText()).convertImageToHeader();
        return code;
    }

    @FXML
    private void guiCrop(ActionEvent actionEvent) {
        runTransformation();
    }

    @FXML
    private void guiResize(ActionEvent actionEvent) {
        runTransformation();
    }

    @FXML
    private void guiNokia5110SizePreset() {
        int height = 48, width = 84;
        resizeNewWidthText.setText(Integer.toString(width));
        resizeNewHeightText.setText(Integer.toString(height));
        preserveAspectCheckBox.setSelected(true);
    }

    @FXML
    private void guiLcdBackgroundChanged() {
        runTransformation();
    }

    @FXML
    private void openCode() {
        String code = convertImageToHeader();
        CodeWindow.show(thisWindow(), code);
    }

    @FXML
    private void guiShowAbout() {
        AboutDialog.show(thisWindow());
    }

    @FXML
    private void guiClose() {
        thisWindow().close();
    }

    private Stage thisWindow() {
        return (Stage)this.bwAlgoChoice.getScene().getWindow();
    }
}
