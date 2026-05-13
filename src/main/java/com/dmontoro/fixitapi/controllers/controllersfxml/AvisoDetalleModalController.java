package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.models.AvisoMaterial;
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

        // Construimos el texto con la descripción y le pegamos los materiales al final
        StringBuilder descripcionCompleta = new StringBuilder();
        descripcionCompleta.append(aviso.getDescripcion() != null ? aviso.getDescripcion() : "");

        if (aviso.getAvisoMateriales() != null && !aviso.getAvisoMateriales().isEmpty()) {
            descripcionCompleta.append("\n\n--- MATERIALES UTILIZADOS ---\n");
            for (AvisoMaterial am : aviso.getAvisoMateriales()) {
                descripcionCompleta.append("• ")
                        .append(am.getMaterial().getNombre())
                        .append(": ")
                        .append(am.getCantidad())
                        .append(" ")
                        .append(am.getMaterial().getUnidad())
                        .append("\n");
            }
        }

        txtDescripcion.setText(descripcionCompleta.toString());

        comboEstado.setItems(FXCollections.observableArrayList("PENDIENTE", "EN PROGRESO", "COMPLETADO"));
        comboEstado.setValue(aviso.getEstado());

        btnVerFoto.setDisable(aviso.getFotoAveria() == null || aviso.getFotoAveria().trim().isEmpty());
        btnVerFirma.setDisable(aviso.getFirmaCliente() == null || aviso.getFirmaCliente().trim().isEmpty());
    }

    @FXML
    public void verFotoAveria() {
        abrirVisorImagenes("Foto de la Avería - Aviso #" + avisoActual.getId(), avisoActual.getFotoAveria());
    }

    @FXML
    public void verFirmaCliente() {
        abrirVisorImagenes("Firma del Cliente - Aviso #" + avisoActual.getId(), avisoActual.getFirmaCliente());
    }

    private void abrirVisorImagenes(String titulo, String base64String) {
        try {
            String textoLimpio = base64String.replaceAll("[\\n\\r\\t ]", "");
            if (textoLimpio.contains(",")) {
                textoLimpio = textoLimpio.substring(textoLimpio.indexOf(",") + 1);
            }

            byte[] imageBytes = java.util.Base64.getDecoder().decode(textoLimpio);
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(imageBytes);
            Image imagen = new Image(bis);

            ImageView imageView = new ImageView(imagen);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(600);
            imageView.setFitHeight(500);

            javafx.scene.layout.StackPane layout = new javafx.scene.layout.StackPane();
            layout.setStyle("-fx-background-color: #0F172A; -fx-padding: 20;");
            layout.getChildren().add(imageView);

            Stage visorStage = new Stage();
            visorStage.setTitle(titulo);
            visorStage.initModality(Modality.APPLICATION_MODAL);
            visorStage.setScene(new Scene(layout));
            visorStage.showAndWait();

        } catch (IllegalArgumentException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Formato");
            alert.setHeaderText("La imagen no se puede mostrar");
            alert.setContentText("El formato de la imagen tiene caracteres inválidos o se guardó incompleta.");
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error al cargar la imagen");
            alert.setContentText(e.getMessage());
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