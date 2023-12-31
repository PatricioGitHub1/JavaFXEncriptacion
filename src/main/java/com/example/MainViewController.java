package com.example;

import java.net.URL;
import java.util.ResourceBundle;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

public class MainViewController implements Initializable{      
    AppData appData = AppData.getInstance();
    @FXML
    Button encryptButton;
    @FXML
    Button decryptButton;
    @FXML
    Button keyButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        encryptButton.setOnAction((event) -> {
            UtilsViews.setViewAnimating("ViewEncrypt");

        });

        decryptButton.setOnAction((event) -> { 
            UtilsViews.setViewAnimating("ViewDecrypt");
        });

        keyButton.setOnAction((event) -> { 
            appData.createKeyFiles();
        });
    } 
}