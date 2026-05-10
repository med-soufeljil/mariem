package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import models.DemandeConge;
import services.DemandeCongeService;
import utils.AuthContext;

import java.sql.SQLException;
import java.time.LocalDate;

public class CongesController {
    @FXML private TextField txtSearchConge;
    @FXML private TableView<DemandeConge> tableConge;
    @FXML private TableColumn<DemandeConge, Integer> colCongeId, colCongeEmployeId;
    @FXML private TableColumn<DemandeConge, String> colCongeEmploye, colCongeType, colCongeMotif, colCongeStatut;
    @FXML private TableColumn<DemandeConge, LocalDate> colCongeDebut, colCongeFin;
    @FXML private TableColumn<DemandeConge, Long> colCongeJours;
    @FXML private Button btnAddConge, btnEditConge, btnDeleteConge, btnApproveConge, btnRejectConge;

    private final DemandeCongeService congeService = new DemandeCongeService();
    private final ObservableList<DemandeConge> congeMaster = FXCollections.observableArrayList();
    private FilteredList<DemandeConge> congeFiltered;

    @FXML
    public void initialize() {
        configureTable();
        configureActions();
        applyPermissions();
        refreshTable();
    }

    private void configureTable() {
        colCongeId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCongeEmployeId.setCellValueFactory(new PropertyValueFactory<>("idEmploye"));
        colCongeEmploye.setCellValueFactory(new PropertyValueFactory<>("employeNom"));
        colCongeType.setCellValueFactory(new PropertyValueFactory<>("typeConge"));
        colCongeDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colCongeFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colCongeJours.setCellValueFactory(new PropertyValueFactory<>("nombreJours"));
        colCongeMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colCongeStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        congeFiltered = new FilteredList<>(congeMaster, d -> true);
        tableConge.setItems(congeFiltered);
        txtSearchConge.textProperty().addListener((obs, old, value) -> filterConges(value));
    }

    private void configureActions() {
        btnAddConge.setOnAction(e -> showCongeDialog(null));
        btnEditConge.setOnAction(e -> {
            DemandeConge selected = tableConge.getSelectionModel().getSelectedItem();
            if (selected != null) showCongeDialog(selected);
        });
        btnDeleteConge.setOnAction(e -> deleteConge());
        btnApproveConge.setOnAction(e -> decideConge("APPROUVE"));
        btnRejectConge.setOnAction(e -> decideConge("REFUSE"));
    }

    private void applyPermissions() {
        boolean admin = AuthContext.isAdmin();
        btnEditConge.setDisable(!admin);
        btnDeleteConge.setDisable(!admin);
        btnApproveConge.setDisable(!admin);
        btnRejectConge.setDisable(!admin);
    }

    private void refreshTable() {
        try {
            congeMaster.setAll(congeService.recuperer());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Chargement", e.getMessage());
        }
    }

    private void filterConges(String query) {
        String q = query == null ? "" : query.toLowerCase().trim();
        congeFiltered.setPredicate(d -> q.isEmpty()
                || String.valueOf(d.getId()).contains(q)
                || String.valueOf(d.getIdEmploye()).contains(q)
                || safe(d.getEmployeNom()).contains(q)
                || safe(d.getTypeConge()).contains(q)
                || safe(d.getMotif()).contains(q)
                || safe(d.getStatut()).contains(q));
    }

    private void showCongeDialog(DemandeConge editing) {
        Dialog<DemandeConge> dialog = new Dialog<>();
        dialog.setTitle(editing == null ? "Nouvelle demande congé" : "Modifier demande congé");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        TextField idEmploye = new TextField(editing == null ? "" : String.valueOf(editing.getIdEmploye()));
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("CONGES_PAYES", "CONGES_VIE_PROFESSIONNELLE_FAMILIALE", "CONGES_MALADIE", "CONGES_SANS_SOLDE", "AUTRE"));
        type.setValue(editing == null ? "CONGES_PAYES" : editing.getTypeConge());
        DatePicker debut = new DatePicker(editing == null ? LocalDate.now() : editing.getDateDebut());
        DatePicker fin = new DatePicker(editing == null ? LocalDate.now() : editing.getDateFin());
        TextField motif = new TextField(editing == null ? "" : editing.getMotif());
        ComboBox<String> statut = new ComboBox<>(FXCollections.observableArrayList("EN_ATTENTE", "APPROUVE", "REFUSE"));
        statut.setValue(editing == null ? "EN_ATTENTE" : editing.getStatut());
        TextField commentaire = new TextField(editing == null ? "" : editing.getCommentaireDecision());

        dialog.getDialogPane().setContent(formGrid(
                new Label("Employé ID"), idEmploye,
                new Label("Type"), type,
                new Label("Début"), debut,
                new Label("Fin"), fin,
                new Label("Motif"), motif,
                new Label("Statut"), statut,
                new Label("Commentaire décision"), commentaire));

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) return null;
            DemandeConge d = editing == null ? new DemandeConge() : editing;
            d.setIdEmploye(Integer.parseInt(idEmploye.getText().trim()));
            d.setTypeConge(type.getValue());
            d.setDateDebut(debut.getValue());
            d.setDateFin(fin.getValue());
            d.setMotif(motif.getText());
            d.setStatut(statut.getValue());
            d.setCommentaireDecision(commentaire.getText());
            return d;
        });

        dialog.showAndWait().ifPresent(d -> {
            try {
                validateDates(d.getDateDebut(), d.getDateFin());
                if (d.getId() == 0) congeService.ajouter(d); else congeService.modifier(d);
                refreshTable();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Congés", ex.getMessage());
            }
        });
    }

    private void deleteConge() {
        DemandeConge selected = tableConge.getSelectionModel().getSelectedItem();
        if (selected == null || !confirm("Supprimer cette demande congé ?")) return;
        try {
            congeService.supprimer(selected.getId());
            refreshTable();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Suppression", e.getMessage());
        }
    }

    private void decideConge(String statut) {
        DemandeConge selected = tableConge.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            congeService.changerStatut(selected.getId(), statut, 1, askCommentaire(statut));
            refreshTable();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Décision", e.getMessage());
        }
    }

    private GridPane formGrid(Object... nodes) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.getStyleClass().add("form-grid");
        for (int i = 0, row = 0; i < nodes.length; i += 2, row++) {
            grid.add((javafx.scene.Node) nodes[i], 0, row);
            grid.add((javafx.scene.Node) nodes[i + 1], 1, row);
        }
        return grid;
    }

    private String askCommentaire(String statut) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Décision " + statut);
        dialog.setHeaderText("Ajouter un commentaire de décision");
        dialog.setContentText("Commentaire:");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        return dialog.showAndWait().orElse("");
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.CANCEL, ButtonType.OK);
        alert.setHeaderText("Confirmation");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void validateDates(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null) throw new IllegalArgumentException("Les dates sont obligatoires.");
        if (fin.isBefore(debut)) throw new IllegalArgumentException("La date fin doit être après la date début.");
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
