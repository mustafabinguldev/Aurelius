package tech.bingulhan.webserver.app.ui;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.util.Duration;
import tech.bingulhan.webserver.app.AureliusApplication;

import javax.swing.JOptionPane;
import java.util.Arrays;
import java.util.List;

public class ApplicationUI extends Application {

    private List<Label> labels;

    @Override
    public void start(Stage stage) throws Exception {
        MenuBar menuBar = new MenuBar();
        Menu serverMenu = new Menu("Server");

        MenuItem stopServerItem = new MenuItem("Stop Server");
        stopServerItem.setOnAction(e -> AureliusApplication.getInstance().stop());

        MenuItem reloadServerItem = new MenuItem("Reload Server");
        reloadServerItem.setOnAction(e -> {
            AureliusApplication.getInstance().getData().loadData();
            updateLabels();
            JOptionPane.showMessageDialog(null, "The server has been updated.", "Updated", JOptionPane.INFORMATION_MESSAGE);
        });

        stopServerItem.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        reloadServerItem.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        serverMenu.getItems().addAll(stopServerItem, reloadServerItem);
        menuBar.getMenus().add(serverMenu);

        VBox infoBox = new VBox(20);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setStyle("-fx-background-color: #f8f8f8; -fx-padding: 30; -fx-border-radius: 15; -fx-background-radius: 15; -fx-border-color: #e0e0e0; -fx-border-width: 2px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 10, 0.5, 2, 2);");


        labels = Arrays.asList(new Label(), new Label(), new Label(), new Label());
        Font infoFont = new Font("Segoe UI", 18);
        labels.forEach(label -> {
            label.setFont(infoFont);
            label.setStyle("-fx-text-fill: #333; -fx-padding: 15px; -fx-background-color: #ffffff; -fx-border-radius: 8px; -fx-border-color: #ddd; -fx-border-width: 1px;");
            label.setMaxWidth(350);
            label.setAlignment(Pos.CENTER);
            label.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 8; -fx-border-color: #ddd; -fx-border-width: 1px;");

            applyFadeTransition(label);
        });

        updateLabels();
        infoBox.getChildren().addAll(labels);


        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(infoBox);
        root.setStyle("-fx-background-color: #ffffff; -fx-padding: 20px;");


        menuBar.setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #388E3C); -fx-text-fill: white;");
        serverMenu.setStyle("-fx-background-color: #2e7d32;");


        Scene scene = new Scene(root, 600, 400);

        stage.setOnCloseRequest(event -> AureliusApplication.getInstance().stop());
        stage.setTitle("Aurelius 0.5.1 - Server Status");
        stage.setScene(scene);
        stage.setResizable(false);

        stage.show();
    }


    private void applyHoverEffect(MenuItem menuItem, String hoverColor) {
        menuItem.setStyle("-fx-background-color: " + hoverColor + "; -fx-text-fill: white;");
    }

    private void applyDefaultEffect(MenuItem menuItem, String defaultColor) {
        menuItem.setStyle("-fx-background-color: " + defaultColor + "; -fx-text-fill: white;");
    }


    private void applyFadeTransition(Label label) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), label);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setCycleCount(1);
        fadeTransition.play();
    }

    private void updateLabels() {
        labels.get(0).setText("📄 Loaded pages: " + AureliusApplication.getInstance().getData().getPages().keySet().size());
        labels.get(1).setText("📦 Container: " + AureliusApplication.getInstance().getData().getContainerStructures().size());
        labels.get(2).setText("🖼️ Media files: " + AureliusApplication.getInstance().getData().getMediaStructures().size());
        labels.get(3).setText("🌐 Port: " + AureliusApplication.getInstance().getPort());
    }

    public void load(String[] args) {
        System.out.println("UI Loading...");
        launch(args);
    }
}
