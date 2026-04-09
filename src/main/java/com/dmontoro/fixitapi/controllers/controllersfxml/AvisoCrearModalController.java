package com.dmontoro.fixitapi.controllers.controllersfxml;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.models.Categoria;
import com.dmontoro.fixitapi.models.Cliente;
import com.dmontoro.fixitapi.models.Tecnico;
import com.dmontoro.fixitapi.repositories.CategoriaRepository;
import com.dmontoro.fixitapi.repositories.ClienteRepository;
import com.dmontoro.fixitapi.repositories.TecnicoRepository;
import com.dmontoro.fixitapi.services.AvisoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Controller
public class AvisoCrearModalController implements Initializable {

    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private ComboBox<Tecnico> comboTecnico;
    @FXML private ComboBox<Categoria> comboCategoria;
    @FXML private ComboBox<String> comboPrioridad;
    @FXML private TextArea txtDescripcion;

    @Autowired private AvisoService avisoService;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private TecnicoRepository tecnicoRepository;
    @Autowired private CategoriaRepository categoriaRepository;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarDesplegables();
    }

    private void cargarDesplegables() {
        // 1. Cargar Prioridades (Fijas)
        comboPrioridad.setItems(FXCollections.observableArrayList("ALTA", "MEDIA", "BAJA"));
        comboPrioridad.setValue("MEDIA");

        // 2. Cargar Clientes desde MySQL
        comboCliente.setItems(FXCollections.observableArrayList(clienteRepository.findAll()));
        comboCliente.setConverter(new StringConverter<Cliente>() {
            @Override public String toString(Cliente c) { return c != null ? c.getNombre() : ""; }
            @Override public Cliente fromString(String s) { return null; }
        });

        // 3. Cargar Técnicos desde MySQL (¡FILTRANDO AL JEFE!)
        List<Tecnico> todosLosUsuarios = tecnicoRepository.findAll();

        // Usamos Java Streams para quedarnos SOLO con los que tienen el rol de Técnico
        List<Tecnico> soloTecnicos = todosLosUsuarios.stream()
                .filter(t -> t.getRol() != null && !t.getRol().equalsIgnoreCase("Administrador"))
                // Nota: Si en tu BD el jefe se llama "Jefe", cambia "Administrador" por "Jefe"
                .collect(Collectors.toList());

        comboTecnico.setItems(FXCollections.observableArrayList(soloTecnicos));
        comboTecnico.setConverter(new StringConverter<Tecnico>() {
            @Override public String toString(Tecnico t) { return t != null ? t.getNombre() : ""; }
            @Override public Tecnico fromString(String s) { return null; }
        });

        // 4. Cargar Categorías desde MySQL
        comboCategoria.setItems(FXCollections.observableArrayList(categoriaRepository.findAll()));
        comboCategoria.setConverter(new StringConverter<Categoria>() {
            @Override public String toString(Categoria c) { return c != null ? c.getNombre() : ""; }
            @Override public Categoria fromString(String s) { return null; }
        });
    }

    @FXML
    public void crearAviso() {
        // Creamos un aviso vacío y lo rellenamos con lo que hay en la pantalla
        Aviso nuevoAviso = new Aviso();

        nuevoAviso.setCliente(comboCliente.getValue());
        nuevoAviso.setTecnico(comboTecnico.getValue());
        nuevoAviso.setCategoria(comboCategoria.getValue());
        nuevoAviso.setPrioridad(comboPrioridad.getValue());
        nuevoAviso.setDescripcion(txtDescripcion.getText());
        // Recuerda que el Estado se pone en "PENDIENTE" automáticamente en tu Service

        // Guardamos en la base de datos
        avisoService.saveAviso(nuevoAviso);

        // Cerramos la ventana
        cerrarModal();
    }

    @FXML
    public void cerrarModal() {
        Stage stage = (Stage) txtDescripcion.getScene().getWindow();
        stage.close();
    }
}