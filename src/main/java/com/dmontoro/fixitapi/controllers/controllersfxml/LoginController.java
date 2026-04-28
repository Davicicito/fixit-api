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

    // Inyectamos el contexto de Spring para poder pasárselo al Dashboard luego
    @Autowired private ConfigurableApplicationContext springContext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. ANIMACIÓN DE LA LLAVE (Magia visual)
        RotateTransition rt = new RotateTransition(Duration.seconds(4), iconoLlave);
        rt.setByAngle(360); // Que dé una vuelta completa
        rt.setCycleCount(Animation.INDEFINITE); // Que no pare nunca
        rt.setInterpolator(Interpolator.LINEAR); // Velocidad constante (sin acelerar ni frenar)
        rt.play();


        // 2. CARGAR DATOS REALES DE LA BASE DE DATOS
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

    @FXML
    public void iniciarSesion(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        // 1. ¿Ha escrito algo?
        if (email.isEmpty() || password.isEmpty()) {
            mostrarError("Campos vacíos", "Por favor, introduce tu correo y contraseña.");
            return;
        }

        // 2. Buscamos a la persona en la base de datos
        // Usamos Técnico porque en tu BD todos (incluso el ADMIN) están en esa tabla
        com.dmontoro.fixitapi.models.Tecnico usuarioLogueado = tecnicoRepository.findAll().stream()
                .filter(t -> t.getEmail() != null && t.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        // 3. ¿Existe y la contraseña es correcta?
        if (usuarioLogueado == null || !password.equals(usuarioLogueado.getPassword())) {
            mostrarError("Credenciales incorrectas", "El correo o la contraseña no son válidos.");
            return;
        }

        // 4. EL PORTERO: ¿Es el Jefe (ADMIN)?
        if (!"ADMIN".equalsIgnoreCase(usuarioLogueado.getRol())) {
            mostrarError("Acceso Denegado", "Esta aplicación de escritorio es solo para Administradores. Los técnicos deben acceder a través de la App móvil.");
            return;
        }

        // 5. SI HA LLEGADO AQUÍ... ¡PUEDE PASAR!
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Dashboard.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // Pasamos los datos REALES de la base de datos al Dashboard
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

    // Método auxiliar para mostrar los mensajes de error con una ventana bonita
    private void mostrarError(String titulo, String mensaje) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error de inicio de sesión");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }}