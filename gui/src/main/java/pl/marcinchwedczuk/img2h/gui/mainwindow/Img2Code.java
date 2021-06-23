package pl.marcinchwedczuk.img2h.gui.mainwindow;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

public class Img2Code {
    private final BufferedImage image;
    private final ExportFormat format;

    public Img2Code(BufferedImage image, ExportFormat format) {
        this.image = image;
        this.format = format;
    }

    public String convertBlackWhiteToHeader() {
        StringBuilder header = new StringBuilder();

        startVariableDeclaration("glcd_bmp", image, header);

        DataBuffer db = image.getRaster().getDataBuffer();
        int size = db.getSize();
        int curr = 0;

        for (int h = 0; h < image.getHeight(); h++) {
            header.append("\n    "); // append enter and 4 spaces

            for (int w = 0; w < image.getWidth(); w += 8) {
                int maxIndex = Math.min(w + 8, image.getWidth());

                // Take 8 pixels at once
                int bits = 0;
                int pos = 0x80;
                for (int i = w; (i < maxIndex); i++) {
                    bits |= (db.getElem(curr) == 0x000000) ? pos : 0;
                    pos >>= 1;
                    curr++;
                }

                append8Bits(header, bits, format);
                header.append(", ");
            }
        }

        // Remove last ', ' before closing '}'
        if (header.length() > 2 && header.substring(header.length()-2).equals(", ")) {
            header.replace(header.length()-2, header.length(), "");
        }

        finishVariableDeclaration(header);
        return header.toString();
    }

    private void append8Bits(StringBuilder header, int bits, ExportFormat format) {
        switch (format) {
            case C_HEX_LITERAL:
                header.append(String.format("0x%02x", bits & 0xff));
                break;

            case C_BITS_LITERAL:
                String b = Integer.toBinaryString(bits & 0xff);
                b = "00000000".substring(b.length()) + b;
                header.append(String.format("0b%8s", b));
                break;
        }
    }


    private void startVariableDeclaration(String variableName, BufferedImage img, StringBuilder header) {
        // TODO: Use #define
        header.append(String.format("static const unsigned int PROGMEM %s_WIDTH = %d;",
                variableName.toUpperCase(), img.getWidth()));
        header.append("\n");
        header.append(String.format("static const unsigned int PROGMEM %s_HEIGHT = %d;",
                variableName.toUpperCase(), img.getHeight()));
        header.append("\n");
        header.append(String.format("static const unsigned char PROGMEM %s[] = {", variableName));
    }

    private void finishVariableDeclaration(StringBuilder header) {
        header.append("\n};");
    }

}
