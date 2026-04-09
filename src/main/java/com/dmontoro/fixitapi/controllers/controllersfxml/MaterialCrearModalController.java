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
        // 1. Cargar las unidades fijas
        comboUnidad.setItems(FXCollections.observableArrayList("unidad", "metros", "litros", "kilos", "cajas"));
        comboUnidad.setValue("unidad"); // Valor por defecto

        // 2. Cargar Categorías desde MySQL
        comboCategoria.setItems(FXCollections.observableArrayList(categoriaRepository.findAll()));
        comboCategoria.setConverter(new StringConverter<Categoria>() {
            @Override public String toString(Categoria c) { return c != null ? c.getNombre() : ""; }
            @Override public Categoria fromString(String s) { return null; }
        });
    }

    @FXML
    public void crearMaterial() {
        try {
            // Validaciones básicas de que haya puesto al menos el nombre
            if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
                mostrarError("El nombre del material es obligatorio.");
                return;
            }

            Material nuevoMaterial = new Material();
            nuevoMaterial.setNombre(txtNombre.getText().trim());
            nuevoMaterial.setUnidad(comboUnidad.getValue());
            nuevoMaterial.setCategoria(comboCategoria.getValue());

            // Convertimos los textos a números (Si están vacíos, ponemos 0)
            nuevoMaterial.setStock(parsearEntero(txtStock.getText()));
            nuevoMaterial.setStockMinimo(parsearEntero(txtStockMinimo.getText()));
            nuevoMaterial.setPrecio(parsearDecimal(txtPrecio.getText()));

            // Guardar en la Base de Datos
            materialRepository.save(nuevoMaterial);

            cerrarModal();

        } catch (Exception e) {
            mostrarError("Revisa los datos introducidos. Asegúrate de que los precios y el stock son números válidos.");
        }
    }

    // Funciones de ayuda para que el programa no explote si el usuario escribe letras en los números
    private Integer parsearEntero(String texto) {
        if (texto == null || texto.trim().isEmpty()) return 0;
        try { return Integer.parseInt(texto.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private Double parsearDecimal(String texto) {
        if (texto == null || texto.trim().isEmpty()) return 0.0;
        try {
            // Cambiamos comas por puntos por si el usuario escribe "3,50" en vez de "3.50"
            return Double.parseDouble(texto.trim().replace(",", "."));
        }
        catch (NumberFormatException e) { return 0.0; }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Validación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void cerrarModal() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}