module pl.marcinchwedczuk.img2h.gui {
    requires pl.marcinchwedczuk.img2h.domain;

    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.imaging;
    requires java.desktop;

    exports pl.marcinchwedczuk.img2h.gui;
    exports pl.marcinchwedczuk.img2h.gui.mainwindow;

    // Allow @FXML injection to private fields.
    opens pl.marcinchwedczuk.img2h.gui.mainwindow;
}