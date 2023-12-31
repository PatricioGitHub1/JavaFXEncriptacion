package com.example;

import java.io.File;

import com.example.AppData.fileType;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class EncryptViewController {
    AppData appData = AppData.getInstance();
    File selectedFile;
    File targetFile;
    File publicKeyFile;

    @FXML
    Label selectedFileLabel;
    @FXML
    Label targetFileLabel;
    @FXML
    Label publicKeyLabel;

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
    public void setPublicKeyFile() {
        publicKeyFile = appData.filePicker();
        if (publicKeyFile != null) {
            publicKeyLabel.setText(publicKeyFile.getName());
        }
    }

    @FXML
    public void encryptFiles() {
        if (appData.fileEncryption(publicKeyFile, selectedFile, targetFile)) {
            System.out.println("Se ha podido encriptar");
        } else {
            System.out.println("No se ha podido encriptar");
        }
    }

}
