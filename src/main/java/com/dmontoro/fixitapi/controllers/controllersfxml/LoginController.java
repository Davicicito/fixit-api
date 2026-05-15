package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.repositories.AvisoRepository;
import com.dmontoro.fixitapi.repositories.ClienteRepository;
import com.dmontoro.fixitapi.repositories.TecnicoRepository;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de inicio de sesion.
 * Se encarga de validar las credenciales del usuario para permitirle el acceso al sistema y
 * muestra estadisticas generales animadas en la pantalla de bienvenida.
 */
@Controller
public class LoginController implements Initializable {

    @FXML private Label iconoLlave;
    @FXML private Label lblTotalAvisos;
    @FXML private Label lblTotalTecnicos;
    @FXML private Label lblTotalClientes;
    @FXML private Label lblTasaExito;

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;

    @Autowired private AvisoRepository avisoRepository;
    @Autowired private TecnicoRepository tecnicoRepository;
    @Autowired private ClienteRepository clienteRepository;

    @Autowired private ConfigurableApplicationContext springContext;

    /**
     * Metodo que se ejecuta automaticamente al cargar la pantalla de login.
     * Arranca la animacion de la llave giratoria y descarga de la base de datos
     * el numero total de trabajadores, clientes y trabajos para mostrarlos en el panel lateral.
     * Tambien calcula el porcentaje de exito basandose en los trabajos terminados.
     *
     * @param location Ubicacion del archivo visual.
     * @param resources Recursos necesarios para construir la ventana.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        RotateTransition rt = new RotateTransition(Duration.seconds(4), iconoLlave);
        rt.setByAngle(360);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.play();


        try {
            long totalAvisos = avisoRepository.count();
            lblTotalAvisos.setText(String.valueOf(totalAvisos));
            lblTotalTecnicos.setText(String.valueOf(tecnicoRepository.count()));
            lblTotalClientes.setText(String.valueOf(clienteRepository.count()));

            // Cálculo dinámico de la Tasa de Éxito
            if (totalAvisos > 0) {
                long completados = avisoRepository.findAll().stream()
                        .filter(a -> "COMPLETADO".equalsIgnoreCase(a.getEstado()))
                        .count();

                double porcentajeEfectividad = ((double) completados / totalAvisos) * 100;
                lblTasaExito.setText(String.format("%.0f%%", porcentajeEfectividad));
            } else {
                lblTasaExito.setText("0%");
            }

        } catch (Exception e) {
            System.out.println("Error al cargar contadores. Asegúrate de tener los repositorios creados.");
        }
    }

    /**
     * Recoge los datos escritos en el formulario y comprueba si el usuario existe en la base de datos.
     * Valida que la contraseña sea correcta y que el trabajador tenga permisos de administrador.
     * Si todo esta bien, le da acceso al sistema y carga el panel principal a pantalla completa.
     *
     * @param event Clic del raton sobre el boton de entrar.
     */
    @FXML
    public void iniciarSesion(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            mostrarError("Campos vacíos", "Por favor, introduce tu correo y contraseña.");
            return;
        }

        // Buscamos a la persona en la base de datos
        com.dmontoro.fixitapi.models.Tecnico usuarioLogueado = tecnicoRepository.findAll().stream()
                .filter(t -> t.getEmail() != null && t.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        if (usuarioLogueado == null || !password.equals(usuarioLogueado.getPassword())) {
            mostrarError("Credenciales incorrectas", "El correo o la contraseña no son válidos.");
            return;
        }

        if (!"ADMIN".equalsIgnoreCase(usuarioLogueado.getRol())) {
            mostrarError("Acceso Denegado", "Esta aplicación de escritorio es solo para Administradores. Los técnicos deben acceder a través de la App móvil.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Dashboard.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            DashboardController dashboardController = loader.getController();
            dashboardController.setDatosUsuario(usuarioLogueado.getNombre(), "Administrador General");

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.hide();

            stage.setScene(new Scene(root, 1400, 900));
            stage.setTitle("FixIt - Panel de Control Administrativo");
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error del sistema", "No se pudo cargar la pantalla principal.");
        }
    }

    /**
     * Metodo de ayuda que crea una pequeña ventana emergente para avisar al usuario si se ha equivocado
     * al escribir sus datos o si no tiene permiso para entrar.
     *
     * @param titulo Texto corto que resume el fallo.
     * @param mensaje Explicacion detallada del problema.
     */
    private void mostrarError(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error de inicio de sesión");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}