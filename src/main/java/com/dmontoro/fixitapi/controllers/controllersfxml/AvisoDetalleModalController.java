package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.services.AvisoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;

@Controller
public class AvisoDetalleModalController {

    @FXML private Label lblTituloFormulario;
    @FXML private Label lblCliente;
    @FXML private Label lblTecnico;
    @FXML private Label lblCategoria;
    @FXML private Label lblPrioridad;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<String> comboEstado;

    // Botones multimedia
    @FXML private Button btnVerFoto;
    @FXML private Button btnVerFirma;

    @Autowired
    private AvisoService avisoService;

    private Aviso avisoActual;

    public void cargarDatosAviso(Aviso aviso) {
        this.avisoActual = aviso;

        lblTituloFormulario.setText("Detalles del Aviso #" + aviso.getId());
        lblCliente.setText(aviso.getCliente() != null ? aviso.getCliente().getNombre() : "Sin Cliente");
        lblTecnico.setText(aviso.getTecnico() != null ? aviso.getTecnico().getNombre() : "Sin Asignar");
        lblCategoria.setText(aviso.getCategoria() != null ? aviso.getCategoria().getNombre() : "General");
        lblPrioridad.setText(aviso.getPrioridad() != null ? aviso.getPrioridad() : "MEDIA");
        txtDescripcion.setText(aviso.getDescripcion());

        comboEstado.setItems(FXCollections.observableArrayList("PENDIENTE", "EN PROGRESO", "COMPLETADO"));
        comboEstado.setValue(aviso.getEstado());

        // LÓGICA DE MULTIMEDIA: Desactivar botones si la base de datos dice que no hay foto o firma (NULL)
        btnVerFoto.setDisable(aviso.getFotoAveria() == null || aviso.getFotoAveria().trim().isEmpty());
        btnVerFirma.setDisable(aviso.getFirmaCliente() == null || aviso.getFirmaCliente().trim().isEmpty());
    }

    // Acción del botón Foto
    @FXML
    public void verFotoAveria() {
        abrirVisorImagenes("Foto de la Avería - Aviso #" + avisoActual.getId(), avisoActual.getFotoAveria());
    }

    // Acción del botón Firma
    @FXML
    public void verFirmaCliente() {
        abrirVisorImagenes("Firma del Cliente - Aviso #" + avisoActual.getId(), avisoActual.getFirmaCliente());
    }

    // EL VISOR DE IMÁGENES UNIVERSAL
    private void abrirVisorImagenes(String titulo, String ruta) {
        try {
            // En el futuro, aquí concatenaremos la ruta de XAMPP o del servidor web.
            // Ejemplo futuro: String rutaCompleta = "http://localhost:8080/uploads/" + ruta;

            // Por ahora, intentamos leerla como archivo local o URL simulada
            File archivoFoto = new File(ruta);
            Image imagen;

            if (archivoFoto.exists()) {
                imagen = new Image(archivoFoto.toURI().toString());
            } else {
                // Si el archivo no existe (que es lo que pasará ahora mismo en tu PC)
                throw new Exception("El archivo no existe físicamente en el servidor todavía.");
            }

            // Si la imagen existe, creamos una ventana nueva solo para verla
            ImageView imageView = new ImageView(imagen);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(600); // Tamaño máximo de ancho
            imageView.setFitHeight(500); // Tamaño máximo de alto

            StackPane layout = new StackPane();
            layout.setStyle("-fx-background-color: #0F172A; -fx-padding: 20;"); // Fondo oscuro para que resalte la foto
            layout.getChildren().add(imageView);

            Stage visorStage = new Stage();
            visorStage.setTitle(titulo);
            visorStage.initModality(Modality.APPLICATION_MODAL);
            visorStage.setScene(new Scene(layout));
            visorStage.showAndWait();

        } catch (Exception e) {
            // SI FALLA (como pasará en este sprint), enseñamos un mensaje profesional
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Imagen no disponible");
            alert.setHeaderText("Archivo no encontrado en el servidor");
            alert.setContentText("La base de datos tiene registrada la ruta:\n" + ruta +
                    "\n\nSin embargo, el archivo físico todavía no se ha subido o no está accesible desde este equipo.");
            alert.showAndWait();
        }
    }

    @FXML
    public void guardarCambios() {
        if (avisoActual != null) {
            avisoActual.setEstado(comboEstado.getValue());
            avisoService.actualizarAviso(avisoActual.getId(), avisoActual);
            cerrarModal();
        }
    }

    @FXML
    public void cerrarModal() {
        Stage stage = (Stage) lblTituloFormulario.getScene().getWindow();
        stage.close();
    }
}