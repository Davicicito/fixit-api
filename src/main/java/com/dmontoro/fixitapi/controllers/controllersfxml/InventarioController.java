package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Material;
import com.dmontoro.fixitapi.repositories.MaterialRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import javafx.scene.shape.SVGPath;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Controller
@Scope("prototype")
public class InventarioController implements Initializable {

    // Menú Lateral
    @FXML private Label lblAvatar;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;

    // Tarjetas KPI
    @FXML private Label lblTotalCard;
    @FXML private Label lblStockBajoCard;
    @FXML private Label lblStockOkCard;
    @FXML private Label lblValorTotalCard;

    // Buscador
    @FXML private TextField txtBuscar;

    // Tabla
    @FXML private TableView<Material> tablaMateriales;
    @FXML private TableColumn<Material, String> colMaterial;
    @FXML private TableColumn<Material, String> colCategoria;
    @FXML private TableColumn<Material, Integer> colStock;
    @FXML private TableColumn<Material, Integer> colStockMin;
    @FXML private TableColumn<Material, String> colUnidad;
    @FXML private TableColumn<Material, String> colPrecio;
    @FXML private TableColumn<Material, String> colValorTotal;
    @FXML private TableColumn<Material, String> colEstado;
    @FXML private TableColumn<Material, String> colAcciones;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private String nombreActual = "Administrador";
    private String rolActual = "Jefe de Equipo";

    private ObservableList<Material> masterData = FXCollections.observableArrayList();
    private FilteredList<Material> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnasTabla();
        cargarDatosTablaYKpis();

