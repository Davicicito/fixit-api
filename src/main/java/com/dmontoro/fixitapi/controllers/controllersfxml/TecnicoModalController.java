package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Categoria;
import com.dmontoro.fixitapi.models.Tecnico;
import com.dmontoro.fixitapi.repositories.CategoriaRepository;
import com.dmontoro.fixitapi.repositories.TecnicoRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Controller
public class TecnicoModalController implements Initializable {

    @FXML private Label lblTituloModal;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private PasswordField txtPassword;

    // Aquí es donde meteremos las píldoras
    @FXML private FlowPane flowEspecialidades;

    @FXML private ComboBox<String> comboEstado;
    @FXML private TextField txtCalificacion;
    @FXML private Button btnGuardar;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Tecnico tecnicoActual;

    // Guardamos la lista de botones para luego saber cuáles ha clicado
    private List<ToggleButton> botonesEspecialidad = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboEstado.setItems(FXCollections.observableArrayList("ACTIVO", "INACTIVO"));
        comboEstado.setValue("ACTIVO");
    }

    public void cargarDatosTecnico(Tecnico t) {
        this.tecnicoActual = t;

        // Limpiamos los colores anteriores del botón
        btnGuardar.getStyleClass().removeAll("btn-primary", "btn-green");

        if (t == null) {
            lblTituloModal.setText("Nuevo Técnico");
            btnGuardar.setText("Crear Técnico");
            btnGuardar.getStyleClass().add("btn-primary"); // Morado
            txtCalificacion.setText("0.0");
        } else {
            lblTituloModal.setText("Editar Técnico: " + t.getNombre());
            btnGuardar.setText("Actualizar Técnico");
            btnGuardar.getStyleClass().add("btn-green"); // Verde

            txtNombre.setText(t.getNombre());
            txtEmail.setText(t.getEmail());
            txtTelefono.setText(t.getTelefono());
            txtCalificacion.setText(t.getCalificacion() != null ? String.valueOf(t.getCalificacion()) : "0.0");
            comboEstado.setValue(t.getActivo() != null && t.getActivo() ? "ACTIVO" : "INACTIVO");
        }

        // --- MAGIA: CREAR LAS PÍLDORAS DE ESPECIALIDADES ---
        flowEspecialidades.getChildren().clear();
        botonesEspecialidad.clear();

        List<Categoria> categoriasBD = categoriaRepository.findAll();
        String especialidadesActuales = (t != null && t.getEspecialidad() != null) ? t.getEspecialidad().toUpperCase() : "";

        for (Categoria c : categoriasBD) {
            ToggleButton pill = new ToggleButton(c.getNombre().toUpperCase());
            pill.getStyleClass().add("pill-especialidad");

            // Si el técnico ya tenía esta especialidad, la dejamos clicada (en azul)
            if (especialidadesActuales.contains(c.getNombre().toUpperCase())) {
                pill.setSelected(true);
            }

            botonesEspecialidad.add(pill);
            flowEspecialidades.getChildren().add(pill);
        }
    }

    @FXML
    public void guardarTecnico() {
        if (txtNombre.getText().isEmpty() || txtEmail.getText().isEmpty()) {
            mostrarError("El nombre y el email son obligatorios.");
            return;
        }

        if (tecnicoActual == null && txtPassword.getText().isEmpty()) {
            mostrarError("Debes asignar una contraseña inicial para el nuevo técnico.");
            return;
        }

        try {
            if (tecnicoActual == null) {
                tecnicoActual = new Tecnico();
                tecnicoActual.setRol("TECNICO");
            }

            tecnicoActual.setNombre(txtNombre.getText().trim());
            tecnicoActual.setEmail(txtEmail.getText().trim());
            tecnicoActual.setTelefono(txtTelefono.getText().trim());

            // Recogemos todos los botones que estén clicados y los juntamos con comas
            String especialidadesSeleccionadas = botonesEspecialidad.stream()
                    .filter(ToggleButton::isSelected)
                    .map(ToggleButton::getText)
                    .collect(Collectors.joining(", "));

            tecnicoActual.setEspecialidad(especialidadesSeleccionadas);

            tecnicoActual.setActivo("ACTIVO".equals(comboEstado.getValue()));

            if (!txtPassword.getText().isEmpty()) {
                tecnicoActual.setPassword(txtPassword.getText());
            }

            double calif = Double.parseDouble(txtCalificacion.getText().replace(",", "."));
            if (calif < 0) calif = 0;
            if (calif > 5) calif = 5;
            tecnicoActual.setCalificacion(calif);

            tecnicoRepository.save(tecnicoActual);
            cerrarModal();

        } catch (Exception e) {
            mostrarError("Error al guardar. Revisa que la calificación sea un número y los datos sean correctos.");
        }
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