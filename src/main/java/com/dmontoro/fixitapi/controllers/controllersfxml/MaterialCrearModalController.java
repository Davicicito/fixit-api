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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarDesplegables();
    }

    private void cargarDesplegables() {
        // 1. Cargar las unidades fijas (EN MAYÚSCULAS PARA QUE QUEDE MÁS BONITO)
        comboUnidad.setItems(FXCollections.observableArrayList("UNIDAD", "METROS", "LITROS", "KILOS", "CAJAS"));

        // ELIMINAMOS el setValue("unidad") para que el desplegable empiece vacío.
        // Así obligamos al usuario a elegir una opción conscientemente.
        comboUnidad.setPromptText("Selecciona unidad");

        // 2. Cargar Categorías desde MySQL
        comboCategoria.setItems(FXCollections.observableArrayList(categoriaRepository.findAll()));
        comboCategoria.setConverter(new StringConverter<Categoria>() {
            @Override public String toString(Categoria c) { return c != null ? c.getNombre() : ""; }
            @Override public Categoria fromString(String s) { return null; }
        });
    }

    @FXML
    public void crearMaterial() {
        // 1. PRIMER MURO: Comprobar campos obligatorios (Nombre, Unidad y Categoría)
        String nombre = txtNombre.getText();
        String unidadSeleccionada = comboUnidad.getValue();
        Categoria categoriaSeleccionada = comboCategoria.getValue();

        if (nombre == null || nombre.trim().isEmpty() || unidadSeleccionada == null || categoriaSeleccionada == null) {
            mostrarError("Campos Incompletos", "Por favor, el Nombre, la Unidad de medida y la Categoría son obligatorios.");
            return; // Cortamos en seco, no se guarda.
        }

        // 2. SEGUNDO MURO: Comprobar que los números son números reales (No letras, no símbolos raros)
        int stockFinal = 0;
        int stockMinFinal = 0;
        double precioFinal = 0.0;

        try {
            // Si el campo está vacío, le ponemos un 0 automáticamente. Si tiene texto, intentamos convertirlo a número.
            String txtS = txtStock.getText() != null ? txtStock.getText().trim() : "";
            if (!txtS.isEmpty()) stockFinal = Integer.parseInt(txtS);

            String txtSM = txtStockMinimo.getText() != null ? txtStockMinimo.getText().trim() : "";
            if (!txtSM.isEmpty()) stockMinFinal = Integer.parseInt(txtSM);

            String txtP = txtPrecio.getText() != null ? txtPrecio.getText().trim() : "";
            if (!txtP.isEmpty()) precioFinal = Double.parseDouble(txtP.replace(",", "."));

            // ¿Qué pasa si intentan meter números negativos? Bloqueo.
            if (stockFinal < 0 || stockMinFinal < 0 || precioFinal < 0) {
                mostrarError("Valores no válidos", "El stock y el precio no pueden ser números negativos.");
                return;
            }

        } catch (NumberFormatException e) {
            // Si el Integer.parseInt o Double.parseDouble fallan porque el usuario ha escrito "cinco" en vez de "5"
            mostrarError("Formato incorrecto", "Revisa los campos de Stock y Precio. Solo se admiten números.");
            return;
        }

        // 3. TODO CORRECTO: Guardamos en base de datos
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

    // Método auxiliar para lanzar las ventanitas de error
    private void mostrarError(String cabecera, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Validación");
        alert.setHeaderText(cabecera);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void cerrarModal() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}