        // Buscador Global en tiempo real
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
                if (filteredData != null) {
                    filteredData.setPredicate(material -> {
                        if (newValue == null || newValue.trim().isEmpty()) return true;

                        String filtro = newValue.toLowerCase();
                        String nombre = material.getNombre() != null ? material.getNombre().toLowerCase() : "";
                        String categoria = material.getCategoria() != null ? material.getCategoria().getNombre().toLowerCase() : "";

                        return nombre.contains(filtro) || categoria.contains(filtro);
                    });
                }
            });
        }
    }

    private void configurarColumnasTabla() {
        // 1. COLUMNA MATERIAL (Con Icono SVG Lucide "Package")
        colMaterial.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMaterial.setCellFactory(column -> new javafx.scene.control.TableCell<Material, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Creamos el SVG del paquete (Cubo)
                    SVGPath svgIcon = new SVGPath();
                    svgIcon.setContent("M16.5 9.4l-9-5.19M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16zM3.27 6.96L12 12.01l8.73-5.05M12 22.08V12");
                    svgIcon.getStyleClass().addAll("icon-lucide", "icon-lucide-blue");

                    HBox iconBox = new HBox(svgIcon);
                    iconBox.getStyleClass().add("icon-material-box");
                    iconBox.setAlignment(Pos.CENTER);

                    Label text = new Label(item);
                    text.setStyle("-fx-text-fill: #0F172A; -fx-font-weight: bold;");

                    HBox layout = new HBox(10, iconBox, text);
                    layout.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(layout);
                }
            }
        });

        // 2. COLUMNA CATEGORÍA (Píldora con borde)
        colCategoria.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCategoria() != null) {
                return new SimpleStringProperty(cellData.getValue().getCategoria().getNombre());
            }
            return new SimpleStringProperty("General");
        });
        colCategoria.setCellFactory(column -> new javafx.scene.control.TableCell<Material, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item.toUpperCase());
                    lbl.getStyleClass().add("pill-categoria");
                    setGraphic(lbl);
                }
            }
        });

        // 3. COLUMNA STOCK (Negro normal o Rojo si es <= Stock Mínimo)
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setCellFactory(column -> new javafx.scene.control.TableCell<Material, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Material mat = getTableRow().getItem();
                    Label lbl = new Label(String.valueOf(item));
                    lbl.setStyle("-fx-font-weight: bold;");

                    // Aquí aplicamos tu regla (pero dinámicamente usando la BD)
                    if (mat != null) {
                        int min = mat.getStockMinimo() != null ? mat.getStockMinimo() : 0;
                        if (item <= min) {
                            lbl.getStyleClass().add("text-rojo-stock");
                        }
                    }
                    setGraphic(lbl);
                }
            }
        });

        colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));

        // Precio formateado
        colPrecio.setCellValueFactory(cellData -> {
            Double precio = cellData.getValue().getPrecio();
            return new SimpleStringProperty(precio != null ? String.format("€%.2f", precio) : "€0.00");
        });

        // Valor Total
        colValorTotal.setCellValueFactory(cellData -> {
            Double precio = cellData.getValue().getPrecio();
            Integer stock = cellData.getValue().getStock();
            if (precio != null && stock != null) {
                return new SimpleStringProperty(String.format("€%.2f", precio * stock));
            }
            return new SimpleStringProperty("€0.00");
        });

        // 4. COLUMNA ESTADO (Píldora OK o Bajo Stock)
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        colEstado.setCellFactory(column -> new javafx.scene.control.TableCell<Material, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Material mat = getTableRow().getItem();
                    int stock = mat.getStock() != null ? mat.getStock() : 0;
                    int stockMin = mat.getStockMinimo() != null ? mat.getStockMinimo() : 0;

                    Label lblEstado = new Label();
                    lblEstado.getStyleClass().add("pill-estado");

                    if (stock <= stockMin) {
                        lblEstado.setText("⚠ Bajo Stock");
                        lblEstado.getStyleClass().add("pill-stock-bajo");
                    } else {
                        lblEstado.setText("✓ OK");
                        lblEstado.getStyleClass().add("pill-stock-ok");
                    }
                    setGraphic(lblEstado);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // BOTONES DE ACCIÓN (Editar y Borrar)
        colAcciones.setCellValueFactory(cellData -> new SimpleStringProperty(""));
        colAcciones.setCellFactory(column -> new javafx.scene.control.TableCell<Material, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Botón Editar (Lápiz)
                    Button btnEdit = new Button();
                    SVGPath svgEdit = new SVGPath();
                    svgEdit.setContent("M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z");
                    svgEdit.getStyleClass().add("icon-lucide");
                    btnEdit.setGraphic(svgEdit);
                    btnEdit.getStyleClass().add("btn-accion-edit");

                    // ACCIÓN DEL BOTÓN EDITAR
                    btnEdit.setOnAction(event -> {
                        Material material = getTableView().getItems().get(getIndex());
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MaterialEditarModal.fxml"));
                            loader.setControllerFactory(springContext::getBean);
                            Parent root = loader.load();

                            MaterialEditarModalController controller = loader.getController();
                            controller.cargarDatosMaterial(material);

                            Stage modalStage = new Stage();
                            modalStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                            modalStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
                            modalStage.setScene(new Scene(root));
                            modalStage.showAndWait();

                            // Refrescar al cerrar
                            cargarDatosTablaYKpis();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    // Botón Borrar (Papelera)
                    Button btnDelete = new Button();
                    SVGPath svgDelete = new SVGPath();
                    svgDelete.setContent("M3 6h18 M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6 M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2 M10 11v6 M14 11v6");
                    svgDelete.getStyleClass().add("icon-lucide");
                    btnDelete.setGraphic(svgDelete);
                    btnDelete.getStyleClass().add("btn-accion-delete");

                    // ACCIÓN DEL BOTÓN BORRAR
                    btnDelete.setOnAction(event -> {
                        Material material = getTableView().getItems().get(getIndex());

                        // 1. Preguntamos por seguridad
                        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Eliminar Material");
                        confirm.setHeaderText("¿Eliminar " + material.getNombre() + "?");
                        confirm.setContentText("Esta acción no se puede deshacer.");

                        confirm.showAndWait().ifPresent(response -> {
                            if (response == javafx.scene.control.ButtonType.OK) {
                                try {
                                    // 2. Intentamos borrarlo
                                    materialRepository.delete(material);
                                    cargarDatosTablaYKpis();
                                } catch (Exception e) {
                                    // 3. ¡Control de Errores Profesional! Si falla (ej. FK Constraint)
                                    javafx.scene.control.Alert error = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                                    error.setTitle("No se puede eliminar");
                                    error.setHeaderText("Material en uso");
                                    error.setContentText("No puedes eliminar este material porque ya ha sido utilizado o asignado a un Aviso de trabajo en el historial.");
                                    error.showAndWait();
                                }
                            }
                        });
                    });

                    HBox botones = new HBox(10, btnEdit, btnDelete);
                    botones.setAlignment(Pos.CENTER);
                    setGraphic(botones);
                }
            }
        });

        // 5. EL TOQUE MAESTRO: Colorear la fila entera si hay poco stock
        tablaMateriales.setRowFactory(tv -> new javafx.scene.control.TableRow<Material>() {
            @Override
            protected void updateItem(Material item, boolean empty) {
                super.updateItem(item, empty);
                // Limpiamos los estilos de la fila para que no se queden atascados
                getStyleClass().remove("row-bajo-stock");

                if (item != null && !empty) {
                    int stock = item.getStock() != null ? item.getStock() : 0;
                    int stockMin = item.getStockMinimo() != null ? item.getStockMinimo() : 0;
                    if (stock <= stockMin) {
                        getStyleClass().add("row-bajo-stock"); // Pone la fila rojiza
                    }
                }
            }
        });
    }
    private void cargarDatosTablaYKpis() {
        List<Material> materiales = materialRepository.findAll();

        masterData.clear();
        masterData.addAll(materiales);

        if (filteredData == null) {
            filteredData = new FilteredList<>(masterData, p -> true);
            SortedList<Material> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(tablaMateriales.comparatorProperty());
            tablaMateriales.setItems(sortedData);
        }

        // Calcular KPIs Superiores
        long totalMateriales = materiales.size();
        long stockBajo = 0;
        long stockOk = 0;
        double valorTotal = 0.0;

        for (Material m : materiales) {
            int stock = m.getStock() != null ? m.getStock() : 0;
            int stockMin = m.getStockMinimo() != null ? m.getStockMinimo() : 0;
            double precio = m.getPrecio() != null ? m.getPrecio() : 0.0;

            if (stock <= stockMin) stockBajo++;
            else stockOk++;

            valorTotal += (stock * precio);
        }

        lblTotalCard.setText(String.valueOf(totalMateriales));
        lblStockBajoCard.setText(String.valueOf(stockBajo));
        lblStockOkCard.setText(String.valueOf(stockOk));
        lblValorTotalCard.setText(String.format("€%.2f", Math.floor(valorTotal)));
    }

    // --- NAVEGACIÓN ---

    public void setDatosUsuario(String nombreReal, String rolReal) {
        this.nombreActual = nombreReal;
        this.rolActual = rolReal;
        lblNombreUsuario.setText(nombreReal.substring(0, 1).toUpperCase() + nombreReal.substring(1));
        lblRolUsuario.setText(rolReal);
        lblAvatar.setText(nombreReal.substring(0, 2).toUpperCase());
    }

    @FXML
    public void irAGestionAvisos(MouseEvent event) {
        navegarAPantalla(event, "/FXML/GestionAvisos.fxml");
    }

    @FXML
    public void irADashboard(MouseEvent event) {
        navegarAPantalla(event, "/FXML/Dashboard.fxml");
    }

    private void navegarAPantalla(MouseEvent event, String rutaFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFXML));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // Pasamos los datos del usuario para no perderlos
            Object controller = loader.getController();
            if (controller instanceof GestionAvisosController) {
                ((GestionAvisosController) controller).setDatosUsuario(nombreActual, rolActual);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void abrirModalCrearMaterial() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MaterialCrearModal.fxml"));
            loader.setControllerFactory(springContext::getBean); // Inyectamos Spring Boot
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            modalStage.initStyle(javafx.stage.StageStyle.UNDECORATED); // Sin bordes de Windows
            modalStage.setScene(new Scene(root));

            // Espera hasta que se cierre la ventana
            modalStage.showAndWait();

            // Refrescamos la tabla y las tarjetas KPI
            cargarDatosTablaYKpis();
        } catch (Exception e) {
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
            stage.setScene(new Scene(root, 1200, 800));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}