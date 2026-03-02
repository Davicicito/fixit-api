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
@CrossOrigin // Clave para que tu app móvil y web no tengan bloqueos
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
        Optional<Aviso> avisoOptional = avisoService.getAvisoById(id);
        if (avisoOptional.isPresent()) {
            Aviso aviso = avisoOptional.get();
            aviso.setDescripcion(avisoDetails.getDescripcion());
            aviso.setEstado(avisoDetails.getEstado());
            aviso.setFotoAveria(avisoDetails.getFotoAveria());
            aviso.setFirmaCliente(avisoDetails.getFirmaCliente());
            aviso.setTecnico(avisoDetails.getTecnico());
            aviso.setCliente(avisoDetails.getCliente());
            aviso.setCategoria(avisoDetails.getCategoria());
            return ResponseEntity.ok(avisoService.saveAviso(aviso));
        } else {
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
    public ResponseEntity<AvisoMaterial> addMaterialAAviso(@PathVariable Long id, @RequestBody AvisoMaterial avisoMaterial) {
        // Enlazamos automáticamente el material con el ID del aviso de la URL
        Aviso aviso = new Aviso();
        aviso.setId(id);
        avisoMaterial.setAviso(aviso);

        return ResponseEntity.status(HttpStatus.CREATED).body(avisoService.añadirMaterialAAviso(avisoMaterial));
    }

    // GET http://localhost:8080/avisos/1/materiales
    // Sirve para que el jefe vea el desglose de materiales de un parte de trabajo
    @GetMapping("/{id}/materiales")
    public ResponseEntity<List<AvisoMaterial>> getMaterialesDeUnAviso(@PathVariable Long id) {
        return ResponseEntity.ok(avisoService.getMaterialesDeUnAviso(id));
    }
}