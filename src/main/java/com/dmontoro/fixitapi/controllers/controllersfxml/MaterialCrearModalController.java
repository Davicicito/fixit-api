package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Categoria;
import com.dmontoro.fixitapi.models.Material;
import com.dmontoro.fixitapi.repositories.CategoriaRepository;
import com.dmontoro.fixitapi.repositories.MaterialRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la ventana flotante para registrar nuevos materiales en el almacen.
 * Gestiona el formulario de creacion y verifica que los datos introducidos sean correctos antes de guardarlos.
 */
@Controller
public class MaterialCrearModalController implements Initializable {

    @FXML private TextField txtNombre;
    @FXML private TextField txtStock;
    @FXML private TextField txtStockMinimo;
    @FXML private ComboBox<String> comboUnidad;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<Categoria> comboCategoria;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    /**
     * Metodo que se ejecuta de forma automatica al abrir la ventana.
     * Llama a la funcion encargada de rellenar los menus desplegables con las opciones disponibles.
     *
     * @param location Ubicacion del archivo de la interfaz.
     * @param resources Recursos visuales de la ventana.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarDesplegables();
    }

    /**
     * Rellena las listas desplegables del formulario.
     * Añade las opciones fijas de medidas de cantidad y descarga las categorias desde la base de datos para que el usuario pueda seleccionarlas.
     */
    private void cargarDesplegables() {
        // 1. Cargar las unidades fijas
        comboUnidad.setItems(FXCollections.observableArrayList("UNIDAD", "METROS", "LITROS", "KILOS", "CAJAS"));
        comboUnidad.setPromptText("Selecciona unidad");

        // 2. Cargar Categorías desde MySQL
        comboCategoria.setItems(FXCollections.observableArrayList(categoriaRepository.findAll()));
        comboCategoria.setConverter(new StringConverter<Categoria>() {
            @Override public String toString(Categoria c) { return c != null ? c.getNombre() : ""; }
            @Override public Categoria fromString(String s) { return null; }
        });
    }

    /**
     * Recoge toda la informacion que el usuario ha escrito en el formulario.
     * Comprueba que no falten datos obligatorios y que los numeros escritos para el stock o el precio sean validos y positivos.
     * Si todo es correcto, guarda el nuevo articulo en la base de datos y cierra la pantalla.
     */
    @FXML
    public void crearMaterial() {
        String nombre = txtNombre.getText();
        String unidadSeleccionada = comboUnidad.getValue();
        Categoria categoriaSeleccionada = comboCategoria.getValue();

        if (nombre == null || nombre.trim().isEmpty() || unidadSeleccionada == null || categoriaSeleccionada == null) {
            mostrarError("Campos Incompletos", "Por favor, el Nombre, la Unidad de medida y la Categoría son obligatorios.");
            return;
        }

        int stockFinal = 0;
        int stockMinFinal = 0;
        double precioFinal = 0.0;

        try {
            String txtS = txtStock.getText() != null ? txtStock.getText().trim() : "";
            if (!txtS.isEmpty()) stockFinal = Integer.parseInt(txtS);

            String txtSM = txtStockMinimo.getText() != null ? txtStockMinimo.getText().trim() : "";
            if (!txtSM.isEmpty()) stockMinFinal = Integer.parseInt(txtSM);

            String txtP = txtPrecio.getText() != null ? txtPrecio.getText().trim() : "";
            if (!txtP.isEmpty()) precioFinal = Double.parseDouble(txtP.replace(",", "."));

            if (stockFinal < 0 || stockMinFinal < 0 || precioFinal < 0) {
                mostrarError("Valores no válidos", "El stock y el precio no pueden ser números negativos.");
                return;
            }

        } catch (NumberFormatException e) {
            mostrarError("Formato incorrecto", "Revisa los campos de Stock y Precio. Solo se admiten números.");
            return;
        }

        try {
            Material nuevoMaterial = new Material();
            nuevoMaterial.setNombre(nombre.trim());
            nuevoMaterial.setUnidad(unidadSeleccionada);
            nuevoMaterial.setCategoria(categoriaSeleccionada);
            nuevoMaterial.setStock(stockFinal);
            nuevoMaterial.setStockMinimo(stockMinFinal);
            nuevoMaterial.setPrecio(precioFinal);

            materialRepository.save(nuevoMaterial);
            cerrarModal();

        } catch (Exception e) {
            mostrarError("Error al guardar", "Hubo un problema de conexión con la base de datos al guardar el material.");
        }
    }

    /**
     * Crea una pequeña ventana de alerta para avisar al administrador de que ha cometido un fallo al rellenar los datos.
     *
     * @param cabecera Titulo principal del mensaje de fallo.
     * @param mensaje Explicacion detallada del problema.
     */
    // Método auxiliar para lanzar las ventanas de error
    private void mostrarError(String cabecera, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Validación");
        alert.setHeaderText(cabecera);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Cierra la ventana emergente actual y devuelve el control a la pantalla del inventario.
     */
    @FXML
    public void cerrarModal() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}