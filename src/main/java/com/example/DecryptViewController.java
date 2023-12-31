package com.example;

import java.io.File;

import com.example.AppData.fileType;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class DecryptViewController {
    AppData appData = AppData.getInstance();
    File selectedFile;
    File targetFile;
    File privateKeyFile;

    @FXML
    Label selectedFileLabel;
    @FXML
    Label targetFileLabel;
    @FXML
    Label privateKeyLabel;
    @FXML
    PasswordField passwordField;
    
    @FXML
    public void goMainView(ActionEvent event) {
        UtilsViews.setViewAnimating("View0");
    }

    @FXML
    public void setSelectedFile() {
        selectedFile = appData.filePicker();
        if (selectedFile != null) {
            selectedFileLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    public void setTargetFile() {
        targetFile = appData.fileSaver(fileType.TARGET_FILE);
        if (targetFile != null) {
            targetFileLabel.setText(targetFile.getName());
        }
    }

    @FXML
    public void setPrivateKeyFile() {
        privateKeyFile = appData.filePicker();
        if (privateKeyFile != null) {
            privateKeyLabel.setText(privateKeyFile.getName());
        }
    }

    @FXML
    public void decryptFiles() {
        System.out.println("decrypt with password:["+passwordField.getText()+"]");
    }
}
