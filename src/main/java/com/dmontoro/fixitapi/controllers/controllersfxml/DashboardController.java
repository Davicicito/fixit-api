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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.time.LocalDateTime;
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

    @FXML private PieChart pieChartCategoria;
    @FXML private LineChart<String, Number> lineChartTendencia;
    @FXML private Label lblRangoSemana;
    @FXML private Button btnSemanaSiguiente;

    // AQUÍ ESTÁ EL GRÁFICO NUEVO
    @FXML private javafx.scene.chart.BarChart<String, Number> graficoActividad;

    @FXML private VBox vboxActividadReciente;

    @FXML private SVGPath lblCrecimientoIcon;
    @FXML private Label lblCrecimientoValor;
    @FXML private Label lblAvatar;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;



    private int semanasAtras = 0;

    @Autowired
    private AvisoRepository avisoRepository;

    @Autowired
    private org.springframework.context.ConfigurableApplicationContext springContext;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Aviso> avisosReales = avisoRepository.findAll();

        cargarKpisReales(avisosReales);
        cargarGraficoCircularReal(avisosReales);

        // LLAMAMOS AL MÉTODO NUEVO CON LAS FECHAS CORRECTAS
        cargarGraficoSemanas();

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

    @FXML
    public void semanaAnterior() {
        semanasAtras++;
        cargarGraficoSemanas();
    }

    @FXML
    public void semanaSiguiente() {
        if (semanasAtras > 0) {
            semanasAtras--;
            cargarGraficoSemanas();
        }
    }

    private void cargarGraficoCircularReal(List<Aviso> avisos) {
        pieChartCategoria.getData().clear();

        Map<String, Long> conteoPorCategoria = avisos.stream()
                .filter(a -> a.getCategoria() != null)
                .collect(Collectors.groupingBy(a -> a.getCategoria().getNombre(), Collectors.counting()));

        for (Map.Entry<String, Long> entry : conteoPorCategoria.entrySet()) {
            pieChartCategoria.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        // --- LA MAGIA: COLORES EXACTOS Y BOCADILLOS BLANCOS ---
        Platform.runLater(() -> {
            int index = 0; // Necesario para encontrar la leyenda correcta
            for (PieChart.Data dato : pieChartCategoria.getData()) {
                Node sliceNode = dato.getNode();
                if (sliceNode != null) {
                    String catNombre = dato.getName().toLowerCase();
                    String colorHex = "";

                    // 1. Decidimos el color por el NOMBRE exacto de la categoría
                    if (catNombre.contains("font")) colorHex = "#3B82F6"; // Azul
                    else if (catNombre.contains("elec")) colorHex = "#F59E0B"; // Naranja
                    else if (catNombre.contains("ascen")) colorHex = "#A855F7"; // Morado

                    if (!colorHex.isEmpty()) {
                        // Pintamos el "quesito" del gráfico
                        sliceNode.setStyle(String.format("-fx-pie-color: %s;", colorHex));

                        // Buscamos el cuadradito de la leyenda de abajo y lo pintamos también
                        Node leyenda = pieChartCategoria.lookup(".default-color" + index + ".chart-legend-item-symbol");
                        if (leyenda != null) {
                            leyenda.setStyle(String.format("-fx-background-color: %s;", colorHex));
                        }
                    }

                    // 2. Creación del Bocadillo (Tooltip)
                    int total = (int) dato.getPieValue();
                    Tooltip tooltip = new Tooltip(dato.getName() + " : " + total);
                    tooltip.getStyleClass().add("chart-tooltip");
                    Tooltip.install(sliceNode, tooltip);

                    // 3. Efecto Hover (Usamos setOpacity para no borrar el color del setStyle)
                    sliceNode.setOnMouseEntered(e -> sliceNode.setOpacity(0.8));
                    sliceNode.setOnMouseExited(e -> sliceNode.setOpacity(1.0));
                }
                index++;
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
                // Solo contamos los avisos de ESTE año para que el gráfico sea real
                if (a.getFechaCreacion().getYear() == LocalDate.now().getYear()) {
                    int mes = a.getFechaCreacion().getMonthValue();
                    avisosPorMes[mes]++;
                }
            }
        }

        String[] nombresMeses = {"", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

        // Dibuja la línea desde Enero hasta el mes en el que estemos ahora mismo
        int mesActual = LocalDate.now().getMonthValue();
        for (int i = 1; i <= mesActual; i++) {
            seriesTendencia.getData().add(new XYChart.Data<>(nombresMeses[i], avisosPorMes[i]));
        }

        lineChartTendencia.getData().add(seriesTendencia);

        // --- LOS BOCADILLOS Y ANIMACIONES DEL RATÓN ---
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> dato : seriesTendencia.getData()) {
                Node nodoPunto = dato.getNode();
                if (nodoPunto != null) {

                    // Creamos el bocadillo clavado a tu diseño ("Feb \n avisos : 289")
                    int total = dato.getYValue().intValue();
                    Tooltip tooltip = new Tooltip(dato.getXValue() + "\navisos : " + total);
                    tooltip.getStyleClass().add("chart-tooltip"); // Usa tu CSS blanco con sombra
                    Tooltip.install(nodoPunto, tooltip);

                    // Efecto Premium: El punto "vibra" o se hace grande al pasar el ratón
                    nodoPunto.setOnMouseEntered(e -> {
                        nodoPunto.setStyle("-fx-cursor: hand;");
                        nodoPunto.setScaleX(1.5); // Lo hace un 50% más grande
                        nodoPunto.setScaleY(1.5);
                    });

                    nodoPunto.setOnMouseExited(e -> {
                        nodoPunto.setStyle("");
                        nodoPunto.setScaleX(1.0); // Vuelve a la normalidad
                        nodoPunto.setScaleY(1.0);
                    });
                }
            }
        });
    }
    private void cargarActividadRecienteReal(List<Aviso> avisos) {
        vboxActividadReciente.getChildren().clear();

        // 1. FILTRAMOS SOLO LOS DEL DÍA DE HOY y ordenamos del más nuevo al más viejo
        List<Aviso> recientes = avisos.stream()
                .filter(a -> a.getFechaCreacion() != null && a.getFechaCreacion().toLocalDate().equals(LocalDate.now()))
                .sorted(Comparator.comparing(Aviso::getFechaCreacion).reversed())
                .limit(3)
                .collect(Collectors.toList());

        // Si hoy no hay avisos, mostramos un mensaje limpio
        if(recientes.isEmpty()) {
            Label vacio = new Label("No hay actividad reciente en el día de hoy.");
            vacio.setStyle("-fx-text-fill: #94A3B8; -fx-padding: 20; -fx-font-style: italic;");
            vboxActividadReciente.getChildren().add(vacio);
            return;
        }

        // 2. DIBUJAMOS CADA TARJETA
        for (Aviso aviso : recientes) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new javafx.geometry.Insets(15, 10, 15, 10));
            row.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #F1F5F9; -fx-border-radius: 12; -fx-border-width: 1; -fx-margin-bottom: 10;");

            // --- ICONO SVG Y COLOR DE CATEGORÍA ---
            StackPane iconWrapper = new StackPane();
            iconWrapper.setPrefSize(42, 42);
            iconWrapper.setMinSize(42, 42);

            SVGPath iconPath = new SVGPath();
            iconPath.setStyle("-fx-stroke: white; -fx-fill: transparent; -fx-stroke-width: 2;");

            String wrapperClass = "icon-wrapper-purple"; // Por defecto (Ascensores / Otros)

            if (aviso.getCategoria() != null) {
                String cat = aviso.getCategoria().getNombre().toLowerCase();
                if (cat.contains("font")) {
                    wrapperClass = "icon-wrapper-blue";
                    iconPath.setContent("M12 22a7 7 0 0 0 7-7c0-2-1-3.9-3-5.5s-3.5-4-4-6.5c-.5 2.5-2 4.9-4 6.5C6 11.1 5 13 5 15a7 7 0 0 0 7 7z"); // Droplet
                } else if (cat.contains("elec")) {
                    wrapperClass = "icon-wrapper-orange";
                    iconPath.setContent("M13 2L3 14h9l-1 8 10-12h-9l1-8z"); // Zap
                } else {
                    wrapperClass = "icon-wrapper-purple";
                    iconPath.setContent("M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z M12 15a3 3 0 1 1 0-6 3 3 0 0 1 0 6z"); // Settings
                }
            } else {
                iconPath.setContent("M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z M12 15a3 3 0 1 1 0-6 3 3 0 0 1 0 6z");
            }

            iconWrapper.getStyleClass().add(wrapperClass);
            iconWrapper.getChildren().add(iconPath);

            // --- TEXTOS CENTRALES ---
            VBox midBox = new VBox(2);
            HBox.setHgrow(midBox, Priority.ALWAYS);

            String nombreCliente = (aviso.getCliente() != null) ? aviso.getCliente().getNombre() : "Cliente Desconocido";
            Label title = new Label(nombreCliente);
            title.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F172A; -fx-font-size: 14px;");

            String nombreTecnico = (aviso.getTecnico() != null) ? aviso.getTecnico().getNombre() : "Sin Asignar";
            Label subtitle = new Label("Técnico: " + nombreTecnico);
            subtitle.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");

            midBox.getChildren().addAll(title, subtitle);

            // --- ESTADO Y TIEMPO REAL ---
            VBox rightBox = new VBox(5);
            rightBox.setAlignment(Pos.CENTER_RIGHT);

            Label status = new Label(aviso.getEstado());
            if ("COMPLETADO".equalsIgnoreCase(aviso.getEstado())) {
                status.getStyleClass().add("status-pill-green");
            } else if ("PENDIENTE".equalsIgnoreCase(aviso.getEstado())) {
                status.getStyleClass().add("status-pill-orange");
            } else {
                status.getStyleClass().add("status-pill-blue");
            }

            // CALCULAR MINUTOS Y HORAS REALES
            long minutos = java.time.temporal.ChronoUnit.MINUTES.between(aviso.getFechaCreacion(), java.time.LocalDateTime.now());
            String tiempoTexto;
            if (minutos < 60) {
                tiempoTexto = "Hace " + Math.max(1, minutos) + " min";
            } else {
                long horas = minutos / 60;
                tiempoTexto = "Hace " + horas + (horas == 1 ? " hora" : " horas");
            }

            Label time = new Label(tiempoTexto);
            time.getStyleClass().add("time-text");

            rightBox.getChildren().addAll(status, time);

            row.getChildren().addAll(iconWrapper, midBox, rightBox);
            VBox.setMargin(row, new javafx.geometry.Insets(0, 0, 10, 0)); // Separación entre filas
            vboxActividadReciente.getChildren().add(row);
        }
    }

    private void calcularCrecimientoMensual(List<Aviso> avisos) {
        LocalDate hoy = LocalDate.now();
        int mesActual = hoy.getMonthValue();
        int anioActual = hoy.getYear();

        int mesAnterior = (mesActual == 1) ? 12 : mesActual - 1;
        int anioAnterior = (mesActual == 1) ? anioActual - 1 : anioActual;

        long totalMesActual = 0;
        long totalMesAnterior = 0;

        for (Aviso a : avisos) {
            if (a.getFechaCreacion() != null) {
                // Extraemos el día exacto de forma segura
                LocalDate fecha = a.getFechaCreacion().toLocalDate();

                if (fecha.getMonthValue() == mesActual && fecha.getYear() == anioActual) {
                    totalMesActual++;
                } else if (fecha.getMonthValue() == mesAnterior && fecha.getYear() == anioAnterior) {
                    totalMesAnterior++;
                }
            }
        }

        double porcentaje = 0.0;
        if (totalMesAnterior == 0) {
            if (totalMesActual > 0) porcentaje = 100.0;
        } else {
            porcentaje = ((double) (totalMesActual - totalMesAnterior) / totalMesAnterior) * 100.0;
        }

        // --- APLICAMOS LOS NUEVOS SVG MODERNOS ---
        if (porcentaje > 0) {
            // Sube (Verde)
            lblCrecimientoValor.setText(String.format("+%.1f%%", porcentaje));
            lblCrecimientoValor.setStyle("-fx-text-fill: #10B981;");
            lblCrecimientoIcon.setContent("M22 7L13.5 15.5 8.5 10.5 2 17 M16 7h6v6");
            lblCrecimientoIcon.setStyle("-fx-stroke: #10B981; -fx-fill: transparent; -fx-stroke-width: 2.5;");
        } else if (porcentaje < 0) {
            // Baja (Rojo)
            lblCrecimientoValor.setText(String.format("%.1f%%", porcentaje));
            lblCrecimientoValor.setStyle("-fx-text-fill: #EF4444;");
            lblCrecimientoIcon.setContent("M22 17L13.5 8.5 8.5 13.5 2 7 M16 17h6v-6");
            lblCrecimientoIcon.setStyle("-fx-stroke: #EF4444; -fx-fill: transparent; -fx-stroke-width: 2.5;");
        } else {
            // Se mantiene (Gris)
            lblCrecimientoValor.setText("0.0%");
            lblCrecimientoValor.setStyle("-fx-text-fill: #6B7280;");
            lblCrecimientoIcon.setContent("M5 12h14");
            lblCrecimientoIcon.setStyle("-fx-stroke: #6B7280; -fx-fill: transparent; -fx-stroke-width: 2.5;");
        }
    }
    public void setDatosUsuario(String nombreReal, String rolReal) {
        String nombreFormateado = nombreReal.substring(0, 1).toUpperCase() + nombreReal.substring(1);

        lblNombreUsuario.setText(nombreFormateado);
        lblRolUsuario.setText(rolReal);

        String iniciales = "";
        String[] partes = nombreFormateado.trim().split("[ ._]");
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
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.hide();
            stage.setMaximized(false);
            stage.setResizable(false);
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
            TecnicosController tecnicosController = loader.getController();
            tecnicosController.setDatosUsuario(lblNombreUsuario.getText(), lblRolUsuario.getText());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void irAClientes(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Clientes.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            ClientesController controller = loader.getController();
            controller.setDatosUsuario(lblNombreUsuario.getText(), lblRolUsuario.getText());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ESTE ES EL MÉTODO QUE CARGA EL GRÁFICO NUEVO DE LAS SEMANAS
    private void cargarGraficoSemanas() {
        LocalDate hoy = LocalDate.now().minusWeeks(semanasAtras);
        LocalDate lunes = hoy.with(java.time.DayOfWeek.MONDAY);
        LocalDate domingo = hoy.with(java.time.DayOfWeek.SUNDAY);

        java.time.format.DateTimeFormatter formateador = java.time.format.DateTimeFormatter.ofPattern("dd MMM");
        lblRangoSemana.setText(lunes.format(formateador) + " - " + domingo.format(formateador));

        btnSemanaSiguiente.setDisable(semanasAtras == 0);

        graficoActividad.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> completados = new javafx.scene.chart.XYChart.Series<>();
        completados.setName("Completados");
        javafx.scene.chart.XYChart.Series<String, Number> nuevos = new javafx.scene.chart.XYChart.Series<>();
        nuevos.setName("Nuevos (Pendientes)");

        List<Aviso> todosLosAvisos = avisoRepository.findAll();
        String[] diasSemana = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};

        for (int i = 0; i < 7; i++) {
            LocalDate diaEvaluado = lunes.plusDays(i);
            int countCompletados = 0;
            int countNuevos = 0;

            for (Aviso aviso : todosLosAvisos) {
                if (aviso.getFechaCreacion() != null) {
                    LocalDate fechaDelAviso = aviso.getFechaCreacion().toLocalDate();

                    if (fechaDelAviso.equals(diaEvaluado)) {
                        if ("COMPLETADO".equalsIgnoreCase(aviso.getEstado())) {
                            countCompletados++;
                        } else {
                            countNuevos++;
                        }
                    }
                }
            }

            completados.getData().add(new javafx.scene.chart.XYChart.Data<>(diasSemana[i], countCompletados));
            nuevos.getData().add(new javafx.scene.chart.XYChart.Data<>(diasSemana[i], countNuevos));
        }

        graficoActividad.getData().addAll(completados, nuevos);

        // --- 1. ARREGLO DEL BUG VISUAL ---
        // Desactivamos las animaciones por defecto de JavaFX que vuelven loco al gráfico al cambiar de semana
        graficoActividad.setAnimated(false);

        // --- 2. RESTAURAR LOS TOOLTIPS (Bocadillos) CON ESTILO ---
        Platform.runLater(() -> {
            for (XYChart.Series<String, Number> serie : graficoActividad.getData()) {
                for (XYChart.Data<String, Number> dato : serie.getData()) {
                    if (dato.getNode() != null) {

                        // Solo le ponemos el cartelito si hay 1 o más avisos (para no saturar si está a 0)
                        if (dato.getYValue().intValue() > 0) {
                            Tooltip tooltip = new Tooltip(dato.getXValue() + "\n" + serie.getName() + ": " + dato.getYValue());
                            tooltip.getStyleClass().add("chart-tooltip"); // Lo conectamos con el CSS nuevo
                            Tooltip.install(dato.getNode(), tooltip);

                            // BONUS PROFESIONAL: Que la barra cambie un poco de opacidad al pasar el ratón
                            dato.getNode().setOnMouseEntered(e -> dato.getNode().setStyle("-fx-opacity: 0.7; -fx-cursor: hand;"));
                            dato.getNode().setOnMouseExited(e -> dato.getNode().setStyle("-fx-opacity: 1.0;"));
                        }

                    }
                }
            }
        });
    }
}