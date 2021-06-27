package pl.marcinchwedczuk.img2h.gui.logic;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.Objects;

public class Img2CodeConverter {
    private final BufferedImage image;
    private final ExportFormat format;
    private final String variableName;

    public Img2CodeConverter(BufferedImage image,
                             ExportFormat format,
                             String variableName) {
        this.image = Objects.requireNonNull(image);
        this.format = Objects.requireNonNull(format);
        this.variableName = Objects.requireNonNull(variableName);
    }

    public String convertImageToHeader() {
        StringBuilder headerFile = new StringBuilder();

        defineWidthAndHeightConstants(headerFile);
        startPixelsArrayDeclaration(headerFile);

        DataBuffer db = image.getRaster().getDataBuffer();
        int curr = 0;

        for (int row = 0; row < image.getHeight(); row++) {
            headerFile.append("\n    "); // append enter and 4 spaces

            for (int byteStart = 0; byteStart < image.getWidth(); byteStart += 8) {
                int maxIndex = Math.min(byteStart + 8, image.getWidth());

                // Take 8 pixels at once
                int bits = 0;
                int pos = 0x80;
                for (int i = byteStart; i < maxIndex; i++) {
                    bits |= (db.getElem(curr) == 0x000000) ? pos : 0;
                    pos >>= 1;
                    curr++;
                }

                append8Bits(headerFile, bits, format);
                headerFile.append(", ");
            }
        }

        // Remove last ', ' before closing '}'
        if (headerFile.length() > 2 && headerFile.substring(headerFile.length()-2).equals(", ")) {
            headerFile.replace(headerFile.length()-2, headerFile.length(), "");
        }

        finishPixelsArrayDeclaration(headerFile);
        return headerFile.toString();
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

    private void defineWidthAndHeightConstants(StringBuilder header) {
        header.append(String.format("#define %s_WIDTH (%d)",
                variableName.toUpperCase(), image.getWidth()));
        header.append("\n");

        header.append(String.format("#define %s_HEIGHT (%d)",
                variableName.toUpperCase(), image.getHeight()));
        header.append("\n");
    }

    private void startPixelsArrayDeclaration(StringBuilder header) {
        header.append(String.format("static const unsigned char PROGMEM %s[] = {", variableName));
    }

    private void finishPixelsArrayDeclaration(StringBuilder header) {
        header.append("\n};");
    }

}
