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

/**
 * Controlador para la ventana emergente de clientes.
 * Sirve para registrar un cliente nuevo en el sistema o para editar la informacion de uno ya existente.
 */
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

    /**
     * Prepara los elementos de la pantalla al abrir la ventana.
     * Agrupa los botones de tipo de cliente para que solo se pueda marcar uno y selecciona la opcion de empresa por defecto.
     *
     * @param location  La ubicacion del archivo visual.
     * @param resources Los recursos necesarios para cargar la vista.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Agrupamos los botones para que solo se pueda elegir uno a la vez
        grupoTipo = new ToggleGroup();
        btnEmpresa.setToggleGroup(grupoTipo);
        btnParticular.setToggleGroup(grupoTipo);

        // Por defecto, marcamos Empresa
        btnEmpresa.setSelected(true);
    }

    /**
     * Configura la ventana dependiendo de la accion elegida.
     * Si recibe un cliente vacio, prepara los textos para crear uno nuevo.
     * Si recibe un cliente con datos, rellena todos los recuadros de la pantalla con su informacion para poder modificarla.
     *
     * @param c El cliente con los datos extraidos de la base de datos.
     */
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

    /**
     * Recoge lo escrito en el formulario y verifica que los campos principales no esten vacios.
     * Si todo esta bien, guarda el cliente en la base de datos y cierra esta ventana.
     * Si ocurre un problema, muestra un mensaje de fallo.
     */
    @FXML
    public void guardarCliente() {
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

    /**
     * Lanza una alerta visual en la pantalla para avisar al usuario de que falta un campo por rellenar o ha ocurrido un fallo al guardar.
     *
     * @param mensaje El texto explicativo que se mostrara dentro de la alerta.
     */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Validación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Cierra la ventana emergente actual y devuelve el control a la pantalla principal.
     */
    @FXML
    public void cerrarModal() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}