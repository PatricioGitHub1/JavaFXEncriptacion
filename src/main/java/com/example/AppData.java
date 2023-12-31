package com.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Date;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AppData {
    private static AppData instance;
    String privateKeyPassword = "";

    enum fileType {
        PRIVATE_KEY_FILE,
        PUBLIC_KEY_FILE,
        TARGET_FILE
    }

    private AppData() {
    }

    public static synchronized AppData getInstance() {
        if (instance == null) {
            instance = new AppData();
        }
        return instance;
    }

    public File filePicker() {
        FileChooser fileChooser = new FileChooser();

        return (fileChooser.showOpenDialog(null));
    }

    public File fileSaver(fileType type) {
        FileChooser fileChooser = new FileChooser();
        if (type == fileType.PRIVATE_KEY_FILE) {
            fileChooser.setTitle("New Private Key");
            fileChooser.setInitialFileName("private_key.key");

        } else if (type == fileType.PUBLIC_KEY_FILE) {
            fileChooser.setTitle("New Public Key");
            fileChooser.setInitialFileName("public_key.pub");

        } else if (type == fileType.TARGET_FILE) {
            fileChooser.setTitle("Target File");
            fileChooser.setInitialFileName("target.txt");
        }

        return fileChooser.showSaveDialog(null);

    }

    public void createKeyFiles() {
        try {
            File publicKeyFile = fileSaver(fileType.PUBLIC_KEY_FILE);
            publicKeyFile.createNewFile();
            
            File privateKeyFile = fileSaver(fileType.PRIVATE_KEY_FILE);
            privateKeyFile.createNewFile();

            buildKeys(publicKeyFile, privateKeyFile);

        } catch (IOException | NullPointerException e) {
            System.out.println("Couldnt'create all keys");
        }
    }

    public void buildKeys(File publicKeyFile, File privateKeyFile) {
        getPassphrase();
        System.out.println("PRIVATE KEY PASS : ["+privateKeyPassword+"]");

        // Construir las keys
        try {
            KeyPair keyPair = generateKeyPair();
            PGPKeyPair pgpKeyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, keyPair, new Date());

            // Save private Key
            PGPSecretKey secretKey = new PGPSecretKey(PGPSignature.DEFAULT_CERTIFICATION, pgpKeyPair, privateKeyPassword, null, null, new JcaPGPContentSignerBuilder(pgpKeyPair.getPublicKey().getAlgorithm(), PGPUtil.SHA256), 
            new JcePBESecretKeyEncryptorBuilder(SymmetricKeyAlgorithmTags.CAST5)
                .setProvider("BC")
                .build(privateKeyPassword.toCharArray())); 


            try (ArmoredOutputStream armoredOutputStream = new ArmoredOutputStream(new FileOutputStream(privateKeyFile))) {
                secretKey.encode(armoredOutputStream);
            }
            
            

            // Save public key (included in the secret key)
            PGPPublicKeyRing publicKeyRing = new PGPPublicKeyRing(secretKey.getPublicKey().getEncoded(), new JcaKeyFingerprintCalculator());
            try (ArmoredOutputStream armoredOutputStream = new ArmoredOutputStream(new FileOutputStream(publicKeyFile))) {
                publicKeyRing.encode(armoredOutputStream);
            }

            System.out.println("Keys generated !");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    // Modal para sacar contraseña private key
    public void getPassphrase() {
        Stage passphraseStage = new Stage();
        passphraseStage.initModality(Modality.APPLICATION_MODAL);
        passphraseStage.setTitle("Enter Passphrase");

        PasswordField passphraseField = new PasswordField();
        passphraseField.setPromptText("Enter passphrase");

        Label errorLabel = new Label("Passphrase cannot be empty");
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        Button confirmButton = new Button("Confirm");
        confirmButton.setOnAction(e -> {
            if (passphraseField.getText().isBlank()) {
                errorLabel.setVisible(true);
            } else {
                privateKeyPassword = passphraseField.getText();
                passphraseStage.close();
            }
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(new Label("Enter Passphrase:"), passphraseField, errorLabel, confirmButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 250, 200);
        passphraseStage.setScene(scene);
        passphraseStage.showAndWait();
    }

    public boolean fileEncryption(File publicKeyFile, File fileToEncrypt, File encryptedFilePath) {
        return false;
    }

}
