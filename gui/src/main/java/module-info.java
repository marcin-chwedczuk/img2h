module pl.marcinchwedczuk.img2h.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires java.desktop;

    // Shaded non-modular dependencies with synthetic module-info
    requires gui.nonmodular.dependencies;

    exports pl.marcinchwedczuk.img2h.gui;
    exports pl.marcinchwedczuk.img2h.gui.mainwindow;
    exports pl.marcinchwedczuk.img2h.gui.waitdialog;
    exports pl.marcinchwedczuk.img2h.gui.codewindow;
    exports pl.marcinchwedczuk.img2h.gui.aboutdialog;

    // Allow @FXML injection to private fields.
    opens pl.marcinchwedczuk.img2h.gui.mainwindow;
    opens pl.marcinchwedczuk.img2h.gui.waitdialog;
    opens pl.marcinchwedczuk.img2h.gui.codewindow;
    opens pl.marcinchwedczuk.img2h.gui.aboutdialog;
}