package com.dmontoro.fixitapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Clase principal que arranca tanto el servidor de datos como la interfaz de usuario.
 * Se encarga de coordinar el inicio de Spring Boot y de configurar la ventana principal
 * de la aplicacion de escritorio colocando el titulo y el icono corporativo.
 */
//http://localhost:8080/swagger-ui/index.html
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "FixIt API", version = "1.0", description = "API REST para la gestión de partes de trabajo técnicos"))
public class FixitApiApplication extends Application {

    private ConfigurableApplicationContext springContext;

    /**
     * Prepara el entorno antes de mostrar la aplicacion.
     * Lanza el motor de Spring Boot para que la base de datos y la API esten listas.
     */
    @Override
    public void init() throws Exception {
        // Arrancamos el motor de Spring Boot en segundo plano
        springContext = SpringApplication.run(FixitApiApplication.class);
    }

    /**
     * Construye la ventana visual de la aplicacion JavaFX.
     * Carga el diseño del login, establece el tamaño de la escena y añade el logo oficial
     * de la empresa en la barra superior de la ventana.
     *
     * @param primaryStage El escenario principal donde se montara toda la interfaz.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/Login.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1024, 768);

        // CONFIGURACIÓN DE LA VENTANA
        primaryStage.setTitle("FixIt - Panel de Control");

        // Carga del logo en la barra de tareas y ventana
        try {
            Image icon = new Image(getClass().getResourceAsStream("/IMAGES/logo.png"));
            if (icon != null) {
                primaryStage.getIcons().add(icon);
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar el logo pero la aplicacion continuara");
        }

        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    /**
     * Detiene todos los procesos cuando el usuario decide salir del programa.
     * Se encarga de apagar el servidor de Spring Boot para liberar la memoria del equipo.
     */
    @Override
    public void stop() throws Exception {
        // Al cerrar la ventana, apagamos el servidor
        springContext.close();
    }

    /**
     * Punto de entrada principal para el arranque de la plataforma.
     * @param args Argumentos de configuracion opcionales.
     */
    public static void main(String[] args) {
        // Método invocado desde el FixitLauncher
        Application.launch(FixitApiApplication.class, args);
    }
}