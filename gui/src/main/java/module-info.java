module pl.marcinchwedczuk.img2h.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires java.desktop;

    // forced to be modular - see ./modularized directory
    requires _modularized_.com.google.common;
    requires _modularized_.org.apache.commons.imaging;

    exports pl.marcinchwedczuk.img2h.gui;
    exports pl.marcinchwedczuk.img2h.gui.mainwindow;
    exports pl.marcinchwedczuk.img2h.gui.waitdialog;
    exports pl.marcinchwedczuk.img2h.gui.codewindow;
    exports pl.marcinchwedczuk.img2h.gui.aboutdialog;
    exports pl.marcinchwedczuk.img2h.gui.testfilechooser;

    // Allow @FXML injection to private fields.
    opens pl.marcinchwedczuk.img2h.gui.mainwindow;
    opens pl.marcinchwedczuk.img2h.gui.waitdialog;
    opens pl.marcinchwedczuk.img2h.gui.codewindow;
    opens pl.marcinchwedczuk.img2h.gui.aboutdialog;
    opens pl.marcinchwedczuk.img2h.gui.testfilechooser;
}