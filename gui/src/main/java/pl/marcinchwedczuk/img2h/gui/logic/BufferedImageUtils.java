package pl.marcinchwedczuk.img2h.gui.logic;

import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.color.ColorConversions;
import org.apache.commons.imaging.palette.Palette;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

public class BufferedImageUtils {
    private BufferedImageUtils() { }

    public static BufferedImage removeAlpha(BufferedImage img, java.awt.Color replacementColor) {
        // Notice type RGB - no alpha channel
        BufferedImage tmp = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = tmp.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // Notice that we drop transparency here if it was present in the original image
        g2d.setColor(replacementColor);
        g2d.fillRect(0, 0, img.getWidth(), img.getHeight());

        // Create image copy
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return tmp;
    }

    public static BufferedImage resizedImage(BufferedImage img, int newW, int newH, ResizeAlgorithm algo) {
        java.awt.Image tmp = img.getScaledInstance(newW, newH, algo.scalingAlgorithm());

        BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resized;
    }

    public static BufferedImage resizeImageWithoutAntialiasing(BufferedImage img, int newW, int newH) {
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.drawImage(img, 0, 0, newW, newH, null);
        g2d.dispose();

        return dimg;
    }

    public static int colorToInt(javafx.scene.paint.Color c) {
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return (r << 16) | (g << 8) | b;
    }

    public static BufferedImage copy(BufferedImage bufferImage) {
        ColorModel colorModel = bufferImage.getColorModel();
        WritableRaster raster = bufferImage.copyData(null);
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
    }

    public static BufferedImage replaceWhiteColor(BufferedImage original, int newColor) {
        DataBuffer db = original.getRaster().getDataBuffer();
        for (int i = 0; i < db.getSize(); i++) {
            int rgb = db.getElem(i);
            if (rgb != 0) {
                db.setElem(i, newColor);
            }
        }
        return original;
    }

    public static BufferedImage cropImage(BufferedImage img,
                                          int cropTop, int cropBottom, int cropLeft, int cropRight) {
        return img.getSubimage(
                cropLeft,
                cropTop,
                img.getWidth() - cropLeft - cropRight,
                img.getHeight() - cropTop - cropBottom);
    }

    public static BufferedImage convertToBlackAndWhiteInPlace(BufferedImage image,
                                                              BlackWhiteConversionAlgorithm bwAlgorithm,
                                                              double threshold) throws Exception {
        double normalizedThreshold = threshold / 100.0; // normalize [0,100] -> [0,1]
        switch (bwAlgorithm) {
            case DITHERING:
                return applyDithering(image, normalizedThreshold);

            case THRESHOLD:
                return applyThreshold(image, normalizedThreshold);

            default:
                throw new IllegalArgumentException("bwAlgorithm");
        }
    }

    public static BufferedImage applyDithering(BufferedImage workingCopy, double normalizedThreshold) throws ImageWriteException {
        // image is updated in place
        org.apache.commons.imaging.palette.Dithering.applyFloydSteinbergDithering(workingCopy,
                new Palette() {
                    @Override
                    public int getPaletteIndex(int rgb) throws ImageWriteException {
                        if (ColorConversions.convertRGBtoHSL(rgb).L < normalizedThreshold) {
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

        return workingCopy;
    }

    private static BufferedImage applyThreshold(BufferedImage original, double normalizedThreshold) {
        DataBuffer db = original.getRaster().getDataBuffer();
        for (int i = 0; i < db.getSize(); i++) {
            int rgb = db.getElem(i);
            if (ColorConversions.convertRGBtoHSL(rgb).L < normalizedThreshold) {
                db.setElem(i, 0x000000);
            }
            else {
                db.setElem(i, 0xffffff);
            }
        }
        return original;
    }
}
