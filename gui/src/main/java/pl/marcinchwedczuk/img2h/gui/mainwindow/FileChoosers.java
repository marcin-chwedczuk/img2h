package pl.marcinchwedczuk.img2h.gui.mainwindow;

import javafx.stage.FileChooser;

import java.io.File;

public class FileChoosers {
    public static FileChooser createOpenImageDialog() {
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
