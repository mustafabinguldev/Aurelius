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

public class ApplicationUI extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("Options");
        MenuItem exitItem = new MenuItem("Stop Server");

        exitItem.setOnAction(e -> AureliusApplication.getInstance().stop());
        menuFile.getItems().add(exitItem);
        menuBar.getMenus().add(menuFile);

        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setStyle("-fx-background-color: #EAEAEA; -fx-padding: 20; -fx-border-color: #BBB; -fx-border-width: 2px;");

        Label pageCountLabel = new Label("📄 Loaded pages: "+ AureliusApplication.getInstance().getData().getPages().keySet().size());
        Label containerCountLabel = new Label("📦 Container: "+ AureliusApplication.getInstance().getData().getContainerStructures().size());
        Label imageCountLabel = new Label("🖼️ Media files: "+ AureliusApplication.getInstance().getData().getMediaStructures().size());
        Label serverPortLabel = new Label("🌐 Port: "+ AureliusApplication.getInstance().getPort());

        Font infoFont = new Font("Arial", 16);
        pageCountLabel.setFont(infoFont);
        containerCountLabel.setFont(infoFont);
        imageCountLabel.setFont(infoFont);
        serverPortLabel.setFont(infoFont);

        infoBox.getChildren().addAll(pageCountLabel, containerCountLabel, imageCountLabel, serverPortLabel);

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(infoBox);


        Scene scene = new Scene(root, 500, 300);
        stage.setTitle("Aurelius 0.4.1 - Server Status");
        stage.setScene(scene);
        stage.show();
    }

    public void load(String[] args) {
        System.out.println("UI loading..");
        launch(args);
    }

}
