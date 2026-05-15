package com.dmontoro.fixitapi.controllers;

import com.dmontoro.fixitapi.models.Tecnico;
import com.dmontoro.fixitapi.services.TecnicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de la API encargado de gestionar la informacion de los tecnicos.
 * Permite realizar todas las operaciones de consulta, registro, modificacion y borrado
 * de los empleados que forman parte de la plantilla.
 */
@RestController
@RequestMapping("/tecnicos")
@CrossOrigin
public class TecnicoController {

    @Autowired
    private TecnicoService tecnicoService;

    // 1. Obtener todos los técnicos
    /**
     * Solicita al sistema la lista completa de todos los trabajadores registrados.
     * Devuelve la informacion necesaria para que el administrador pueda ver a todo el equipo.
     *
     * @return Una respuesta con la lista de todos los empleados y el codigo de exito.
     */
    @GetMapping
    public ResponseEntity<List<Tecnico>> getAllTecnicos() {
        List<Tecnico> tecnicos = tecnicoService.getAllTecnicos();
        return new ResponseEntity<>(tecnicos, HttpStatus.OK); // Devuelve 200 OK
    }

    // 2. Obtener un técnico por ID
    /**
     * Busca a un trabajador especifico mediante su numero de identificacion.
     * Si lo encuentra entrega su ficha personal completa y si no devuelve una señal de que no existe.
     *
     * @param id El numero identificador del tecnico.
     * @return La informacion del trabajador si se localiza en la base de datos.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tecnico> getTecnicoById(@PathVariable Long id) {
        Optional<Tecnico> tecnico = tecnicoService.getTecnicoById(id);
        if (tecnico.isPresent()) {
            return ResponseEntity.ok(tecnico.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 3. Crear un nuevo técnico
    /**
     * Recibe los datos de un nuevo empleado y los guarda en el sistema de forma definitiva.
     *
     * @param tecnico El objeto con el nombre, correo y demas datos del nuevo trabajador.
     * @return El perfil del tecnico recien creado.
     */
    @PostMapping
    public ResponseEntity<Tecnico> createTecnico(@RequestBody Tecnico tecnico) {
        Tecnico nuevoTecnico = tecnicoService.saveTecnico(tecnico);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTecnico);
    }

    // 4. Actualizar un técnico
    /**
     * Modifica los datos de un empleado que ya estaba registrado.
     * Actualiza campos como el nombre, el correo o la especialidad tecnica y guarda los cambios.
     *
     * @param id El identificador del tecnico que se desea actualizar.
     * @param tecnicoDetails La informacion nueva que se va a guardar.
     * @return La ficha del trabajador ya actualizada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tecnico> updateTecnico(@PathVariable Long id, @RequestBody Tecnico tecnicoDetails) {
        Optional<Tecnico> tecnicoOptional = tecnicoService.getTecnicoById(id);

        if (tecnicoOptional.isPresent()) {
            Tecnico tecnicoExistente = tecnicoOptional.get();

            // Actualizamos los campos
            tecnicoExistente.setNombre(tecnicoDetails.getNombre());
            tecnicoExistente.setEmail(tecnicoDetails.getEmail());
            tecnicoExistente.setPassword(tecnicoDetails.getPassword());
            tecnicoExistente.setEspecialidad(tecnicoDetails.getEspecialidad());
            tecnicoExistente.setRol(tecnicoDetails.getRol());

            Tecnico tecnicoActualizado = tecnicoService.saveTecnico(tecnicoExistente);
            return ResponseEntity.ok(tecnicoActualizado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 5. Eliminar un técnico
    /**
     * Elimina a un trabajador de la base de datos utilizando su numero de identificador.
     * Si el tecnico no se encuentra en el sistema responde avisando del problema.
     *
     * @param id El numero identificador del empleado a borrar.
     * @return Una respuesta indicando que la operacion se ha completado.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTecnico(@PathVariable Long id) {
        Optional<Tecnico> tecnico = tecnicoService.getTecnicoById(id);
        if (tecnico.isPresent()) {
            tecnicoService.deleteTecnico(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}