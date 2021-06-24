package pl.marcinchwedczuk.img2h.gui.logic;

public class MathUtils {
    private MathUtils() { }

    public static int clamp(int value, int min, int max) {
        return value < min ? min :
               value > max ? max :
               value;
    }
}
