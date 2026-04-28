package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.models.Cliente;
import com.dmontoro.fixitapi.repositories.AvisoRepository;
import com.dmontoro.fixitapi.repositories.ClienteRepository;
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
public class ClientesController implements Initializable {

    @FXML private Label lblAvatar, lblNombreUsuario, lblRolUsuario;
    @FXML private Label lblTotalCard, lblEmpresasCard, lblAvisosActivosCard, lblTotalAvisosCard;
    @FXML private TextField txtBuscar;
    @FXML private FlowPane flowPaneClientes;

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private AvisoRepository avisoRepository;
    @Autowired private ConfigurableApplicationContext springContext;

    private String nombreActual = "Administrador";
    private String rolActual = "Jefe de Equipo";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarDatos();
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, old, newValue) -> cargarDatos());
        }
    }

    private void cargarDatos() {
        flowPaneClientes.getChildren().clear();

        List<Cliente> clientes = clienteRepository.findAll();
        List<Aviso> todosAvisos = avisoRepository.findAll();
        String filtro = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase() : "";

        int totalClientes = 0;
        int totalEmpresas = 0;
        int clientesConAvisosActivos = 0;
        int totalAvisosGlobal = todosAvisos.size();

        for (Cliente c : clientes) {
            boolean coincideNombre = c.getNombre() != null && c.getNombre().toLowerCase().contains(filtro);
            boolean coincideDir = c.getDireccion() != null && c.getDireccion().toLowerCase().contains(filtro);

            if (!coincideNombre && !coincideDir) continue;

            totalClientes++;
            if ("EMPRESA".equalsIgnoreCase(c.getTipo())) totalEmpresas++;

            // Calcular los avisos del cliente
            int totalAvisosCliente = 0;
            int avisosActivosCliente = 0;

            for (Aviso a : todosAvisos) {
                if (a.getCliente() != null && a.getCliente().getId().equals(c.getId())) {
                    totalAvisosCliente++;
                    if (!"COMPLETADO".equalsIgnoreCase(a.getEstado())) {
                        avisosActivosCliente++;
                    }
                }
            }

            if (avisosActivosCliente > 0) clientesConAvisosActivos++;

            VBox card = crearTarjetaCliente(c, totalAvisosCliente, avisosActivosCliente);
            flowPaneClientes.getChildren().add(card);
        }

        lblTotalCard.setText(String.valueOf(totalClientes));
        lblEmpresasCard.setText(String.valueOf(totalEmpresas));
        lblAvisosActivosCard.setText(String.valueOf(clientesConAvisosActivos));
        lblTotalAvisosCard.setText(String.valueOf(totalAvisosGlobal));
    }

    private VBox crearTarjetaCliente(Cliente c, int totalAvisos, int avisosActivos) {
        VBox card = new VBox(15);
        card.getStyleClass().add("tecnico-card"); // Usamos el mismo borde/sombra de Técnicos
        card.setPrefWidth(420); // Un poco más ancha que los técnicos para que quepa la info

        // 1. CABECERA (Icono Edificio + Nombre + Píldora)
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox avatarBox = new VBox();
        avatarBox.getStyleClass().add("avatar-squircle-orange");
        SVGPath iconBuilding = new SVGPath();
        iconBuilding.setContent("M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18 M6 12H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2 M18 9h2a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-2 M10 6h4 M10 10h4 M10 14h4 M10 18h4");
        iconBuilding.setStyle("-fx-stroke: white; -fx-fill: transparent; -fx-stroke-width: 2;");
        avatarBox.getChildren().add(iconBuilding);

        VBox nombreBox = new VBox(5);
        Label nombre = new Label(c.getNombre() != null ? c.getNombre() : "Sin Nombre");
        nombre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0F172A;");

        Label tipo = new Label(c.getTipo() != null ? c.getTipo() : "PARTICULAR");
        tipo.getStyleClass().add("pill-empresa"); // Píldora moradita

        nombreBox.getChildren().addAll(nombre, tipo);
        header.getChildren().addAll(avatarBox, nombreBox);

        // 2. CONTACTO (Dirección, Email, Teléfono con iconos Lucide)
        VBox contacto = new VBox(8);

        HBox dirBox = new HBox(8); dirBox.setAlignment(Pos.CENTER_LEFT);
        SVGPath dirIcon = new SVGPath(); dirIcon.setContent("M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z M12 10a3 3 0 1 1 0-6 3 3 0 0 1 0 6Z"); dirIcon.setStyle("-fx-stroke: #64748B; -fx-fill: transparent; -fx-stroke-width: 2;");
        Label dirLbl = new Label(c.getDireccion() != null ? c.getDireccion() : "Sin dirección"); dirLbl.getStyleClass().add("text-sm-gray");
        dirBox.getChildren().addAll(dirIcon, dirLbl);

        HBox emailBox = new HBox(8); emailBox.setAlignment(Pos.CENTER_LEFT);
        SVGPath emailIcon = new SVGPath(); emailIcon.setContent("M4 7l6.2 4.65a2 2 0 002.4 0L18.8 7 M4 7h16v10H4V7z"); emailIcon.setStyle("-fx-stroke: #64748B; -fx-fill: transparent; -fx-stroke-width: 2;");
        Label emailLbl = new Label(c.getEmail() != null ? c.getEmail() : "Sin email"); emailLbl.getStyleClass().add("text-sm-gray");
        emailBox.getChildren().addAll(emailIcon, emailLbl);

        HBox phoneBox = new HBox(8); phoneBox.setAlignment(Pos.CENTER_LEFT);
        SVGPath phoneIcon = new SVGPath(); phoneIcon.setContent("M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"); phoneIcon.setStyle("-fx-stroke: #64748B; -fx-fill: transparent; -fx-stroke-width: 2;");
        Label phoneLbl = new Label(c.getTelefono() != null ? c.getTelefono() : "Sin teléfono"); phoneLbl.getStyleClass().add("text-sm-gray");
        phoneBox.getChildren().addAll(phoneIcon, phoneLbl);

        contacto.getChildren().addAll(dirBox, emailBox, phoneBox);

        // 3. NOTAS DEL CLIENTE
        VBox notasBox = new VBox(8);
        notasBox.getStyleClass().add("nota-divider");
        HBox notaContent = new HBox(8); notaContent.setAlignment(Pos.CENTER_LEFT);
        SVGPath notaIcon = new SVGPath(); notaIcon.setContent("M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z M14 2v6h6 M16 13H8 M16 17H8 M10 9H8"); notaIcon.setStyle("-fx-stroke: #94A3B8; -fx-fill: transparent; -fx-stroke-width: 2;");
        Label notaLbl = new Label(c.getNotas() != null && !c.getNotas().isEmpty() ? c.getNotas() : "Sin notas adicionales");
        notaLbl.getStyleClass().add("text-nota");
        notaContent.getChildren().addAll(notaIcon, notaLbl);
        notasBox.getChildren().add(notaContent);

        // --- ZONA INFERIOR ---
        VBox separadorAbajo = new VBox(15);
        separadorAbajo.getStyleClass().add("card-divider");

        // 4. ESTADÍSTICAS
        HBox statsBox = new HBox();
        statsBox.setPadding(new javafx.geometry.Insets(0, 0, 15, 0));

        VBox activosBox = new VBox(2); HBox.setHgrow(activosBox, Priority.ALWAYS);
        Label trTitulo = new Label("Avisos Activos"); trTitulo.getStyleClass().add("text-sm-gray");
        Label trNum = new Label(String.valueOf(avisosActivos));
        // Aumentado a 22px
        trNum.setStyle("-fx-font-weight: bold; -fx-font-size: 22px; -fx-text-fill: #EA580C;");
        activosBox.getChildren().addAll(trTitulo, trNum);

        VBox totalesBox = new VBox(2);
        Label toTitulo = new Label("Total Avisos"); toTitulo.getStyleClass().add("text-sm-gray");
        Label toNum = new Label(String.valueOf(totalAvisos));
        // Aumentado a 22px
        toNum.setStyle("-fx-font-weight: bold; -fx-font-size: 22px; -fx-text-fill: #0F172A;");
        totalesBox.getChildren().addAll(toTitulo, toNum);

        statsBox.getChildren().addAll(activosBox, totalesBox);

        // 5. BOTONES ACCIÓN
        HBox accionesBox = new HBox(15);

        // --- BOTÓN EDITAR ---
        Button btnEdit = new Button(" Editar");
        SVGPath editIcon = new SVGPath(); editIcon.setContent("M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7 M18.375 2.625a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4Z"); editIcon.getStyleClass().add("icon-lucide"); editIcon.setStyle("-fx-stroke: #0F172A;");
        btnEdit.setGraphic(editIcon); btnEdit.getStyleClass().add("btn-pill-edit"); btnEdit.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnEdit, Priority.ALWAYS);

        // 👇 AQUÍ PONEMOS LA ACCIÓN DEL BOTÓN EDITAR 👇
        btnEdit.setOnAction(e -> abrirModalCliente(c));

        // --- BOTÓN ELIMINAR ---
        Button btnDelete = new Button(" Eliminar");
        SVGPath deleteIcon = new SVGPath(); deleteIcon.setContent("M3 6h18 M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6 M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2 M10 11v6 M14 11v6"); deleteIcon.getStyleClass().add("icon-lucide"); deleteIcon.setStyle("-fx-stroke: #EF4444;");
        btnDelete.setGraphic(deleteIcon); btnDelete.getStyleClass().add("btn-pill-delete"); btnDelete.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnDelete, Priority.ALWAYS);

        // 👇 AQUÍ PONEMOS LA ACCIÓN DEL BOTÓN ELIMINAR (Con Confirmación) 👇
        btnDelete.setOnAction(e -> {
            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Eliminar Cliente");
            confirm.setHeaderText("¿Estás seguro de eliminar a " + c.getNombre() + "?");
            confirm.setContentText("Esta acción es definitiva. Si tiene avisos registrados, puede que el sistema no te deje borrarlo.");

            confirm.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    try {
                        clienteRepository.delete(c);
                        cargarDatos(); // Refrescamos la pantalla
                    } catch (Exception ex) {
                        javafx.scene.control.Alert error = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                        error.setTitle("No se puede eliminar");
                        error.setHeaderText("Cliente con historial");
                        error.setContentText("No puedes borrar a este cliente porque tiene trabajos asociados en el historial.");
                        error.showAndWait();
                    }
                }
            });
        });

        // Juntamos los botones y cerramos la tarjeta
        accionesBox.getChildren().addAll(btnEdit, btnDelete);

        separadorAbajo.getChildren().addAll(statsBox, accionesBox);
        card.getChildren().addAll(header, contacto, notasBox, separadorAbajo);

        return card;
    }

    @FXML
    public void abrirModalCrearCliente() {
        abrirModalCliente(null); // 'null' significa que vamos a crear uno nuevo
    }

    private void abrirModalCliente(Cliente c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ClienteModal.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            ClienteModalController controller = loader.getController();
            controller.cargarDatosCliente(c);

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

    // --- SESIÓN Y NAVEGACIÓN ---
    public void setDatosUsuario(String nombreReal, String rolReal) {
        this.nombreActual = nombreReal; this.rolActual = rolReal;
        if (lblNombreUsuario != null) lblNombreUsuario.setText(nombreReal.substring(0, 1).toUpperCase() + nombreReal.substring(1));
        if (lblRolUsuario != null) lblRolUsuario.setText(rolReal);
        if (lblAvatar != null) lblAvatar.setText(nombreReal.substring(0, 2).toUpperCase());
    }

    // =======================================================
    // NAVEGACIÓN UNIVERSAL (A PRUEBA DE BUGS DE SESIÓN)
    // =======================================================
    @FXML public void irADashboard(MouseEvent event) { navegarAPantalla(event, "/FXML/Dashboard.fxml"); }
    @FXML public void irAGestionAvisos(MouseEvent event) { navegarAPantalla(event, "/FXML/GestionAvisos.fxml"); }
    @FXML public void irAInventario(MouseEvent event) { navegarAPantalla(event, "/FXML/Inventario.fxml"); }
    @FXML public void irATecnicos(MouseEvent event) { navegarAPantalla(event, "/FXML/Tecnicos.fxml"); }

    private void navegarAPantalla(MouseEvent event, String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // EL ANTÍDOTO: Comprobamos a qué pantalla vamos y le enchufamos la mochila con tus datos
            Object controller = loader.getController();
            if (controller instanceof DashboardController) ((DashboardController) controller).setDatosUsuario(nombreActual, rolActual);
            else if (controller instanceof GestionAvisosController) ((GestionAvisosController) controller).setDatosUsuario(nombreActual, rolActual);
            else if (controller instanceof InventarioController) ((InventarioController) controller).setDatosUsuario(nombreActual, rolActual);
            else if (controller instanceof TecnicosController) ((TecnicosController) controller).setDatosUsuario(nombreActual, rolActual);
            else if (controller instanceof ClientesController) ((ClientesController) controller).setDatosUsuario(nombreActual, rolActual);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML public void cerrarSesion(MouseEvent event) { /* Igual que en las otras */ }
}