package com.bincard.bincard_superadmin;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    private static HostServices appHostServices;

    @Override
    public void start(Stage primaryStage) {
        appHostServices = getHostServices();
        new MainMenuFX(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public static HostServices getAppHostServices() {
        return appHostServices;
    }
}