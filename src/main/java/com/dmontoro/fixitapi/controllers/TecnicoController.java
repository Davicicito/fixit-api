package com.dmontoro.fixitapi.controllers;

import com.dmontoro.fixitapi.models.Tecnico;
import com.dmontoro.fixitapi.services.TecnicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tecnicos")
@CrossOrigin
public class TecnicoController {

    @Autowired
    private TecnicoService tecnicoService;

    // 1. Obtener todos los técnicos
    @GetMapping
    public ResponseEntity<List<Tecnico>> getAllTecnicos() {
        List<Tecnico> tecnicos = tecnicoService.getAllTecnicos();
        return new ResponseEntity<>(tecnicos, HttpStatus.OK); // Devuelve 200 OK
    }

    // 2. Obtener un técnico por ID
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
    @PostMapping
    public ResponseEntity<Tecnico> createTecnico(@RequestBody Tecnico tecnico) {
        Tecnico nuevoTecnico = tecnicoService.saveTecnico(tecnico);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTecnico);
    }

    // 4. Actualizar un técnico
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