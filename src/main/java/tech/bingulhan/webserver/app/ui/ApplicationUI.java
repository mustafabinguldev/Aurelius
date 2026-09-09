package tech.bingulhan.webserver.app.ui;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tech.bingulhan.webserver.app.AureliusApplication;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class ApplicationUI extends Application {

    private static final String STYLESHEET = "/tech/bingulhan/webserver/app/ui/application-ui.css";

    private List<Label> metricValues;

    @Override
    public void start(Stage stage) {
        Label brandMark = new Label("A");
        brandMark.getStyleClass().add("brand-mark");
        Label eyebrow = new Label("AURELIUS / SERVER CONSOLE");
        eyebrow.getStyleClass().add("eyebrow");
        Label title = new Label("Server overview");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Runtime resources, presented with clarity.");
        subtitle.getStyleClass().add("page-subtitle");
        VBox brandCopy = new VBox(4, eyebrow, title, subtitle);

        Label status = new Label("●  SERVER ONLINE");
        status.getStyleClass().add("status-pill");
        Button reloadButton = new Button("Reload data");
        reloadButton.getStyleClass().add("primary-action");
        reloadButton.setOnAction(event -> reloadData(stage));
        Button stopButton = new Button("Stop server");
        stopButton.getStyleClass().add("danger-action");
        stopButton.setOnAction(event -> stopServer());

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox header = new HBox(16, brandMark, brandCopy, headerSpacer, status, reloadButton, stopButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header-panel");

        Label dashboardLabel = new Label("SYSTEM SNAPSHOT");
        dashboardLabel.getStyleClass().add("section-label");
        Label dashboardHint = new Label("The active server configuration at a glance");
        dashboardHint.getStyleClass().add("section-hint");
        HBox sectionHeading = new HBox(12, dashboardLabel, dashboardHint);
        sectionHeading.setAlignment(Pos.CENTER_LEFT);

        metricValues = Arrays.asList(new Label(), new Label(), new Label(), new Label());
        Label footer = new Label("Reload data when configuration files or served resources change.");
        footer.getStyleClass().add("footer-note");
        VBox dashboard = new VBox(18, sectionHeading, createMetricGrid(), footer);
        dashboard.getStyleClass().add("dashboard-content");

        BorderPane root = new BorderPane(dashboard);
        root.setTop(header);
        root.getStyleClass().add("app-shell");

        Scene scene = new Scene(root, 980, 680);
        scene.getStylesheets().add(stylesheet());
        updateMetricValues();
        applyEntranceTransition(dashboard);

        stage.setOnCloseRequest(event -> stopServer());
        stage.setTitle("Aurelius 1.1 - Server Status");
        stage.setMinWidth(860);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();
    }


    private GridPane createMetricGrid() {
        GridPane metricGrid = new GridPane();
        metricGrid.setHgap(18);
        metricGrid.setVgap(18);
        metricGrid.getStyleClass().add("metric-grid");

        ColumnConstraints firstColumn = new ColumnConstraints();
        firstColumn.setPercentWidth(50);
        firstColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints secondColumn = new ColumnConstraints();
        secondColumn.setPercentWidth(50);
        secondColumn.setHgrow(Priority.ALWAYS);
        metricGrid.getColumnConstraints().addAll(firstColumn, secondColumn);

        metricGrid.add(createMetricCard("01", "LOADED PAGES", "Published routes available to serve", metricValues.get(0)), 0, 0);
        metricGrid.add(createMetricCard("02", "CONTAINERS", "Structured content definitions", metricValues.get(1)), 1, 0);
        metricGrid.add(createMetricCard("03", "MEDIA FILES", "Assets ready for delivery", metricValues.get(2)), 0, 1);
        metricGrid.add(createMetricCard("04", "SERVER PORT", "Active network endpoint", metricValues.get(3)), 1, 1);
        return metricGrid;
    }

    private VBox createMetricCard(String number, String title, String description, Label value) {
        Label metricNumber = new Label(number);
        metricNumber.getStyleClass().add("metric-number");
        Label metricTitle = new Label(title);
        metricTitle.getStyleClass().add("metric-title");
        Label metricDescription = new Label(description);
        metricDescription.getStyleClass().add("metric-description");
        value.getStyleClass().add("metric-value");

        Region cardSpacer = new Region();
        VBox.setVgrow(cardSpacer, Priority.ALWAYS);
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox cardHeader = new HBox(metricTitle, headerSpacer, metricNumber);

        VBox card = new VBox(12, cardHeader, cardSpacer, value, metricDescription);
        card.setPadding(new Insets(24));
        card.setMinHeight(190);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("metric-card");
        return card;
    }


    private void reloadData(Stage stage) {
        AureliusApplication.getInstance().getData().loadData();
        updateMetricValues();
        showReloadedAlert(stage);
    }

    private void updateMetricValues() {
        metricValues.get(0).setText(String.valueOf(AureliusApplication.getInstance().getData().getPages().size()));
        metricValues.get(1).setText(String.valueOf(AureliusApplication.getInstance().getData().getContainerStructures().size()));
        metricValues.get(2).setText(String.valueOf(AureliusApplication.getInstance().getData().getMediaStructures().size()));
        metricValues.get(3).setText(String.valueOf(AureliusApplication.getInstance().getPort()));
    }

    private void showReloadedAlert(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle("Aurelius");
        alert.setHeaderText("Data reloaded");
        alert.setContentText("The dashboard now reflects the active server configuration.");
        alert.showAndWait();
    }

    private String stylesheet() {
        URL stylesheet = getClass().getResource(STYLESHEET);
        if (stylesheet == null) {
            throw new IllegalStateException("Missing dashboard stylesheet: " + STYLESHEET);
        }
        return stylesheet.toExternalForm();
    }

    private void applyEntranceTransition(Node node) {
        FadeTransition transition = new FadeTransition(Duration.millis(350), node);
        transition.setFromValue(0);
        transition.setToValue(1);
        transition.play();
    }

    public void load(String[] args) {
        System.out.println("UI Loading...");
        launch(args);
    }

    private void stopServer() {
        AureliusApplication.getInstance().stop();
        Platform.exit();
    }
}
