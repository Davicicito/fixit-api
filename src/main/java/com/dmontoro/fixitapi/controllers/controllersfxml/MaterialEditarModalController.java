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

/**
 * Controlador de la ventana emergente que permite modificar un material que ya existe en el almacen.
 * Se encarga de mostrar la informacion previa y guardar los cambios en el sistema si todo es correcto.
 */
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

    /**
     * Recibe el material seleccionado en la tabla y rellena todos los huecos del formulario con sus datos actuales.
     * Tambien prepara las listas desplegables de categorias y medidas para que el administrador pueda cambiarlas si lo necesita.
     *
     * @param material El objeto material con la informacion sacada de la base de datos.
     */
    public void cargarDatosMaterial(Material material) {
        this.materialActual = material;
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

    /**
     * Lee lo que el usuario ha modificado en la pantalla y comprueba que al menos el nombre no este vacio.
     * Toma los nuevos valores, los guarda en la base de datos para sobrescribir los antiguos y cierra la ventana.
     */
    @FXML
    public void actualizarMaterial() {
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre del material es obligatorio.");
            return;
        }

        try {
            // Actualizamos el objeto
            materialActual.setNombre(txtNombre.getText().trim());
            materialActual.setUnidad(comboUnidad.getValue());
            materialActual.setCategoria(comboCategoria.getValue());
            materialActual.setStock(parsearEntero(txtStock.getText()));
            materialActual.setStockMinimo(parsearEntero(txtStockMinimo.getText()));
            materialActual.setPrecio(parsearDecimal(txtPrecio.getText()));

            materialRepository.save(materialActual);
            cerrarModal();

        } catch (Exception e) {
            mostrarError("Revisa los datos introducidos. Verifica que stock y precio sean numéricos.");
        }
    }

    /**
     * Metodo de ayuda que coge el texto escrito en la casilla de stock y lo convierte en un numero entero sin decimales.
     * Si falla o esta vacio devuelve un cero por seguridad.
     *
     * @param texto La cantidad escrita por el usuario.
     * @return El numero entero listo para guardar en el sistema.
     */
    private Integer parsearEntero(String texto) {
        if (texto == null || texto.trim().isEmpty()) return 0;
        try { return Integer.parseInt(texto.trim()); } catch (Exception e) { return 0; }
    }

    /**
     * Metodo de ayuda que coge el texto del precio y lo convierte en un numero con decimales.
     * Ademas cambia las comas por puntos para que el sistema informatico no de errores al guardar.
     *
     * @param texto El precio escrito por el administrador.
     * @return El numero decimal preparado para usar.
     */
    private Double parsearDecimal(String texto) {
        if (texto == null || texto.trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(texto.trim().replace(",", ".")); } catch (Exception e) { return 0.0; }
    }

    /**
     * Saca un mensaje visual de fallo en la pantalla para avisar de que algun dato introducido es incorrecto o falta.
     *
     * @param mensaje La explicacion del error que vera la persona.
     */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Cierra esta pequeña ventana y devuelve el control a la pantalla del inventario general.
     */
    @FXML
    public void cerrarModal() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}