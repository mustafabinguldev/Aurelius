package tech.bingulhan.webserver.app.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import tech.bingulhan.webserver.app.AureliusApplication;

import javax.swing.JOptionPane;

public class ApplicationUI extends Application {
    private Label pageCountLabel;
    private Label containerCountLabel;
    private Label imageCountLabel;
    private Label serverPortLabel;

    @Override
    public void start(Stage stage) throws Exception {
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("Options");
        MenuItem exitItem = new MenuItem("Stop Server");
        exitItem.setOnAction(e -> AureliusApplication.getInstance().stop());

        MenuItem reloadItem = new MenuItem("Reload Project");
        reloadItem.setOnAction(e -> {
            AureliusApplication.getInstance().getData().loadData();
            updateLabels();
            JOptionPane.showMessageDialog(null, "The server has been updated.", "Updated", JOptionPane.INFORMATION_MESSAGE);
        });

        menuFile.getItems().addAll(reloadItem, exitItem);
        menuBar.getMenus().add(menuFile);

        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setStyle("-fx-background-color: #EAEAEA; -fx-padding: 20; -fx-border-color: #BBB; -fx-border-width: 2px;");

        pageCountLabel = new Label();
        containerCountLabel = new Label();
        imageCountLabel = new Label();
        serverPortLabel = new Label();

        Font infoFont = new Font("Arial", 16);
        pageCountLabel.setFont(infoFont);
        containerCountLabel.setFont(infoFont);
        imageCountLabel.setFont(infoFont);
        serverPortLabel.setFont(infoFont);

        updateLabels();

        infoBox.getChildren().addAll(pageCountLabel, containerCountLabel, imageCountLabel, serverPortLabel);

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(infoBox);

        Scene scene = new Scene(root, 500, 300);
        stage.setOnCloseRequest(event -> AureliusApplication.getInstance().stop());
        stage.setTitle("Aurelius 0.4.5 - Server Status");
        stage.setScene(scene);
        stage.show();
    }

    private void updateLabels() {
        pageCountLabel.setText("📄 Loaded pages: " + AureliusApplication.getInstance().getData().getPages().keySet().size());
        containerCountLabel.setText("📦 Container: " + AureliusApplication.getInstance().getData().getContainerStructures().size());
        imageCountLabel.setText("🖼️ Media files: " + AureliusApplication.getInstance().getData().getMediaStructures().size());
        serverPortLabel.setText("🌐 Port: " + AureliusApplication.getInstance().getPort());
    }

    public void load(String[] args) {
        System.out.println("UI loading..");
        launch(args);
    }
}
