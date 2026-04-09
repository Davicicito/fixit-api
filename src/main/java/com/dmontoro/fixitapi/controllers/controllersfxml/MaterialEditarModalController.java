package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Categoria;
import com.dmontoro.fixitapi.models.Material;
import com.dmontoro.fixitapi.repositories.CategoriaRepository;
import com.dmontoro.fixitapi.repositories.MaterialRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class MaterialEditarModalController {

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

    private Material materialActual;

    // Se llama desde el InventarioController para pasarle los datos
    public void cargarDatosMaterial(Material material) {
        this.materialActual = material;

        // Cargar listas
        comboUnidad.setItems(FXCollections.observableArrayList("unidad", "metros", "litros", "kilos", "cajas"));
        comboCategoria.setItems(FXCollections.observableArrayList(categoriaRepository.findAll()));
        comboCategoria.setConverter(new StringConverter<Categoria>() {
            @Override public String toString(Categoria c) { return c != null ? c.getNombre() : ""; }
            @Override public Categoria fromString(String s) { return null; }
        });

        // Rellenar campos con los datos del material
        txtNombre.setText(material.getNombre());
        txtStock.setText(material.getStock() != null ? String.valueOf(material.getStock()) : "0");
        txtStockMinimo.setText(material.getStockMinimo() != null ? String.valueOf(material.getStockMinimo()) : "0");
        txtPrecio.setText(material.getPrecio() != null ? String.valueOf(material.getPrecio()) : "0.0");

        if (material.getUnidad() != null) comboUnidad.setValue(material.getUnidad());

        // Buscar y seleccionar la categoría actual
        if (material.getCategoria() != null) {
            for (Categoria cat : comboCategoria.getItems()) {
                if (cat.getId().equals(material.getCategoria().getId())) {
                    comboCategoria.setValue(cat);
                    break;
                }
            }
        }
    }

    @FXML
    public void actualizarMaterial() {
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre del material es obligatorio.");
            return;
        }

        try {
            // Actualizamos el objeto que ya teníamos
            materialActual.setNombre(txtNombre.getText().trim());
            materialActual.setUnidad(comboUnidad.getValue());
            materialActual.setCategoria(comboCategoria.getValue());
            materialActual.setStock(parsearEntero(txtStock.getText()));
            materialActual.setStockMinimo(parsearEntero(txtStockMinimo.getText()));
            materialActual.setPrecio(parsearDecimal(txtPrecio.getText()));

            // Guardamos (como el objeto ya tiene un ID, Spring Boot hace un UPDATE)
            materialRepository.save(materialActual);
            cerrarModal();

        } catch (Exception e) {
            mostrarError("Revisa los datos introducidos. Verifica que stock y precio sean numéricos.");
        }
    }

    private Integer parsearEntero(String texto) {
        if (texto == null || texto.trim().isEmpty()) return 0;
        try { return Integer.parseInt(texto.trim()); } catch (Exception e) { return 0; }
    }

    private Double parsearDecimal(String texto) {
        if (texto == null || texto.trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(texto.trim().replace(",", ".")); } catch (Exception e) { return 0.0; }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
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