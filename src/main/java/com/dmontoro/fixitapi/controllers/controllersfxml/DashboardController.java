package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.repositories.AvisoRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Controller
@Scope("prototype")
public class DashboardController implements Initializable {

    @FXML private Label lblPendientes;
    @FXML private Label lblEnProgreso;
    @FXML private Label lblCompletados;
    @FXML private Label lblUrgentes;

    @FXML private BarChart<String, Number> barChartActividad;
    @FXML private PieChart pieChartCategoria;
    @FXML private LineChart<String, Number> lineChartTendencia;

    @FXML private VBox vboxActividadReciente;

    @FXML private Label lblCrecimientoIcon;
    @FXML private Label lblCrecimientoValor;
    @FXML private Label lblAvatar;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;

    @Autowired
    private AvisoRepository avisoRepository;

    @Autowired
    private org.springframework.context.ConfigurableApplicationContext springContext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Aviso> avisosReales = avisoRepository.findAll();

        cargarKpisReales(avisosReales);
        cargarGraficoCircularReal(avisosReales);
        cargarGraficoBarrasSemanalReal(avisosReales);
        cargarGraficoLineasMensualReal(avisosReales);
        cargarActividadRecienteReal(avisosReales);
        calcularCrecimientoMensual(avisosReales);
    }

    private void cargarKpisReales(List<Aviso> avisos) {
        long pendientes = 0;
        long enProgreso = 0;
        long completados = 0;

        for (Aviso aviso : avisos) {
            if ("PENDIENTE".equalsIgnoreCase(aviso.getEstado())) pendientes++;
            else if ("EN PROGRESO".equalsIgnoreCase(aviso.getEstado())) enProgreso++;
            else if ("COMPLETADO".equalsIgnoreCase(aviso.getEstado())) completados++;
        }

        lblPendientes.setText(String.valueOf(pendientes));
        lblEnProgreso.setText(String.valueOf(enProgreso));
        lblCompletados.setText(String.valueOf(completados));
        lblUrgentes.setText(String.valueOf(avisos.size()));
    }

    private void cargarGraficoCircularReal(List<Aviso> avisos) {
        pieChartCategoria.getData().clear();

        Map<String, Long> conteoPorCategoria = avisos.stream()
                .filter(a -> a.getCategoria() != null)
                .collect(Collectors.groupingBy(a -> a.getCategoria().getNombre(), Collectors.counting()));

        for (Map.Entry<String, Long> entry : conteoPorCategoria.entrySet()) {
            pieChartCategoria.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
    }

    private void cargarGraficoBarrasSemanalReal(List<Aviso> avisos) {
        barChartActividad.getData().clear();

        XYChart.Series<String, Number> seriesCompletados = new XYChart.Series<>();
        seriesCompletados.setName("Completados");

        XYChart.Series<String, Number> seriesNuevos = new XYChart.Series<>();
        seriesNuevos.setName("Nuevos (Pendientes)");

        int[] completadosPorDia = new int[8];
        int[] nuevosPorDia = new int[8];

        for (Aviso a : avisos) {
            if (a.getFechaCreacion() != null) {
                int diaSemana = a.getFechaCreacion().getDayOfWeek().getValue();
                if ("COMPLETADO".equalsIgnoreCase(a.getEstado())) {
                    completadosPorDia[diaSemana]++;
                } else {
                    nuevosPorDia[diaSemana]++;
                }
            }
        }

        String[] nombresDias = {"", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        for (int i = 1; i <= 7; i++) {
            seriesCompletados.getData().add(new XYChart.Data<>(nombresDias[i], completadosPorDia[i]));
            seriesNuevos.getData().add(new XYChart.Data<>(nombresDias[i], nuevosPorDia[i]));
        }

        barChartActividad.getData().addAll(seriesCompletados, seriesNuevos);

        Platform.runLater(() -> {
            for (XYChart.Series<String, Number> serie : barChartActividad.getData()) {
                for (XYChart.Data<String, Number> dato : serie.getData()) {
                    if (dato.getNode() != null && dato.getYValue().intValue() > 0) {
                        Tooltip tooltip = new Tooltip(serie.getName() + "\n" + dato.getXValue() + ": " + dato.getYValue() + " avisos");
                        Tooltip.install(dato.getNode(), tooltip);
                    }
                }
            }
        });
    }

    private void cargarGraficoLineasMensualReal(List<Aviso> avisos) {
        lineChartTendencia.getData().clear();

        XYChart.Series<String, Number> seriesTendencia = new XYChart.Series<>();
        seriesTendencia.setName("Avisos Totales");

        int[] avisosPorMes = new int[13];

        for (Aviso a : avisos) {
            if (a.getFechaCreacion() != null) {
                int mes = a.getFechaCreacion().getMonthValue();
                avisosPorMes[mes]++;
            }
        }

        String[] nombresMeses = {"", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

        for (int i = 1; i <= 3; i++) {
            seriesTendencia.getData().add(new XYChart.Data<>(nombresMeses[i], avisosPorMes[i]));
        }

        lineChartTendencia.getData().add(seriesTendencia);

        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> dato : seriesTendencia.getData()) {
                if (dato.getNode() != null && dato.getYValue().intValue() > 0) {
                    Tooltip tooltip = new Tooltip("Mes de " + dato.getXValue() + "\nTotal: " + dato.getYValue() + " avisos");
                    Tooltip.install(dato.getNode(), tooltip);
                }
            }
        });
    }

    private void cargarActividadRecienteReal(List<Aviso> avisos) {
        vboxActividadReciente.getChildren().clear();

        List<Aviso> recientes = avisos.stream()
                .sorted(Comparator.comparingLong(Aviso::getId).reversed())
                .limit(3)
                .collect(Collectors.toList());

        for (Aviso aviso : recientes) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("list-item");
            row.setPadding(new javafx.geometry.Insets(12, 0, 12, 0)); // Más espacio para respirar

            // 1. CONTENEDOR FIJO PARA EL ICONO (Soluciona el aplastamiento)
            StackPane iconWrapper = new StackPane();
            iconWrapper.setPrefSize(42, 42); // Medida exacta cuadrada
            iconWrapper.setMinSize(42, 42);
            iconWrapper.getStyleClass().add("list-icon-wrapper");

            Label iconLabel = new Label("🔧");
            iconLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white;");

            // Configurar el icono por la Categoría
            if (aviso.getCategoria() != null) {
                String cat = aviso.getCategoria().getNombre().toLowerCase();
                if (cat.contains("elec")) iconLabel.setText("⚡");
                else if (cat.contains("font")) iconLabel.setText("💧");
                else iconLabel.setText("⚙️");
            }

            // ¡LA FORMA PROFESIONAL! Asignar color dinámico y estable
            String colorFondo = "bg-blue"; // Por defecto
            if (aviso.getTecnico() != null) {
                String nombreTecnico = aviso.getTecnico().getNombre();

                // Nuestra paleta de 8 colores del CSS
                String[] colores = {
                        "bg-blue", "bg-orange", "bg-purple", "bg-green",
                        "bg-red", "bg-teal", "bg-pink", "bg-indigo"
                };

                // Usamos una función matemática (hashCode) para convertir el nombre en un número.
                // Así, "Juan Pérez" SIEMPRE dará el mismo número y tendrá el mismo color.
                int indiceColor = Math.abs(nombreTecnico.hashCode()) % colores.length;
                colorFondo = colores[indiceColor];
            }
            iconWrapper.getStyleClass().add(colorFondo);
            iconWrapper.getChildren().add(iconLabel);

            // 2. Textos Centrales
            VBox midBox = new VBox(2);
            HBox.setHgrow(midBox, Priority.ALWAYS);

            String nombreCliente = (aviso.getCliente() != null) ? aviso.getCliente().getNombre() : "Cliente Desconocido";
            Label title = new Label(nombreCliente);
            title.getStyleClass().add("list-title");

            String nombreTecnico = (aviso.getTecnico() != null) ? aviso.getTecnico().getNombre() : "Sin Asignar";
            Label subtitle = new Label("Técnico: " + nombreTecnico);
            subtitle.getStyleClass().add("list-subtitle");

            midBox.getChildren().addAll(title, subtitle);

            // 3. Píldora de Estado y Fecha
            VBox rightBox = new VBox(5);
            rightBox.setAlignment(Pos.CENTER_RIGHT);

            Label status = new Label(aviso.getEstado());
            status.getStyleClass().add("status-pill");
            if ("COMPLETADO".equalsIgnoreCase(aviso.getEstado())) {
                status.getStyleClass().add("status-completado");
            } else if ("PENDIENTE".equalsIgnoreCase(aviso.getEstado())) {
                status.getStyleClass().add("status-pendiente");
            } else {
                status.getStyleClass().add("status-progreso");
            }

            // Formatear la fecha
            String tiempoStr = "Desconocido";
            if (aviso.getFechaCreacion() != null) {
                LocalDate fecha = aviso.getFechaCreacion();
                if (fecha.equals(LocalDate.now())) {
                    tiempoStr = "Hoy";
                } else if (fecha.equals(LocalDate.now().minusDays(1))) {
                    tiempoStr = "Ayer";
                } else {
                    tiempoStr = fecha.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
                }
            }
            Label time = new Label(tiempoStr);
            time.getStyleClass().add("list-time");

            rightBox.getChildren().addAll(status, time);

            // 4. Juntarlo todo
            row.getChildren().addAll(iconWrapper, midBox, rightBox);
            vboxActividadReciente.getChildren().add(row);
        }
    }

    private void calcularCrecimientoMensual(List<Aviso> avisos) {
        LocalDate hoy = LocalDate.now();
        int mesActual = hoy.getMonthValue();
        int anioActual = hoy.getYear();

        // Calculamos cuál fue el mes pasado (teniendo en cuenta si estamos en enero)
        int mesAnterior = (mesActual == 1) ? 12 : mesActual - 1;
        int anioAnterior = (mesActual == 1) ? anioActual - 1 : anioActual;

        long totalMesActual = 0;
        long totalMesAnterior = 0;

        // Contamos cuántos avisos hubo este mes y el mes pasado
        for (Aviso a : avisos) {
            if (a.getFechaCreacion() != null) {
                LocalDate fecha = a.getFechaCreacion();
                if (fecha.getMonthValue() == mesActual && fecha.getYear() == anioActual) {
                    totalMesActual++;
                } else if (fecha.getMonthValue() == mesAnterior && fecha.getYear() == anioAnterior) {
                    totalMesAnterior++;
                }
            }
        }

        // Calculamos el porcentaje matemático
        double porcentaje = 0.0;
        if (totalMesAnterior == 0) {
            if (totalMesActual > 0) porcentaje = 100.0; // Si pasas de 0 a algo, es un 100% de crecimiento
        } else {
            porcentaje = ((double) (totalMesActual - totalMesAnterior) / totalMesAnterior) * 100.0;
        }

        // Lo pintamos en pantalla cambiando colores
        if (porcentaje > 0) {
            lblCrecimientoValor.setText(String.format("+%.1f%%", porcentaje));
            lblCrecimientoValor.setStyle("-fx-text-fill: #10B981;"); // Verde éxito
            lblCrecimientoIcon.setText("📈");
            lblCrecimientoIcon.setStyle("-fx-text-fill: #10B981;");
        } else if (porcentaje < 0) {
            lblCrecimientoValor.setText(String.format("%.1f%%", porcentaje)); // El signo menos ya se pone solo
            lblCrecimientoValor.setStyle("-fx-text-fill: #EF4444;"); // Rojo alerta
            lblCrecimientoIcon.setText("📉");
            lblCrecimientoIcon.setStyle("-fx-text-fill: #EF4444;");
        } else {
            lblCrecimientoValor.setText("0.0%");
            lblCrecimientoValor.setStyle("-fx-text-fill: #6B7280;"); // Gris neutral
            lblCrecimientoIcon.setText("➖");
            lblCrecimientoIcon.setStyle("-fx-text-fill: #6B7280;");
        }
    }
    // Este método lo llamaremos desde el Login justo antes de cambiar de pantalla
    public void setDatosUsuario(String nombreReal, String rolReal) {
        // Ponemos la primera letra en mayúscula por si entra en minúsculas
        String nombreFormateado = nombreReal.substring(0, 1).toUpperCase() + nombreReal.substring(1);

        lblNombreUsuario.setText(nombreFormateado);
        lblRolUsuario.setText(rolReal);

        // Lógica para extraer las iniciales
        String iniciales = "";
        String[] partes = nombreFormateado.trim().split("[ ._]"); // Separa por espacios, puntos o barras bajas
        if (partes.length >= 2) {
            iniciales = partes[0].substring(0, 1) + partes[1].substring(0, 1);
        } else if (nombreFormateado.length() >= 2) {
            iniciales = nombreFormateado.substring(0, 2);
        } else {
            iniciales = nombreFormateado;
        }

        lblAvatar.setText(iniciales.toUpperCase());
    }
    @FXML
    public void cerrarSesion(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Login.fxml"));
            // Importante inyectar el contexto de Spring si lo estás usando en LoginController
            // loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // Obtenemos la ventana actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Pequeño truco para que Windows reajuste bien la ventana al quitar el maximizado
            stage.hide();

            // Le quitamos el maximizado y bloqueamos el redimensionamiento
            stage.setMaximized(false);
            stage.setResizable(false);

            // Volvemos al tamaño original del Login
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("FixIt - Acceso Administrativo");

            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void irAGestionAvisos(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/GestionAvisos.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // Le pasamos los datos del usuario a la nueva pantalla
            GestionAvisosController gestionController = loader.getController();
            gestionController.setDatosUsuario(lblNombreUsuario.getText(), lblRolUsuario.getText());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void irAInventario(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Inventario.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            InventarioController inventarioController = loader.getController();
            inventarioController.setDatosUsuario(lblNombreUsuario.getText(), lblRolUsuario.getText());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void irATecnicos(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Tecnicos.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            // En el Dashboard leemos directamente de los Labels
            TecnicosController tecnicosController = loader.getController();
            tecnicosController.setDatosUsuario(lblNombreUsuario.getText(), lblRolUsuario.getText());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}