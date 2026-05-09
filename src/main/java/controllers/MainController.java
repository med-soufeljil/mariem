package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import services.OfferResponseHttpServer;
import utils.ApiRuntime;
import utils.AuthContext;
import utils.SessionContext;

import java.util.Optional;

public class MainController {

    private static MainController instance;

    @FXML
    private Button btnDashboard, btnCandidat, btnOffre, btnReunion, btnRecrutement;
    @FXML
    private Button btnRecruitmentSpace, btnTrainingSpace, btnFormation, btnApprenant, btnFormationDashboard;
    @FXML
    private ToggleButton toggleDarkMode;
    @FXML
    private AnchorPane contentArea;
    @FXML
    private VBox welcomePane, recruitmentMenu, trainingMenu;
    @FXML
    private BorderPane rootPane;
    @FXML
    private Label lblRole, lblModuleSubtitle, lblTopbarTitle, lblTopbarSubtitle, lblSystemStatus;
    @FXML
    private Label lblWelcomeTitle, lblWelcomeSubtitle;

    private String activeSpace = "recruitment";

    @FXML
    public void initialize() {
        instance = this;
        rootPane.getStyleClass().add("light-mode");

        pickRoleIfNeeded();
        syncFormationRole();
        OfferResponseHttpServer.ensureStarted();
        ApiRuntime.ensureStarted();

        toggleDarkMode.selectedProperty().addListener((obs, oldVal, isDarkMode) -> {
            rootPane.getStyleClass().removeAll("light-mode", "dark-mode");
            rootPane.getStyleClass().add(isDarkMode ? "dark-mode" : "light-mode");
        });

        btnRecruitmentSpace.setOnAction(e -> showRecruitmentSpace());
        btnTrainingSpace.setOnAction(e -> showTrainingSpace());

        btnDashboard.setOnAction(e -> loadUI("Dashboard.fxml"));
        btnCandidat.setOnAction(e -> loadUI("Candidat.fxml"));
        btnOffre.setOnAction(e -> loadUI("Offre.fxml"));
        btnRecrutement.setOnAction(e -> loadUI("Recrutement.fxml"));
        btnReunion.setOnAction(e -> loadUI("Reunion.fxml"));

        btnFormation.setOnAction(e -> loadUI("FormationView.fxml"));
        btnApprenant.setOnAction(e -> loadUI("ApprenantView.fxml"));
        btnFormationDashboard.setOnAction(e -> loadUI("dashboardformation.fxml"));

        applyPermissions();
        showRecruitmentSpace();
    }

    public static boolean isActive() {
        return instance != null;
    }

    public static void navigate(String fxml) {
        if (instance != null) {
            instance.loadUI(fxml);
        }
    }

    public static void showHome() {
        if (instance != null) {
            instance.showWelcome();
        }
    }

    public static void showTrainingHome() {
        if (instance != null) {
            instance.showTrainingSpace();
        }
    }

    private void pickRoleIfNeeded() {
        if (AuthContext.getRole() != null) {
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>("ADMIN", FXCollections.observableArrayList("ADMIN", "CANDIDAT"));
        dialog.setTitle("Connexion rôle");
        dialog.setHeaderText("Sélectionnez votre rôle");
        dialog.setContentText("Rôle:");
        Optional<String> choice = dialog.showAndWait();
        AuthContext.setRole("CANDIDAT".equals(choice.orElse("ADMIN")) ? AuthContext.Role.CANDIDAT : AuthContext.Role.ADMIN);
    }

    private void syncFormationRole() {
        if (SessionContext.getCurrentRole() == null) {
            SessionContext.setCurrentRole(AuthContext.isAdmin() ? SessionContext.Role.ADMIN : SessionContext.Role.USER);
        }
    }

    private void applyPermissions() {
        boolean isAdmin = AuthContext.isAdmin();
        lblRole.setText("Role: " + (isAdmin ? "ADMIN RH" : "CANDIDAT / USER"));

        btnDashboard.setVisible(isAdmin);
        btnDashboard.setManaged(isAdmin);
        btnCandidat.setDisable(!isAdmin);
        btnRecrutement.setDisable(!isAdmin);
        btnReunion.setDisable(!isAdmin);

        btnApprenant.setVisible(isAdmin);
        btnApprenant.setManaged(isAdmin);
        btnFormationDashboard.setVisible(isAdmin);
        btnFormationDashboard.setManaged(isAdmin);
    }

    private void showRecruitmentSpace() {
        activeSpace = "recruitment";
        recruitmentMenu.setVisible(true);
        recruitmentMenu.setManaged(true);
        trainingMenu.setVisible(false);
        trainingMenu.setManaged(false);
        lblModuleSubtitle.setText("Recruitment Management Suite");
        lblTopbarTitle.setText("Recruitment Operations Dashboard");
        lblTopbarSubtitle.setText("Track candidates, offers, hires and interviews in one place");
        lblSystemStatus.setText("● Connected to recruitment database");
        showWelcome();
    }

    private void showTrainingSpace() {
        activeSpace = "training";
        recruitmentMenu.setVisible(false);
        recruitmentMenu.setManaged(false);
        trainingMenu.setVisible(true);
        trainingMenu.setManaged(true);
        lblModuleSubtitle.setText("Training Management Suite");
        lblTopbarTitle.setText("Training Operations Dashboard");
        lblTopbarSubtitle.setText("Track formations, learners, feedbacks and reports with the same UI");
        lblSystemStatus.setText("● Connected to training database");
        showWelcome();
    }

    private void showWelcome() {
        contentArea.getChildren().setAll(welcomePane);
        AnchorPane.setTopAnchor(welcomePane, 20.0);
        AnchorPane.setBottomAnchor(welcomePane, 20.0);
        AnchorPane.setLeftAnchor(welcomePane, 20.0);
        AnchorPane.setRightAnchor(welcomePane, 20.0);
        welcomePane.setVisible(true);
        welcomePane.setManaged(true);
        if ("training".equals(activeSpace)) {
            lblWelcomeTitle.setText("Bienvenue dans la gestion des formations 🎓");
            lblWelcomeSubtitle.setText("Utilisez le même sidebar pour gérer formations, apprenants et dashboard.");
        } else {
            lblWelcomeTitle.setText("Welcome back, HR Team 👋");
            lblWelcomeSubtitle.setText("Choose a module from the left to start managing the full recruitment lifecycle.");
        }
    }

    private void loadUI(String fxml) {
        try {
            Parent pane = FXMLLoader.load(getClass().getResource("/" + fxml));
            if (pane != null) {
                contentArea.getChildren().setAll(pane);
                AnchorPane.setTopAnchor(pane, 0.0);
                AnchorPane.setBottomAnchor(pane, 0.0);
                AnchorPane.setLeftAnchor(pane, 0.0);
                AnchorPane.setRightAnchor(pane, 0.0);
                welcomePane.setVisible(false);
                welcomePane.setManaged(false);
            }
        } catch (Exception ex) {
            System.err.println("Erreur lors du chargement du FXML: " + fxml);
            ex.printStackTrace();
        }
    }
}
