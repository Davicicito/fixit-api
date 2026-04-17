package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Cliente;
import com.dmontoro.fixitapi.repositories.ClienteRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class ClienteModalController implements Initializable {

    @FXML private Label lblTituloModal;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private TextArea txtNotas;

    @FXML private ToggleButton btnEmpresa;
    @FXML private ToggleButton btnParticular;
    @FXML private Button btnGuardar;

    @Autowired
    private ClienteRepository clienteRepository;

    private Cliente clienteActual;
    private ToggleGroup grupoTipo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Agrupamos los botones para que solo se pueda elegir uno a la vez
        grupoTipo = new ToggleGroup();
        btnEmpresa.setToggleGroup(grupoTipo);
        btnParticular.setToggleGroup(grupoTipo);

        // Por defecto, marcamos Empresa
        btnEmpresa.setSelected(true);
    }

    public void cargarDatosCliente(Cliente c) {
        this.clienteActual = c;

        if (c == null) {
            lblTituloModal.setText("Nuevo Cliente");
            btnGuardar.setText("Crear Cliente");
        } else {
            lblTituloModal.setText("Editar Cliente");
            btnGuardar.setText("Actualizar Cliente");

            txtNombre.setText(c.getNombre());
            txtEmail.setText(c.getEmail());
            txtTelefono.setText(c.getTelefono());
            txtDireccion.setText(c.getDireccion());
            txtNotas.setText(c.getNotas());

            if ("PARTICULAR".equalsIgnoreCase(c.getTipo())) {
                btnParticular.setSelected(true);
            } else {
                btnEmpresa.setSelected(true);
            }
        }
    }

    @FXML
    public void guardarCliente() {
        // Validaciones básicas de campos con asterisco
        if (txtNombre.getText().isEmpty() || txtEmail.getText().isEmpty() || txtTelefono.getText().isEmpty() || txtDireccion.getText().isEmpty()) {
            mostrarError("Por favor, rellena todos los campos obligatorios marcados con asterisco (*).");
            return;
        }

        try {
            if (clienteActual == null) {
                clienteActual = new Cliente();
            }

            clienteActual.setNombre(txtNombre.getText().trim());
            clienteActual.setEmail(txtEmail.getText().trim());
            clienteActual.setTelefono(txtTelefono.getText().trim());
            clienteActual.setDireccion(txtDireccion.getText().trim());
            clienteActual.setNotas(txtNotas.getText().trim());

            // Leemos qué botón está pulsado
            clienteActual.setTipo(btnEmpresa.isSelected() ? "EMPRESA" : "PARTICULAR");

            clienteRepository.save(clienteActual);
            cerrarModal();

        } catch (Exception e) {
            mostrarError("Ha ocurrido un error al guardar el cliente. Verifica los datos.");
        }
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