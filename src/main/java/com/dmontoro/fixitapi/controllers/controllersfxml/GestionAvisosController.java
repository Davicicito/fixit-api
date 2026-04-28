package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.repositories.AvisoRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import org.springframework.context.annotation.Scope;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

@Controller
@Scope("prototype")
public class GestionAvisosController implements Initializable {

    @FXML private Label lblAvatar;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;

    @FXML private Label lblTotalCard;
    @FXML private Label lblPendientesCard;
    @FXML private Label lblProgresoCard;
    @FXML private Label lblCompletadosCard;

    // --- ELEMENTOS DE FILTRADO ---
    @FXML private TextField txtBuscar;
    @FXML private Button btnFiltroTodos;
    @FXML private Button btnFiltroPendiente;
    @FXML private Button btnFiltroProgreso;
    @FXML private Button btnFiltroCompletado;

    // --- VARIABLES DE DATOS Y FILTROS ---
    private ObservableList<Aviso> masterData = FXCollections.observableArrayList();
    private FilteredList<Aviso> filteredData;
    private String estadoFiltroActual = "TODOS";

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

    private String nombreActual = "Administrador";
    private String rolActual = "Jefe de Equipo";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnasTabla();
        cargarDatosTablaYKpis();

        // Escuchar lo que el usuario escribe en el buscador en tiempo real
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
                aplicarFiltros();
            });
        }
    }

    // =======================================================
    // LÓGICA DE BOTONES DE FILTRO
    // =======================================================
    @FXML
    public void filtrarPorTodos() { cambiarEstadoFiltro("TODOS", btnFiltroTodos); }

    @FXML
    public void filtrarPorPendiente() { cambiarEstadoFiltro("PENDIENTE", btnFiltroPendiente); }

    @FXML
    public void filtrarPorProgreso() { cambiarEstadoFiltro("EN PROGRESO", btnFiltroProgreso); }

    @FXML
    public void filtrarPorCompletado() { cambiarEstadoFiltro("COMPLETADO", btnFiltroCompletado); }

    private void cambiarEstadoFiltro(String nuevoEstado, Button botonPulsado) {
        this.estadoFiltroActual = nuevoEstado;

        // Quitar la clase activa a todos
        btnFiltroTodos.getStyleClass().removeAll("filter-btn-active", "filter-btn");
        btnFiltroPendiente.getStyleClass().removeAll("filter-btn-active", "filter-btn");
        btnFiltroProgreso.getStyleClass().removeAll("filter-btn-active", "filter-btn");
        btnFiltroCompletado.getStyleClass().removeAll("filter-btn-active", "filter-btn");

        btnFiltroTodos.getStyleClass().add("filter-btn");
        btnFiltroPendiente.getStyleClass().add("filter-btn");
        btnFiltroProgreso.getStyleClass().add("filter-btn");
        btnFiltroCompletado.getStyleClass().add("filter-btn");

        // Poner la clase activa solo al pulsado
        botonPulsado.getStyleClass().remove("filter-btn");
        botonPulsado.getStyleClass().add("filter-btn-active");

        aplicarFiltros();
    }

    private void aplicarFiltros() {
        if (filteredData == null) return;

        filteredData.setPredicate(aviso -> {
            // Filtro 1: ¿Coincide con el botón de estado pulsado?
            boolean coincideEstado = true;
            if (!estadoFiltroActual.equals("TODOS")) {
                coincideEstado = aviso.getEstado() != null && aviso.getEstado().equalsIgnoreCase(estadoFiltroActual);
            }

            // Filtro 2: BÚSQUEDA GLOBAL (Omni-search)
            boolean coincideTexto = true;
            String textoBusqueda = txtBuscar.getText();

            if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
                String filtroLower = textoBusqueda.toLowerCase();

                // Recolectamos absolutamente todos los datos de la fila y los pasamos a minúsculas
                String id = String.valueOf(aviso.getId());
                String cliente = aviso.getCliente() != null ? aviso.getCliente().getNombre().toLowerCase() : "";
                String tecnico = aviso.getTecnico() != null ? aviso.getTecnico().getNombre().toLowerCase() : "";
                String categoria = aviso.getCategoria() != null ? aviso.getCategoria().getNombre().toLowerCase() : "";
                String descripcion = aviso.getDescripcion() != null ? aviso.getDescripcion().toLowerCase() : "";
                String estado = aviso.getEstado() != null ? aviso.getEstado().toLowerCase() : "";
                String prioridad = aviso.getPrioridad() != null ? aviso.getPrioridad().toLowerCase() : "";

                // Formateamos la fecha a texto para poder buscar, por ejemplo: "04/03"
                String fecha = "";
                if (aviso.getFechaCreacion() != null) {
                    fecha = aviso.getFechaCreacion().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }

                // Si AL MENOS UNO de los campos contiene lo que hemos escrito, mostramos la fila
                coincideTexto = id.contains(filtroLower) ||
                        cliente.contains(filtroLower) ||
                        tecnico.contains(filtroLower) ||
                        categoria.contains(filtroLower) ||
                        descripcion.contains(filtroLower) ||
                        estado.contains(filtroLower) ||
                        prioridad.contains(filtroLower) ||
                        fecha.contains(filtroLower);
            }

            // Solo mostramos la fila si cumple con el botón pulsado Y con el texto del buscador
            return coincideEstado && coincideTexto;
        });
    }

    // =======================================================
    // CONFIGURACIÓN DE TABLA Y DATOS
    // =======================================================

    private void configurarColumnasTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrioridad.setCellValueFactory(new PropertyValueFactory<>("prioridad"));

        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(column -> new javafx.scene.control.TableCell<Aviso, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lblEstado = new Label(item.toUpperCase());
                    lblEstado.getStyleClass().add("pill-estado");

                    if (item.equalsIgnoreCase("PENDIENTE")) {
                        lblEstado.getStyleClass().add("pill-pendiente");
                    } else if (item.equalsIgnoreCase("EN PROGRESO")) {
                        lblEstado.getStyleClass().add("pill-progreso");
                    } else if (item.equalsIgnoreCase("COMPLETADO")) {
                        lblEstado.getStyleClass().add("pill-completado");
                    } else {
                        lblEstado.getStyleClass().add("pill-default");
                    }

                    setGraphic(lblEstado);
                    setText(null);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

        colAcciones.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        colAcciones.setCellFactory(column -> new javafx.scene.control.TableCell<Aviso, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.control.Button btnVer = new javafx.scene.control.Button("👁 Ver Detalles");
                    btnVer.getStyleClass().add("btn-accion-tabla");

                    btnVer.setOnAction(event -> {
                        Aviso avisoSeleccionado = getTableView().getItems().get(getIndex());
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AvisoDetalleModal.fxml"));
                            loader.setControllerFactory(springContext::getBean);
                            Parent root = loader.load();

                            AvisoDetalleModalController modalController = loader.getController();
                            modalController.cargarDatosAviso(avisoSeleccionado);

                            Stage modalStage = new Stage();
                            modalStage.setTitle("Detalles del Aviso");
                            modalStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                            modalStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
                            modalStage.setScene(new Scene(root));

                            modalStage.showAndWait();
                            cargarDatosTablaYKpis();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    setGraphic(btnVer);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });

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

        // --- COLUMNA CATEGORÍA (Con iconos SVG de colores) ---
        colCategoria.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCategoria() != null) {
                return new SimpleStringProperty(cellData.getValue().getCategoria().getNombre());
            }
            return new SimpleStringProperty("General");
        });

        colCategoria.setCellFactory(column -> new javafx.scene.control.TableCell<Aviso, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
                    icon.setStyle("-fx-fill: transparent; -fx-stroke-width: 2; -fx-stroke-line-cap: round;");

                    String catLower = item.toLowerCase();
                    if (catLower.contains("font")) {
                        icon.setContent("M12 22a7 7 0 0 0 7-7c0-2-1-3.9-3-5.5s-3.5-4-4-6.5c-.5 2.5-2 4.9-4 6.5C6 11.1 5 13 5 15a7 7 0 0 0 7 7z");
                        icon.setStyle(icon.getStyle() + "-fx-stroke: #3B82F6;"); // Azul
                    } else if (catLower.contains("elec")) {
                        icon.setContent("M13 2L3 14h9l-1 8 10-12h-9l1-8z");
                        icon.setStyle(icon.getStyle() + "-fx-stroke: #F59E0B;"); // Naranja
                    } else {
                        icon.setContent("M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z M12 15a3 3 0 1 1 0-6 3 3 0 0 1 0 6z");
                        icon.setStyle(icon.getStyle() + "-fx-stroke: #A855F7;"); // Morado
                    }

                    Label textLbl = new Label(item);
                    textLbl.setStyle("-fx-text-fill: #475569;");

                    box.getChildren().addAll(icon, textLbl);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        // --- COLUMNA PRIORIDAD (Con punto de color) ---
        colPrioridad.setCellFactory(column -> new javafx.scene.control.TableCell<Aviso, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    // Creamos el punto redondo
                    javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(4);
                    String prioLower = item.toLowerCase();

                    if (prioLower.contains("alta") || prioLower.contains("urgente")) {
                        dot.setFill(javafx.scene.paint.Color.web("#EF4444")); // Rojo
                    } else if (prioLower.contains("media")) {
                        dot.setFill(javafx.scene.paint.Color.web("#F59E0B")); // Naranja
                    } else {
                        dot.setFill(javafx.scene.paint.Color.web("#10B981")); // Verde
                    }

                    Label textLbl = new Label(item.toUpperCase());
                    textLbl.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 12px;");

                    box.getChildren().addAll(dot, textLbl);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        colFecha.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaCreacion() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new SimpleStringProperty(cellData.getValue().getFechaCreacion().format(formatter));
            }
            return new SimpleStringProperty("-");
        });
    }

    private void cargarDatosTablaYKpis() {
        List<Aviso> avisos = avisoRepository.findAll();

        // 1. Guardamos los datos en la lista maestra
        masterData.clear();
        masterData.addAll(avisos);

        // 2. Si es la primera vez, configuramos el colador
        if (filteredData == null) {
            filteredData = new FilteredList<>(masterData, p -> true);
            SortedList<Aviso> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(tablaAvisos.comparatorProperty());
            tablaAvisos.setItems(sortedData);
        }

        // 3. Reaplicamos los filtros por si se ha cerrado la ventana de edición
        aplicarFiltros();

        // 4. Actualizamos las tarjetas de arriba
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

    @FXML
    public void abrirModalCrearAviso() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AvisoCrearModal.fxml"));
            loader.setControllerFactory(springContext::getBean); // Clave para la base de datos
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            modalStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();

            // Refrescamos la tabla al cerrar
            cargarDatosTablaYKpis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

            DashboardController dashboardController = loader.getController();
            dashboardController.setDatosUsuario(nombreActual, rolActual);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void irAInventario(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Inventario.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // Le pasamos los datos del usuario al Inventario para que no se pierda el perfil
            InventarioController inventarioController = loader.getController();
            inventarioController.setDatosUsuario(nombreActual, rolActual);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void irATecnicos(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Tecnicos.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // Pasamos los datos del usuario
            TecnicosController tecnicosController = loader.getController();
            tecnicosController.setDatosUsuario(nombreActual, rolActual);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
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