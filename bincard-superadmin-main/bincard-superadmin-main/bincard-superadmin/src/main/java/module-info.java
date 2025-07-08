module com.bincard.bincard_superadmin {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires transitive javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    
    // FontAwesome için gerekli modüller
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.core;

    opens com.bincard.bincard_superadmin to javafx.fxml;
    exports com.bincard.bincard_superadmin;
}