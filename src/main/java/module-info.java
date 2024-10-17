module azari.amirhossein.dfa_minimization {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens azari.amirhossein.dfa_minimization to javafx.fxml;
    exports azari.amirhossein.dfa_minimization;
    exports azari.amirhossein.dfa_minimization.utils;
    opens azari.amirhossein.dfa_minimization.utils to javafx.fxml;
}