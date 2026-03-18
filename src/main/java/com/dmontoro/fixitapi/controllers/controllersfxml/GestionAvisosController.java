package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.repositories.AvisoRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class GestionAvisosController implements Initializable {

    // --- MENÚ LATERAL (PERFIL) ---
    @FXML private Label lblAvatar;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;

    // --- TARJETAS SUPERIORES (KPIs) ---
    @FXML private Label lblTotalCard;
    @FXML private Label lblPendientesCard;
    @FXML private Label lblProgresoCard;
    @FXML private Label lblCompletadosCard;

    // --- FILTROS ---
    @FXML private TextField txtBuscar;

    // --- TABLA Y COLUMNAS ---
    @FXML private TableView<Aviso> tablaAvisos;
    @FXML private TableColumn<Aviso, Long> colId;
    @FXML private TableColumn<Aviso, String> colCliente;
    @FXML private TableColumn<Aviso, String> colTecnico;
    @FXML private TableColumn<Aviso, String> colCategoria;
    @FXML private TableColumn<Aviso, String> colDescripcion;
    @FXML private TableColumn<Aviso, String> colEstado;
    @FXML private TableColumn<Aviso, String> colPrioridad;
    @FXML private TableColumn<Aviso, String> colFecha;
    @FXML private TableColumn<Aviso, String> colAcciones;

    @Autowired
    private AvisoRepository avisoRepository;

    @Autowired
    private ConfigurableApplicationContext springContext;

    // Variables para guardar quién ha iniciado sesión y pasarlo entre pantallas
    private String nombreActual = "Administrador";
    private String rolActual = "Jefe de Equipo";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnasTabla();
        cargarDatosTablaYKpis();
    }

    private void configurarColumnasTabla() {
        // Mapeo básico de columnas a los atributos del modelo Aviso
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Mapeo de objetos anidados (Cliente, Técnico, Categoría)
        colCliente.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCliente() != null) {
                return new SimpleStringProperty(cellData.getValue().getCliente().getNombre());
            }
            return new SimpleStringProperty("Sin Cliente");
        });

        colTecnico.setCellValueFactory(cellData -> {
            if (cellData.getValue().getTecnico() != null) {
                return new SimpleStringProperty(cellData.getValue().getTecnico().getNombre());
            }
            return new SimpleStringProperty("Sin Asignar");
        });

        colCategoria.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCategoria() != null) {
                return new SimpleStringProperty(cellData.getValue().getCategoria().getNombre());
            }
            return new SimpleStringProperty("General");
        });

        // Formateo de la fecha
        colFecha.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaCreacion() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new SimpleStringProperty(cellData.getValue().getFechaCreacion().format(formatter));
            }
            return new SimpleStringProperty("-");
        });

        // Valores por defecto para diseño
        colPrioridad.setCellValueFactory(cellData -> new SimpleStringProperty("MEDIA"));
        colAcciones.setCellValueFactory(cellData -> new SimpleStringProperty("⋮"));
    }

    private void cargarDatosTablaYKpis() {
        List<Aviso> avisos = avisoRepository.findAll();
        ObservableList<Aviso> listaAvisos = FXCollections.observableArrayList(avisos);
        tablaAvisos.setItems(listaAvisos);

        // Contadores para las tarjetas
        long pendientes = 0;
        long enProgreso = 0;
        long completados = 0;

        for (Aviso a : avisos) {
            if ("PENDIENTE".equalsIgnoreCase(a.getEstado())) pendientes++;
            else if ("EN PROGRESO".equalsIgnoreCase(a.getEstado())) enProgreso++;
            else if ("COMPLETADO".equalsIgnoreCase(a.getEstado())) completados++;
        }

        lblTotalCard.setText(String.valueOf(avisos.size()));
        lblPendientesCard.setText(String.valueOf(pendientes));
        lblProgresoCard.setText(String.valueOf(enProgreso));
        lblCompletadosCard.setText(String.valueOf(completados));
    }

    // --- MÉTODOS DE SESIÓN Y NAVEGACIÓN ---

    public void setDatosUsuario(String nombreReal, String rolReal) {
        this.nombreActual = nombreReal;
        this.rolActual = rolReal;

        String nombreFormateado = nombreReal.substring(0, 1).toUpperCase() + nombreReal.substring(1);
        lblNombreUsuario.setText(nombreFormateado);
        lblRolUsuario.setText(rolReal);

        String iniciales = "";
        String[] partes = nombreFormateado.trim().split("[ ._]");
        if (partes.length >= 2) {
            iniciales = partes[0].substring(0, 1) + partes[1].substring(0, 1);
        } else if (nombreFormateado.length() >= 2) {
            iniciales = nombreFormateado.substring(0, 2);
        } else {
            iniciales = nombreFormateado;
        }
        lblAvatar.setText(iniciales.toUpperCase());
    }

    @FXML
    public void irADashboard(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Dashboard.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // Devolvemos los datos del usuario al Dashboard para que no se pierdan
            DashboardController dashboardController = loader.getController();
            dashboardController.setDatosUsuario(nombreActual, rolActual);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root); // Cambiamos solo el contenido, sin cerrar la ventana

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
            stage.hide();
            stage.setMaximized(false);
            stage.setResizable(false);
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("FixIt - Acceso Administrativo");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}