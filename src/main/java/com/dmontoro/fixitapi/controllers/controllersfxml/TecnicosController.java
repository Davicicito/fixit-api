package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.models.Tecnico;
import com.dmontoro.fixitapi.repositories.AvisoRepository;
import com.dmontoro.fixitapi.repositories.TecnicoRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Controller
@Scope("prototype")
public class TecnicosController implements Initializable {

    @FXML private Label lblAvatar;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;

    @FXML private Label lblTotalCard;
    @FXML private Label lblActivosCard;
    @FXML private Label lblTrabajosCard;
    @FXML private Label lblCalificacionCard;

    @FXML private TextField txtBuscar;
    @FXML private FlowPane flowPaneTecnicos;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Autowired
    private ConfigurableApplicationContext springContext;

    @Autowired
    private AvisoRepository avisoRepository;

    private String nombreActual = "Administrador";
    private String rolActual = "Jefe de Equipo";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarDatos();

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, old, newValue) -> {
                cargarDatos();
            });
        }
    }

    private void cargarDatos() {
        flowPaneTecnicos.getChildren().clear();

        List<Tecnico> tecnicos = tecnicoRepository.findAll();
        List<Aviso> todosLosAvisos = avisoRepository.findAll(); // <-- Traemos todos los avisos

        String filtro = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase() : "";

        int totalActivos = 0;
        double sumaCalificaciones = 0;
        int totalTecnicosReales = 0;
        int totalTrabajosGlobales = 0;

        for (Tecnico t : tecnicos) {
            if ("ADMIN".equalsIgnoreCase(t.getRol())) continue;

            totalTecnicosReales++;
            if (t.getActivo() != null && t.getActivo()) totalActivos++;
            sumaCalificaciones += t.getCalificacion() != null ? t.getCalificacion() : 0.0;

            // LA MAGIA INFALIBLE: Contamos cuántos avisos tienen a este técnico asignado
            int trabajosTecnico = (int) todosLosAvisos.stream()
                    .filter(a -> a.getTecnico() != null && a.getTecnico().getId().equals(t.getId()))
                    .count();

            totalTrabajosGlobales += trabajosTecnico;

            boolean coincideNombre = t.getNombre() != null && t.getNombre().toLowerCase().contains(filtro);
            boolean coincideEspecialidad = t.getEspecialidad() != null && t.getEspecialidad().toLowerCase().contains(filtro);

            if (!coincideNombre && !coincideEspecialidad) {
                continue;
            }

            VBox card = crearTarjetaTecnico(t, trabajosTecnico);
            flowPaneTecnicos.getChildren().add(card);
        }

        lblTotalCard.setText(String.valueOf(totalTecnicosReales));
        lblActivosCard.setText(String.valueOf(totalActivos));
        lblTrabajosCard.setText(String.valueOf(totalTrabajosGlobales));

        double media = totalTecnicosReales > 0 ? (sumaCalificaciones / totalTecnicosReales) : 0;
        lblCalificacionCard.setText(String.format("%.1f", media));
    }

    @FXML
    public void abrirModalCrearTecnico() {
        abrirModalTecnico(null); // Le pasamos null para decirle que es uno NUEVO
    }

    private VBox crearTarjetaTecnico(Tecnico t, int trabajos) {
        VBox card = new VBox(15);
        card.getStyleClass().add("tecnico-card");
        card.setPrefWidth(350);

        // 1. CABECERA
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(extraerIniciales(t.getNombre()));
        avatar.getStyleClass().add("avatar-squircle");

        VBox nombreBox = new VBox(5);
        Label nombre = new Label(t.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0F172A;");

        Label estado = new Label(t.getActivo() != null && t.getActivo() ? "ACTIVO" : "INACTIVO");
        estado.getStyleClass().add(t.getActivo() != null && t.getActivo() ? "pill-activo" : "pill-inactivo");

        nombreBox.getChildren().addAll(nombre, estado);
        header.getChildren().addAll(avatar, nombreBox);

        // 2. CONTACTO
        VBox contacto = new VBox(8);

        HBox emailBox = new HBox(8); emailBox.setAlignment(Pos.CENTER_LEFT);
        SVGPath emailIcon = new SVGPath(); emailIcon.setContent("M4 7l6.2 4.65a2 2 0 002.4 0L18.8 7 M4 7h16v10H4V7z"); emailIcon.setStyle("-fx-stroke: #64748B; -fx-fill: transparent; -fx-stroke-width: 2;");
        Label emailLbl = new Label(t.getEmail()); emailLbl.getStyleClass().add("text-sm-gray");
        emailBox.getChildren().addAll(emailIcon, emailLbl);

        HBox phoneBox = new HBox(8); phoneBox.setAlignment(Pos.CENTER_LEFT);
        SVGPath phoneIcon = new SVGPath(); phoneIcon.setContent("M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"); phoneIcon.setStyle("-fx-stroke: #64748B; -fx-fill: transparent; -fx-stroke-width: 2;");
        Label phoneLbl = new Label(t.getTelefono() != null ? t.getTelefono() : "Sin teléfono"); phoneLbl.getStyleClass().add("text-sm-gray");
        phoneBox.getChildren().addAll(phoneIcon, phoneLbl);

        contacto.getChildren().addAll(emailBox, phoneBox);

        // 3. ESPECIALIDADES
        VBox especialidadesBox = new VBox(5);
        Label espTitulo = new Label("Especialidades:"); espTitulo.getStyleClass().add("text-sm-gray");
        HBox pillsBox = new HBox(8);
        if (t.getEspecialidad() != null && !t.getEspecialidad().trim().isEmpty()) {
            String[] separadas = t.getEspecialidad().split(",");
            for (String esp : separadas) {
                Label pill = new Label(esp.trim().toUpperCase());
                pill.getStyleClass().add("pill-categoria");
                pillsBox.getChildren().add(pill);
            }
        } else {
            Label pill = new Label("GENERAL");
            pill.getStyleClass().add("pill-categoria");
            pillsBox.getChildren().add(pill);
        }
        especialidadesBox.getChildren().addAll(espTitulo, pillsBox);

        // --- ZONA INFERIOR CON SEPARADOR ---
        VBox separador = new VBox(15);
        separador.getStyleClass().add("card-divider");
        // 4. ESTADÍSTICAS
        HBox statsBox = new HBox();
        statsBox.setPadding(new javafx.geometry.Insets(0, 0, 15, 0));

        VBox trabajosBox = new VBox(2); HBox.setHgrow(trabajosBox, Priority.ALWAYS);
        Label trTitulo = new Label("Trabajos"); trTitulo.getStyleClass().add("text-sm-gray");

        Label trNum = new Label(String.valueOf(trabajos));
        trNum.setStyle("-fx-font-weight: bold; -fx-font-size: 22px; -fx-text-fill: #0F172A;");

        // Asegúrate de que trNum se añade correctamente a la caja
        trabajosBox.getChildren().addAll(trTitulo, trNum);

        VBox caliBox = new VBox(2);
        Label caliTitulo = new Label("Calificación"); caliTitulo.getStyleClass().add("text-sm-gray");
        Label caliNum = new Label((t.getCalificacion() != null ? t.getCalificacion() : "0.0") + " ★");
        caliNum.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #EAB308;");
        caliBox.getChildren().addAll(caliTitulo, caliNum);

        statsBox.getChildren().addAll(trabajosBox, caliBox);

        // 5. BOTONES ACCIÓN CON PÍLDORAS
        HBox accionesBox = new HBox(15);

        Button btnEdit = new Button(" Editar");
        SVGPath editIcon = new SVGPath();
        editIcon.setContent("M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7 M18.375 2.625a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4Z");
        editIcon.getStyleClass().add("icon-lucide");
        editIcon.setStyle("-fx-stroke: #0F172A;");
        btnEdit.setGraphic(editIcon);
        btnEdit.getStyleClass().add("btn-pill-edit");
        btnEdit.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnEdit, Priority.ALWAYS);

        // ACCIÓN DE EDITAR (Abre el modal con los datos del técnico)
        btnEdit.setOnAction(e -> abrirModalTecnico(t));

        Button btnDelete = new Button(" Eliminar");
        SVGPath deleteIcon = new SVGPath();
        deleteIcon.setContent("M3 6h18 M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6 M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2 M10 11v6 M14 11v6");
        deleteIcon.getStyleClass().add("icon-lucide");
        deleteIcon.setStyle("-fx-stroke: #EF4444;");
        btnDelete.setGraphic(deleteIcon);
        btnDelete.getStyleClass().add("btn-pill-delete");
        btnDelete.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnDelete, Priority.ALWAYS);

        // ACCIÓN DE ELIMINAR (Con confirmación)
        btnDelete.setOnAction(e -> {
            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Eliminar Técnico");
            confirm.setHeaderText("¿Estás seguro de eliminar a " + t.getNombre() + "?");
            confirm.setContentText("Si tiene trabajos asignados, puede que no te deje eliminarlo por seguridad.");

            confirm.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    try {
                        tecnicoRepository.delete(t);
                        cargarDatos();
                    } catch (Exception ex) {
                        javafx.scene.control.Alert error = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                        error.setTitle("No se puede eliminar");
                        error.setHeaderText("Técnico con historial");
                        error.setContentText("No puedes borrar a este técnico porque tiene avisos asignados. En su lugar, edítalo y ponlo como 'INACTIVO'.");
                        error.showAndWait();
                    }
                }
            });
        });

        accionesBox.getChildren().addAll(btnEdit, btnDelete);

        // 1. Metemos las estadísticas y los botones DENTRO de la zona del separador
        separador.getChildren().addAll(statsBox, accionesBox);

        // 2. Y a la tarjeta principal solo le pasamos sus 4 bloques principales
        card.getChildren().addAll(header, contacto, especialidadesBox, separador);

        return card;
    }


    private String extraerIniciales(String nombre) {
        if (nombre == null || nombre.isEmpty()) return "??";
        String[] partes = nombre.trim().split(" ");
        if (partes.length >= 2) return (partes[0].substring(0,1) + partes[1].substring(0,1)).toUpperCase();
        if (nombre.length() >= 2) return nombre.substring(0, 2).toUpperCase();
        return nombre.toUpperCase();
    }

    public void setDatosUsuario(String nombreReal, String rolReal) {
        this.nombreActual = nombreReal;
        this.rolActual = rolReal;

        if (lblNombreUsuario != null) lblNombreUsuario.setText(nombreReal.substring(0, 1).toUpperCase() + nombreReal.substring(1));
        if (lblRolUsuario != null) lblRolUsuario.setText(rolReal);
        if (lblAvatar != null) lblAvatar.setText(extraerIniciales(nombreReal));
    }

    @FXML public void irADashboard(MouseEvent event) { navegarAPantalla(event, "/FXML/Dashboard.fxml"); }
    @FXML public void irAGestionAvisos(MouseEvent event) { navegarAPantalla(event, "/FXML/GestionAvisos.fxml"); }
    @FXML public void irAInventario(MouseEvent event) { navegarAPantalla(event, "/FXML/Inventario.fxml"); }

    private void navegarAPantalla(MouseEvent event, String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardController) ((DashboardController) controller).setDatosUsuario(nombreActual, rolActual);
            else if (controller instanceof GestionAvisosController) ((GestionAvisosController) controller).setDatosUsuario(nombreActual, rolActual);
            else if (controller instanceof InventarioController) ((InventarioController) controller).setDatosUsuario(nombreActual, rolActual);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void abrirModalTecnico(Tecnico t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/TecnicoModal.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            TecnicoModalController controller = loader.getController();
            controller.cargarDatosTecnico(t);

            Stage modalStage = new Stage();
            modalStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            modalStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();

            // Refrescamos los datos al cerrar
            cargarDatos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void irAClientes(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Clientes.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // Pasamos los datos del usuario a la nueva pantalla
            ClientesController controller = loader.getController();
            controller.setDatosUsuario(nombreActual, rolActual);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void cerrarSesion(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}