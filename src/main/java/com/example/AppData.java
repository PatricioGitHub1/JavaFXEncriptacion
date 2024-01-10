package com.example;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.jcajce.JcaPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.apache.commons.io.IOUtils;
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

        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZLIB);
            byte[] publicKbytes = Files.readAllBytes(Paths.get(publicKeyFile.toURI()));
            InputStream in= new ByteArrayInputStream(publicKbytes);
            in = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(in);

            JcaPGPPublicKeyRingCollection pgpPub = new JcaPGPPublicKeyRingCollection(in);
            in.close();

            PGPPublicKey publicK = null;
            Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();
            while (publicK == null && rIt.hasNext())
            {
                PGPPublicKeyRing kRing = rIt.next();
                Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
                while (publicK == null && kIt.hasNext())
                {
                    PGPPublicKey k = kIt.next();

                    if (k.isEncryptionKey())
                    {
                        publicK = k;
                    }
                }
            }
            PGPUtil.writeFileToLiteralData(comData.open(bOut), PGPLiteralData.BINARY, fileToEncrypt);
            comData.close();
            PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(
            new BcPGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.CAST5).setSecureRandom(new SecureRandom())
                    .setWithIntegrityPacket(true));
            cPk.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(publicK));

            byte[] bytes = bOut.toByteArray();

            OutputStream cOut;
            OutputStream out = new FileOutputStream(encryptedFilePath);
            cOut = cPk.open(out, bytes.length);
            cOut.write(bytes);
            cOut.close();
            out.close();
            return true;
        } catch (IOException e) {
                
            e.printStackTrace();
        } catch (PGPException e) {
        
            e.printStackTrace();
        }
        
        return false;
    }

    public boolean fileDecryption(File privateKeyFile, File fileToDecrypt, File decryptedFilePath, String password) {
        try {
            FileInputStream inputStreamFileToDecrypt = new FileInputStream(fileToDecrypt);
            FileOutputStream outputStreamDecryptedFile = new FileOutputStream(decryptedFilePath);
            
            InputStream encryptedIn = PGPUtil.getDecoderStream(inputStreamFileToDecrypt);
            JcaPGPObjectFactory pgpObjectFactory = new JcaPGPObjectFactory(encryptedIn);

            Object obj = pgpObjectFactory.nextObject();
            PGPEncryptedDataList encryptedDataList = (obj instanceof PGPEncryptedDataList)
                            ? (PGPEncryptedDataList) obj : (PGPEncryptedDataList) pgpObjectFactory.nextObject();
            
            PGPPrivateKey pgpPrivateKey = null;
            PGPPublicKeyEncryptedData publicKeyEncryptedData = null;

            PGPSecretKeyRingCollection pgpSecretKeyRingCollection = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(new FileInputStream(privateKeyFile))
            , new JcaKeyFingerprintCalculator());

            Iterator<PGPEncryptedData> encryptedDataItr = encryptedDataList.getEncryptedDataObjects(); 
            while (pgpPrivateKey == null && encryptedDataItr.hasNext()) {
                publicKeyEncryptedData = (PGPPublicKeyEncryptedData) encryptedDataItr.next();
                pgpPrivateKey = findSecretKey(pgpSecretKeyRingCollection, publicKeyEncryptedData.getKeyID(), password);
            }
            
            if (null == publicKeyEncryptedData) {
                throw new PGPException("Could not generate PGPPublicKeyEncryptedData object");

            }
            if (pgpPrivateKey == null) {
                throw new PGPException("Could Not Extract private key");

            }
            decrypt(outputStreamDecryptedFile, pgpPrivateKey, publicKeyEncryptedData);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection pgpSecretKeyRingCollection ,long keyID, String passwCode) throws PGPException {
        PGPSecretKey pgpSecretKey = pgpSecretKeyRingCollection.getSecretKey(keyID);
        return pgpSecretKey == null ? null : pgpSecretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(passwCode.toCharArray()));
    }

    static boolean decrypt(OutputStream clearOut, PGPPrivateKey pgpPrivateKey, PGPPublicKeyEncryptedData publicKeyEncryptedData) throws IOException, PGPException {
        PublicKeyDataDecryptorFactory decryptorFactory = new JcePublicKeyDataDecryptorFactoryBuilder()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(pgpPrivateKey);
        InputStream decryptedCompressedIn = publicKeyEncryptedData.getDataStream(decryptorFactory);

        JcaPGPObjectFactory decCompObjFac = new JcaPGPObjectFactory(decryptedCompressedIn);
        PGPCompressedData pgpCompressedData = (PGPCompressedData) decCompObjFac.nextObject();

        InputStream compressedDataStream = new BufferedInputStream(pgpCompressedData.getDataStream());
        JcaPGPObjectFactory pgpCompObjFac = new JcaPGPObjectFactory(compressedDataStream);

        Object message = pgpCompObjFac.nextObject();

        if (message instanceof PGPLiteralData) {
            PGPLiteralData pgpLiteralData = (PGPLiteralData) message;
            InputStream decDataStream = pgpLiteralData.getInputStream();
            IOUtils.copy(decDataStream, clearOut);
            clearOut.close();
            return true;
        } else if (message instanceof PGPOnePassSignatureList) {
            throw new PGPException("Encrypted message contains a signed message not literal data");
        } else {
            throw new PGPException("Message is not a simple encrypted file - Type Unknown");
        }
        // Performing Integrity check
        /*if (publicKeyEncryptedData.isIntegrityProtected()) {
            if (!publicKeyEncryptedData.verify()) {
                throw new PGPException("Message failed integrity check");
            }
        }*/
    }
}
