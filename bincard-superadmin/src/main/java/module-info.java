module com.bincard.bincard_superadmin {
    // JavaFX core modules - require sadece, transitive'siz
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.swing;
    requires javafx.media;
    
    // Java standard modules
    requires java.desktop;
    requires java.base;
    requires jdk.jsobject;
    requires java.net.http;
    
    // Third-party libraries
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

    // Opens ve exports
    opens com.bincard.bincard_superadmin to javafx.fxml, javafx.web;
    opens com.bincard.bincard_superadmin.model to javafx.fxml;
    
    exports com.bincard.bincard_superadmin;
    exports com.bincard.bincard_superadmin.model;
}