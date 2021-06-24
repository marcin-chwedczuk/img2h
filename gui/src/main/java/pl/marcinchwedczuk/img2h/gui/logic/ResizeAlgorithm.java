package pl.marcinchwedczuk.img2h.gui.logic;

import java.awt.*;

public enum ResizeAlgorithm {
    FAST(Image.SCALE_FAST),
    SMOOTH(Image.SCALE_SMOOTH),
    REPLICATE(Image.SCALE_REPLICATE),
    AREA_AVERAGING(Image.SCALE_AREA_AVERAGING);

    private int scalingAlgorithm;
    ResizeAlgorithm(int scalingAlgorithm) {
        this.scalingAlgorithm = scalingAlgorithm;
    }
    public int scalingAlgorithm() { return scalingAlgorithm; }
}
