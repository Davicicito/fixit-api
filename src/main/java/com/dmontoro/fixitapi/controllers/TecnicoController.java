package com.dmontoro.fixitapi.controllers;

import com.dmontoro.fixitapi.models.Tecnico;
import com.dmontoro.fixitapi.services.TecnicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController // Indica que esta clase responderá a peticiones HTTP devolviendo JSON
@RequestMapping("/tecnicos") // La ruta base será localhost:8080/tecnicos
@CrossOrigin // VITAL: Permite que tu frontend (móvil o web) se conecte sin bloqueos de seguridad CORS
public class TecnicoController {

    @Autowired
    private TecnicoService tecnicoService;

    // 1. Obtener todos los técnicos (READ ALL)
    @GetMapping
    public ResponseEntity<List<Tecnico>> getAllTecnicos() {
        List<Tecnico> tecnicos = tecnicoService.getAllTecnicos();
        return new ResponseEntity<>(tecnicos, HttpStatus.OK); // Devuelve 200 OK
    }

    // 2. Obtener un técnico por ID (READ BY ID)
    @GetMapping("/{id}")
    public ResponseEntity<Tecnico> getTecnicoById(@PathVariable Long id) {
        Optional<Tecnico> tecnico = tecnicoService.getTecnicoById(id);
        if (tecnico.isPresent()) {
            return ResponseEntity.ok(tecnico.get()); // Devuelve 200 OK con los datos
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Devuelve 404 si no existe
        }
    }

    // 3. Crear un nuevo técnico (CREATE)
    @PostMapping
    public ResponseEntity<Tecnico> createTecnico(@RequestBody Tecnico tecnico) {
        Tecnico nuevoTecnico = tecnicoService.saveTecnico(tecnico);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTecnico); // Devuelve 201 Created
    }

    // 4. Actualizar un técnico (UPDATE)
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
            return ResponseEntity.ok(tecnicoActualizado); // Devuelve 200 OK
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Devuelve 404 si no existe el ID
        }
    }

    // 5. Eliminar un técnico (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTecnico(@PathVariable Long id) {
        Optional<Tecnico> tecnico = tecnicoService.getTecnicoById(id);
        if (tecnico.isPresent()) {
            tecnicoService.deleteTecnico(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // Devuelve 204 No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Devuelve 404
        }
    }
}