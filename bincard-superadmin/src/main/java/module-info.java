module com.bincard.bincard_superadmin {
    // JavaFX core modules
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.web;
    requires transitive javafx.graphics;
    requires transitive javafx.swing;
    requires transitive javafx.media;
    
    // Java standard modules
    requires java.desktop;
    requires java.base;
    requires jdk.jsobject;
    requires java.net.http;
    
    // Third-party libraries (only those with proper modules)
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

    // Opens and exports
    opens com.bincard.bincard_superadmin to javafx.fxml, javafx.web;
    opens com.bincard.bincard_superadmin.model to javafx.fxml;
    
    exports com.bincard.bincard_superadmin;
    exports com.bincard.bincard_superadmin.model;
}