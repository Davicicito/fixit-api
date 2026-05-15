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

/**
 * Controlador de la ventana flotante encargada de crear o modificar a los trabajadores.
 * Gestiona el formulario con sus datos personales y permite seleccionar visualmente las especialidades de cada uno.
 */
@Controller
public class TecnicoModalController implements Initializable {

    @FXML private Label lblTituloModal;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private PasswordField txtPassword;

    @FXML private FlowPane flowEspecialidades;

    @FXML private ComboBox<String> comboEstado;
    @FXML private TextField txtCalificacion;
    @FXML private Button btnGuardar;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Tecnico tecnicoActual;

    private List<ToggleButton> botonesEspecialidad = new ArrayList<>();

    /**
     * Metodo que arranca automaticamente al abrir la ventana.
     * Prepara el menu desplegable del estado laboral colocando las opciones de activo e inactivo.
     *
     * @param location Ubicacion del archivo de la interfaz grafica.
     * @param resources Recursos necesarios para construir la ventana.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboEstado.setItems(FXCollections.observableArrayList("ACTIVO", "INACTIVO"));
        comboEstado.setValue("ACTIVO");
    }

    /**
     * Revisa si vamos a crear un tecnico nuevo o a editar uno que ya existe.
     * Si es nuevo prepara todos los campos en blanco. Si ya existe rellena el formulario con su informacion,
     * busca todas las especialidades disponibles en la base de datos y deja pulsadas aquellas en las que el trabajador ya es experto.
     *
     * @param t El empleado con los datos extraidos de la base de datos o un objeto vacio si es nuevo.
     */
    public void cargarDatosTecnico(Tecnico t) {
        this.tecnicoActual = t;

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

        flowEspecialidades.getChildren().clear();
        botonesEspecialidad.clear();

        List<Categoria> categoriasBD = categoriaRepository.findAll();
        String especialidadesActuales = (t != null && t.getEspecialidad() != null) ? t.getEspecialidad().toUpperCase() : "";

        for (Categoria c : categoriasBD) {
            ToggleButton pill = new ToggleButton(c.getNombre().toUpperCase());
            pill.getStyleClass().add("pill-especialidad");

            // Si el técnico ya tenía esta especialidad, la dejamos clicada
            if (especialidadesActuales.contains(c.getNombre().toUpperCase())) {
                pill.setSelected(true);
            }

            botonesEspecialidad.add(pill);
            flowEspecialidades.getChildren().add(pill);
        }
    }

    /**
     * Comprueba que los campos obligatorios esten rellenados y junta todas las especialidades marcadas en un solo bloque de texto.
     * Despues transfiere todos esos datos al objeto del trabajador y lo manda a guardar en la base de datos.
     */
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

            // Recogemos todos los botones que estén clicados
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

    /**
     * Muestra una alerta en pantalla si ocurre algun problema durante el guardado o si el administrador deja datos vacios.
     *
     * @param mensaje Explicacion detallada del error que ha ocurrido.
     */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Cierra la ventana emergente actual y vuelve a la pantalla de la plantilla de empleados.
     */
    @FXML
    public void cerrarModal() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}