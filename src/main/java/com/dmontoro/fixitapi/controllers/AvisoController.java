package com.dmontoro.fixitapi.controllers;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.models.AvisoMaterial;
import com.dmontoro.fixitapi.services.AvisoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/avisos")
@CrossOrigin
public class AvisoController {

    @Autowired
    private AvisoService avisoService;

    // ==========================================
    // 1. CRUD BÁSICO DE AVISOS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Aviso>> getAllAvisos() {
        return new ResponseEntity<>(avisoService.getAllAvisos(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Aviso> getAvisoById(@PathVariable Long id) {
        Optional<Aviso> aviso = avisoService.getAvisoById(id);
        return aviso.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<Aviso> createAviso(@RequestBody Aviso aviso) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avisoService.saveAviso(aviso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Aviso> updateAviso(@PathVariable Long id, @RequestBody Aviso avisoDetails) {
        try {
            return ResponseEntity.ok(avisoService.actualizarAviso(id, avisoDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAviso(@PathVariable Long id) {
        if (avisoService.getAvisoById(id).isPresent()) {
            avisoService.deleteAviso(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // ==========================================
    // 2. CONSULTAS AVANZADAS (LOS JOIN DEL PDF)
    // ==========================================

    @GetMapping("/tecnico/{tecnicoId}")
    public ResponseEntity<List<Aviso>> getAvisosByTecnico(@PathVariable Long tecnicoId) {
        return ResponseEntity.ok(avisoService.getAvisosPorTecnico(tecnicoId));
    }

    @GetMapping("/material/{material}")
    public ResponseEntity<List<Aviso>> getAvisosByMaterial(@PathVariable String material) {
        return ResponseEntity.ok(avisoService.getAvisosPorMaterial(material));
    }

    @GetMapping("/pendientes/{categoria}")
    public ResponseEntity<List<Aviso>> getPendientesByCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(avisoService.getAvisosPendientesPorCategoria(categoria));
    }

    // ==========================================
    // 3. GESTIÓN DE LA TABLA INTERMEDIA (N:M)
    // ==========================================

    // POST http://localhost:8080/avisos/1/materiales
    // Sirve para añadir un material a un aviso concreto indicando su cantidad
    @PostMapping("/{id}/materiales")
    public ResponseEntity<?> addMaterialAAviso(@PathVariable Long id, @RequestBody AvisoMaterial avisoMaterial) {
        try {
            // Le pasamos la pelota al servicio
            AvisoMaterial nuevoMaterial = avisoService.añadirMaterialAAviso(id, avisoMaterial);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMaterial);
        } catch (RuntimeException e) {
            // Si el servicio detecta un error (cantidades negativas, no existe el aviso...), devuelve un 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // GET http://localhost:8080/avisos/1/materiales
    // Sirve para que el jefe vea el desglose de materiales de un parte de trabajo
    @GetMapping("/{id}/materiales")
    public ResponseEntity<List<AvisoMaterial>> getMaterialesDeUnAviso(@PathVariable Long id) {
        return ResponseEntity.ok(avisoService.getMaterialesDeUnAviso(id));
    }
}