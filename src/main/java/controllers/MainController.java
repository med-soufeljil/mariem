package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import utils.AuthContext;

public class MainController {
    @FXML private Button btnFormationFlow, btnRecruitmentFlow;
    @FXML private ToggleButton toggleDarkMode;
    @FXML private AnchorPane contentArea;
    @FXML private VBox welcomePane;
    @FXML private BorderPane rootPane;
    @FXML private Label lblRole;

    @FXML
    public void initialize() {
        rootPane.getStyleClass().add("light-mode");
        lblRole.setText("Role: " + (AuthContext.isAdmin() ? "ADMIN" : "USER"));

        toggleDarkMode.selectedProperty().addListener((obs, oldVal, isDarkMode) -> {
            rootPane.getStyleClass().removeAll("light-mode", "dark-mode");
            rootPane.getStyleClass().add(isDarkMode ? "dark-mode" : "light-mode");
        });

        btnRecruitmentFlow.setOnAction(e -> loadUI("RecrutementModule.fxml"));
        btnFormationFlow.setOnAction(e -> loadUI("mainformation.fxml"));
    }

    private void loadUI(String fxml) {
        try {
            Parent pane = FXMLLoader.load(getClass().getResource("/" + fxml));
            contentArea.getChildren().setAll(pane);
            AnchorPane.setTopAnchor(pane, 0.0);
            AnchorPane.setBottomAnchor(pane, 0.0);
            AnchorPane.setLeftAnchor(pane, 0.0);
            AnchorPane.setRightAnchor(pane, 0.0);
            welcomePane.setVisible(false);
            welcomePane.setManaged(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
