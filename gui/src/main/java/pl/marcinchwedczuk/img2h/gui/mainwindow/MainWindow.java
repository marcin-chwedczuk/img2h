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
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.color.ColorConversions;
import org.apache.commons.imaging.palette.Palette;

import javax.imageio.ImageIO;
import java.awt.*;
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
    public ChoiceBox<ResizeAlgorithm> resizeAlgorithmChoice;

    @FXML
    private ChoiceBox<BlackWhiteAlgorithm> bwAlgoChoice;

    @FXML
    public ChoiceBox<ExportFormat> exportFormatChoice;

    private FileChooser openImageDialog = setupOpenImageDialog();
    private BufferedImage originalImage = null;

    @FXML
    private Rectangle cropShadowTop;

    @FXML
    private Rectangle cropShadowBottom;

    @FXML
    private Rectangle cropShadowLeft;

    @FXML
    private Rectangle cropShadowRight;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bwAlgoChoice.getItems().addAll(BlackWhiteAlgorithm.values());
        bwAlgoChoice.setValue(BlackWhiteAlgorithm.DITHERING);

        exportFormatChoice.getItems().addAll(ExportFormat.values());
        exportFormatChoice.setValue(ExportFormat.C_BITS_LITERAL);

        resizeAlgorithmChoice.getItems().addAll(ResizeAlgorithm.values());
        resizeAlgorithmChoice.setValue(ResizeAlgorithm.SMOOTH);
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

    private void setCropShadow(int imageWidth, int imageHeight,
                               int cropTop, int cropBottom, int cropLeft, int cropRight) {
        this.cropShadowTop.setX(0);
        this.cropShadowTop.setY(0);
        this.cropShadowTop.setWidth(imageWidth);
        this.cropShadowTop.setHeight(cropTop);

        this.cropShadowBottom.setX(0);
        this.cropShadowBottom.setY(imageHeight - cropBottom);
        this.cropShadowBottom.setWidth(imageWidth);
        this.cropShadowBottom.setHeight(cropBottom);

        this.cropShadowLeft.setX(0);
        this.cropShadowLeft.setY(cropTop);
        this.cropShadowLeft.setWidth(cropLeft);
        this.cropShadowLeft.setHeight(imageHeight - cropTop - cropBottom);

        this.cropShadowRight.setX(imageWidth - cropRight);
        this.cropShadowRight.setY(cropTop);
        this.cropShadowRight.setWidth(cropRight);
        this.cropShadowRight.setHeight(imageHeight - cropTop - cropBottom);
    }

    private void setCropBounds(int cropTop, int cropBottom, int cropLeft, int cropRight) {
        cropTopText.setText(Integer.toString(cropTop));
        cropDownText.setText(Integer.toString(cropBottom));
        cropLeftText.setText(Integer.toString(cropLeft));
        cropRightText.setText(Integer.toString(cropRight));
    }

    private void loadImage(File imageFile) {
        try {
            ImageFormat format = Imaging.guessFormat(imageFile);
            try {
                originalImage = Imaging.getBufferedImage(imageFile);
            } catch (ImageReadException e) {
                e.printStackTrace();
                originalImage = ImageIO.read(imageFile);
            }

            formatLabel.setText(format.toString());
            originalWidthLabel.setText(originalImage.getWidth() + " px");
            originalHeightLabel.setText(originalImage.getHeight() + " px");

            Image image = SwingFXUtils.toFXImage(originalImage, null);
            this.originalImageView.setImage(image);
            this.originalImageView.setFitWidth(image.getWidth());
            this.originalImageView.setFitHeight(image.getHeight());

            setCropBounds(0, 0, 0, 0);
            setCropShadow(originalImage.getWidth(), originalImage.getHeight(), 0, 0, 0, 0);

            resizeNewWidthText.setText(Integer.toString(originalImage.getWidth()));
            resizeNewHeightText.setText(Integer.toString(originalImage.getHeight()));
            resizeAlgorithmChoice.setValue(ResizeAlgorithm.SMOOTH);

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
            BufferedImage workingCopy = createCopy(originalImage);

            int
                    cropTop = Integer.parseInt(cropTopText.getText()),
                    cropBottom = Integer.parseInt(cropDownText.getText()),
                    cropLeft = Integer.parseInt(cropLeftText.getText()),
                    cropRight = Integer.parseInt(cropRightText.getText());
            workingCopy = createCroppedImage(workingCopy, cropTop, cropBottom, cropLeft, cropRight);
            setCropShadow(
                    originalImage.getWidth(), originalImage.getHeight(),
                    cropTop, cropBottom, cropLeft, cropRight);

            workingCopy = createResizedImage(workingCopy,
                    Integer.parseInt(resizeNewWidthText.getText()),
                    Integer.parseInt(resizeNewHeightText.getText()),
                    resizeAlgorithmChoice.getValue());

            convertToBlackAndWhite(workingCopy);

            Image transformedFxImage = SwingFXUtils.toFXImage(workingCopy, null);
            this.lcdImage.setImage(transformedFxImage);
            this.lcdImage.setFitWidth(transformedFxImage.getWidth());
            this.lcdImage.setFitHeight(transformedFxImage.getHeight());
        } catch (Exception e) {
            UiService.errorDialog("Failure: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static BufferedImage createCopy(BufferedImage bufferImage) {
        ColorModel colorModel = bufferImage.getColorModel();
        WritableRaster raster = bufferImage.copyData(null);
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
    }

    public static BufferedImage createResizedImage(BufferedImage img, int newW, int newH, ResizeAlgorithm algo) {
        java.awt.Image tmp = img.getScaledInstance(newW, newH, algo.scalingAlgorithm());
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    public static BufferedImage createCroppedImage(BufferedImage img, int cropTop, int cropBottom, int cropLeft, int cropRight) {
        return img.getSubimage(
                cropLeft,
                cropTop,
                img.getWidth() - cropLeft - cropRight,
                img.getHeight() - cropTop - cropBottom);
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
        runTransformation();
    }

    public void guiResize(ActionEvent actionEvent) {
        runTransformation();
    }

    public void guiExport(ActionEvent actionEvent) {
        runTransformation();
    }

    public void guiNokia5110SizePreset(ActionEvent actionEvent) {
        int height = 48, width = 84;
        resizeNewWidthText.setText(Integer.toString(width));
        resizeNewHeightText.setText(Integer.toString(height));
        cropToAspectRatio(width, height);
    }

    private void cropToAspectRatio(int aspectWidth, int aspectHeight) {
        double requestedAspect = (double)aspectHeight / aspectWidth;
        double actualAspect = (double)originalImage.getHeight() / originalImage.getWidth();

        if (actualAspect >= requestedAspect) {
            // image higher than expected
            int newWidth = originalImage.getWidth();
            int newHeight = (int)(requestedAspect * newWidth + 0.5);
            int cropTop = (originalImage.getHeight() - newHeight) / 2;
            int cropBottom = originalImage.getHeight() - newHeight - cropTop;
            setCropBounds(cropTop, cropBottom, 0, 0);
            setCropShadow(originalImage.getWidth(), originalImage.getHeight(),
                    cropTop, cropBottom, 0, 0);
        }
        else {
            // image wider than expected
            int newHeight = originalImage.getHeight();
            int newWidth = (int)(newHeight * 1.0 / requestedAspect);
            int cropLeft = (originalImage.getWidth() - newWidth) / 2;
            int cropRight = originalImage.getWidth() - newWidth - cropLeft;
            setCropBounds(0, 0, cropLeft, cropRight);
            setCropShadow(originalImage.getWidth(), originalImage.getHeight(),
                    0, 0, cropLeft, cropRight);
        }
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

    private int cropMoveTop, cropMoveBottom, cropMoveLeft, cropMoveRight;
    private int cropMoveStartX, cropMoveStartY;
    private boolean cropMoveInProgress = false;

    @FXML
    private void guiMoveCropInProgress(MouseEvent mouseEvent) {
        System.out.println("MOVE");
        if (!cropMoveInProgress) {
            return;
        }

        int deltaX = (int)mouseEvent.getX() - cropMoveStartX;
        int deltaY = (int)mouseEvent.getY() - cropMoveStartY;
        deltaX = clamp(deltaX, -cropMoveLeft, cropMoveRight);
        deltaY = clamp(deltaY, -cropMoveTop, cropMoveBottom);

        setCropBounds(cropMoveTop + deltaY, cropMoveBottom - deltaY,
                cropMoveLeft + deltaX, cropMoveRight - deltaX);

        setCropShadow(originalImage.getWidth(), originalImage.getHeight(),
                cropMoveTop + deltaY, cropMoveBottom - deltaY,
                cropMoveLeft + deltaX, cropMoveRight - deltaX);
    }

    private int clamp(int value, int min, int max) {
        return value < min ? min :
               value > max ? max :
               value;
    }

    @FXML
    private void guiMoveCropStart(MouseEvent mouseEvent) {
        cropMoveInProgress = true;
        cropMoveStartX = (int)mouseEvent.getX();
        cropMoveStartY = (int)mouseEvent.getY();

        cropMoveTop = Integer.parseInt(cropTopText.getText());
        cropMoveBottom = Integer.parseInt(cropDownText.getText());
        cropMoveLeft = Integer.parseInt(cropLeftText.getText());
        cropMoveRight = Integer.parseInt(cropRightText.getText());
    }

    @FXML
    private void guiMoveCropStop(MouseEvent mouseEvent) {
        cropMoveInProgress = false;
    }
}
