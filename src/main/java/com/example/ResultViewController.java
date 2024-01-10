package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class ResultViewController {
    @FXML
    private Label labelStatus;

    public void goMainView() {
        UtilsViews.setViewAnimating("View0");
    }

    public void updateStatusLabel(boolean wasSuccess) {
        if (wasSuccess) {
            System.out.println("Se ha podido encriptar");
            labelStatus.setText("Operation was a Success");
            labelStatus.setTextFill(Color.GREEN);
        } else {
            System.out.println("No se ha podido encriptar");
            labelStatus.setText("Operation was a Failure");
            labelStatus.setTextFill(Color.RED);
        }
        
    }
}
