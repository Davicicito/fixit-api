package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.repositories.AvisoRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Controller
public class DashboardController implements Initializable {

    @FXML private Label lblPendientes;
    @FXML private Label lblEnProgreso;
    @FXML private Label lblCompletados;
    @FXML private Label lblUrgentes;

    @FXML private BarChart<String, Number> barChartActividad;
    @FXML private PieChart pieChartCategoria;
    @FXML private LineChart<String, Number> lineChartTendencia;

    @Autowired
    private AvisoRepository avisoRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<Aviso> avisosReales = avisoRepository.findAll();

        cargarKpisReales(avisosReales);
        cargarGraficoCircularReal(avisosReales);

        // Gráficos de fechas 100% reales
        cargarGraficoBarrasSemanalReal(avisosReales);
        cargarGraficoLineasMensualReal(avisosReales);
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

        // --- SOLUCIÓN: ESPERAR A QUE SE DIBUJEN PARA PONER EL TOOLTIP ---
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

        // --- SOLUCIÓN: ESPERAR A QUE SE DIBUJEN PARA PONER EL TOOLTIP ---
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> dato : seriesTendencia.getData()) {
                if (dato.getNode() != null && dato.getYValue().intValue() > 0) {
                    Tooltip tooltip = new Tooltip("Mes de " + dato.getXValue() + "\nTotal: " + dato.getYValue() + " avisos");
                    Tooltip.install(dato.getNode(), tooltip);
                }
            }
        });
    }
}