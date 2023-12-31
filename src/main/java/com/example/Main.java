package com.example;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        final int windowWidth = 800;
        final int windowHeight = 600;

        UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
        UtilsViews.addView(getClass(), "View0", "/assets/MainView.fxml");
        UtilsViews.addView(getClass(), "ViewDecrypt", "/assets/DecryptView.fxml");
        UtilsViews.addView(getClass(), "ViewEncrypt", "/assets/EncryptView.fxml");

        Scene scene = new Scene(UtilsViews.parentContainer);
        
        stage.setScene(scene);
        stage.setTitle("Animació entre vistes");
        stage.setMinWidth(windowWidth);
        stage.setMinHeight(windowHeight);
        stage.show();

        // Add icon only if not Mac
        if (!System.getProperty("os.name").contains("Mac")) {
            Image icon = new Image("file:/icons/icon.png");
            stage.getIcons().add(icon);
        }
    }
}