module com.bincard.bincard_superadmin {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires transitive javafx.graphics;
    
    requires java.desktop;
    requires jdk.jsobject;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    
    // FontAwesome için gerekli modüller
    requires transitive org.kordamp.ikonli.fontawesome5;
    requires transitive org.kordamp.ikonli.core;

    opens com.bincard.bincard_superadmin to javafx.fxml, javafx.web;
    exports com.bincard.bincard_superadmin;
    exports com.bincard.bincard_superadmin.model;
}