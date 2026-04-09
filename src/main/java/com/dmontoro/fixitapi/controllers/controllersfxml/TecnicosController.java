package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Tecnico;
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

    // --- PERFIL Y MENÚ LATERAL ---
    @FXML private Label lblAvatar;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;

    // --- TARJETAS KPI SUPERIORES ---
    @FXML private Label lblTotalCard;
    @FXML private Label lblActivosCard;
    @FXML private Label lblTrabajosCard;
    @FXML private Label lblCalificacionCard;

    // --- BUSCADOR Y CONTENEDOR DE TARJETAS ---
    @FXML private TextField txtBuscar;
    @FXML private FlowPane flowPaneTecnicos;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Autowired
    private ConfigurableApplicationContext springContext;

    // Variables internas para mantener la sesión
    private String nombreActual = "Administrador";
    private String rolActual = "Jefe de Equipo";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarDatos();

        // Buscador en tiempo real
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, old, newValue) -> {
                cargarDatos();
            });
        }
    }

    private void cargarDatos() {
        flowPaneTecnicos.getChildren().clear();

        List<Tecnico> tecnicos = tecnicoRepository.findAll();
        String filtro = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase() : "";

        int totalActivos = 0;
        double sumaCalificaciones = 0;
        int totalTecnicosReales = 0;
        int totalTrabajosGlobales = 0;

        for (Tecnico t : tecnicos) {
            // No mostramos a los Administradores (como el Jefe) en la lista de técnicos
            if ("ADMIN".equalsIgnoreCase(t.getRol())) continue;

            totalTecnicosReales++;
            if (t.getActivo() != null && t.getActivo()) totalActivos++;
            sumaCalificaciones += t.getCalificacion() != null ? t.getCalificacion() : 0.0;

            // Contamos los trabajos reales si la lista de avisos no es nula
            int trabajosTecnico = (t.getAvisos() != null) ? t.getAvisos().size() : 0;
            totalTrabajosGlobales += trabajosTecnico;

            // Filtro del buscador
            boolean coincideNombre = t.getNombre() != null && t.getNombre().toLowerCase().contains(filtro);
            boolean coincideEspecialidad = t.getEspecialidad() != null && t.getEspecialidad().toLowerCase().contains(filtro);

            if (!coincideNombre && !coincideEspecialidad) {
                continue;
            }

            // Crear y añadir la tarjeta a la pantalla
            VBox card = crearTarjetaTecnico(t, trabajosTecnico);
            flowPaneTecnicos.getChildren().add(card);
        }

        // Actualizar las tarjetas de arriba
        lblTotalCard.setText(String.valueOf(totalTecnicosReales));
        lblActivosCard.setText(String.valueOf(totalActivos));
        lblTrabajosCard.setText(String.valueOf(totalTrabajosGlobales));

        double media = totalTecnicosReales > 0 ? (sumaCalificaciones / totalTecnicosReales) : 0;
        lblCalificacionCard.setText(String.format("%.1f", media));
    }
    @FXML
    public void abrirModalCrearTecnico() {
        // De momento solo ponemos un mensaje en la consola para que no explote.
        // En el siguiente paso meteremos aquí el código para abrir la ventana modal real.
        System.out.println("¡Botón de crear técnico pulsado!");
    }

    // ==========================================
    // CREADOR DE TARJETAS VISUALES
    // ==========================================
    private VBox crearTarjetaTecnico(Tecnico t, int trabajos) {
        VBox card = new VBox(15);
        card.getStyleClass().add("tecnico-card");
        card.setPrefWidth(350);

        // 1. CABECERA (Avatar + Nombre + Estado)
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(extraerIniciales(t.getNombre()));
        avatar.getStyleClass().addAll("avatar-large", "bg-purple");

        VBox nombreBox = new VBox(5);
        Label nombre = new Label(t.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0F172A;");

        Label estado = new Label(t.getActivo() != null && t.getActivo() ? "ACTIVO" : "INACTIVO");
        estado.getStyleClass().add(t.getActivo() != null && t.getActivo() ? "pill-activo" : "pill-inactivo");

        nombreBox.getChildren().addAll(nombre, estado);
        header.getChildren().addAll(avatar, nombreBox);

        // 2. CONTACTO (Email y Teléfono)
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

        // 4. ESTADÍSTICAS
        HBox statsBox = new HBox();

        VBox trabajosBox = new VBox(2); HBox.setHgrow(trabajosBox, Priority.ALWAYS);
        Label trTitulo = new Label("Trabajos"); trTitulo.getStyleClass().add("text-sm-gray");
        Label trNum = new Label(String.valueOf(trabajos)); trNum.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        trabajosBox.getChildren().addAll(trTitulo, trNum);

        VBox caliBox = new VBox(2);
        Label caliTitulo = new Label("Calificación"); caliTitulo.getStyleClass().add("text-sm-gray");
        Label caliNum = new Label((t.getCalificacion() != null ? t.getCalificacion() : "0.0") + " ★");
        caliNum.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #EAB308;");
        caliBox.getChildren().addAll(caliTitulo, caliNum);

        statsBox.getChildren().addAll(trabajosBox, caliBox);

        // 5. BOTONES ACCIÓN
        HBox accionesBox = new HBox(15);

        Button btnEdit = new Button(" Editar");
        SVGPath editIcon = new SVGPath(); editIcon.setContent("M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"); editIcon.getStyleClass().add("icon-lucide");
        btnEdit.setGraphic(editIcon);
        btnEdit.getStyleClass().add("btn-outline-edit");
        btnEdit.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnEdit, Priority.ALWAYS);

        Button btnDelete = new Button(" Eliminar");
        SVGPath deleteIcon = new SVGPath(); deleteIcon.setContent("M3 6h18 M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6 M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2 M10 11v6 M14 11v6"); deleteIcon.getStyleClass().add("icon-lucide");
        btnDelete.setGraphic(deleteIcon);
        btnDelete.getStyleClass().add("btn-outline-delete");
        btnDelete.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnDelete, Priority.ALWAYS);

        accionesBox.getChildren().addAll(btnEdit, btnDelete);

        card.getChildren().addAll(header, contacto, especialidadesBox, statsBox, accionesBox);
        return card;
    }

    private String extraerIniciales(String nombre) {
        if (nombre == null || nombre.isEmpty()) return "??";
        String[] partes = nombre.trim().split(" ");
        if (partes.length >= 2) return (partes[0].substring(0,1) + partes[1].substring(0,1)).toUpperCase();
        if (nombre.length() >= 2) return nombre.substring(0, 2).toUpperCase();
        return nombre.toUpperCase();
    }

    // ==========================================
    // SISTEMA DE NAVEGACIÓN Y SESIÓN
    // ==========================================
    public void setDatosUsuario(String nombreReal, String rolReal) {
        this.nombreActual = nombreReal;
        this.rolActual = rolReal;

        if (lblNombreUsuario != null) lblNombreUsuario.setText(nombreReal.substring(0, 1).toUpperCase() + nombreReal.substring(1));
        if (lblRolUsuario != null) lblRolUsuario.setText(rolReal);
        if (lblAvatar != null) lblAvatar.setText(extraerIniciales(nombreReal));
    }

    @FXML
    public void irADashboard(MouseEvent event) {
        navegarAPantalla(event, "/FXML/Dashboard.fxml");
    }

    @FXML
    public void irAGestionAvisos(MouseEvent event) {
        navegarAPantalla(event, "/FXML/GestionAvisos.fxml");
    }

    @FXML
    public void irAInventario(MouseEvent event) {
        navegarAPantalla(event, "/FXML/Inventario.fxml");
    }

    private void navegarAPantalla(MouseEvent event, String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // Detectamos qué controlador se ha cargado para pasarle los datos
            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setDatosUsuario(nombreActual, rolActual);
            } else if (controller instanceof GestionAvisosController) {
                ((GestionAvisosController) controller).setDatosUsuario(nombreActual, rolActual);
            } else if (controller instanceof InventarioController) {
                ((InventarioController) controller).setDatosUsuario(nombreActual, rolActual);
            }

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