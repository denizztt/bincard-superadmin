package com.bincard.bincard_superadmin;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.IkonResolver;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public class HelloApplication extends Application {
    private static HostServices appHostServices;

    @Override
    public void start(Stage primaryStage) {
        // Ikonli FontAwesome modülünü yükle
        initializeIkonli();
        
        appHostServices = getHostServices();
        
        // Kayıtlı token'ları kontrol et
        try {
            System.out.println("Kayıtlı token'lar kontrol ediliyor...");
            TokenResponse savedTokens = ApiClientFX.getSavedTokens();
            
            if (savedTokens != null) {
                System.out.println("Geçerli token'lar bulundu, doğrudan dashboard'a yönlendiriliyor.");
                new SuperadminDashboardFX(primaryStage, savedTokens.getAccessToken(), savedTokens.getRefreshToken());
            } else {
                System.out.println("Geçerli token bulunamadı, giriş ekranına yönlendiriliyor.");
                new MainMenuFX(primaryStage);
            }
        } catch (Exception e) {
            System.err.println("Token kontrolü sırasında hata: " + e.getMessage());
            e.printStackTrace();
            // Hata durumunda normal giriş ekranına yönlendir
            new MainMenuFX(primaryStage);
        }
        
        primaryStage.show();
    }
    
    private void initializeIkonli() {
        try {
            System.out.println("FontAwesome ikonları yükleniyor...");
            // IkonResolver sınıfından bir örnek almak, modüllerin doğru yüklendiğini doğrular
            IkonResolver.getInstance();
            // Test amaçlı bir ikon çağırma - bu satır sayesinde FontAwesome sınıfının
            // sınıf yükleyicisi tarafından başlatılmasını zorlarız
            FontAwesomeSolid.BUS.getDescription();
            System.out.println("FontAwesome ikonları başarıyla yüklendi.");
        } catch (Exception e) {
            System.err.println("FontAwesome ikonları yüklenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public static HostServices getAppHostServices() {
        return appHostServices;
    }
}