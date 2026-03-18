package com.dmontoro.fixitapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


//http://localhost:8080/swagger-ui/index.html
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "FixIt API", version = "1.0", description = "API REST para la gestión de partes de trabajo técnicos"))
public class FixitApiApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception {
        // Arrancamos el motor de Spring Boot en segundo plano
        springContext = SpringApplication.run(FixitApiApplication.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/FXML/Login.fxml"));
        // El puente mágico: Spring controla los Controladores de JavaFX
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1024, 768);

        primaryStage.setTitle("FixIt - Panel de Control");
        primaryStage.setScene(scene);

        // Si en tu diseño prefieres que arranque maximizada como en Uber:
         primaryStage.setMaximized(true);

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // Al cerrar la ventana, apagamos el servidor
        springContext.close();
    }

    public static void main(String[] args) {
        // Método invocado desde el FixitLauncher
        Application.launch(FixitApiApplication.class, args);
    }
}