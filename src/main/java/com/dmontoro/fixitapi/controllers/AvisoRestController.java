package com.dmontoro.fixitapi.controllers;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.repositories.AvisoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avisos")
public class AvisoRestController {

    @Autowired
    private AvisoRepository avisoRepository;

    @GetMapping("/tecnico/{idTecnico}")
    public ResponseEntity<List<Aviso>> getAvisosPorTecnico(@PathVariable Long idTecnico) {

        List<Aviso> avisosDelTecnico = avisoRepository.findAll().stream()
                // 1. Nos quedamos solo con los de este técnico
                .filter(aviso -> aviso.getTecnico() != null && aviso.getTecnico().getId().equals(idTecnico))
                // 2. Si no es "COMPLETADO", o si el estado está vacío (null), que pase.
                .filter(aviso -> aviso.getEstado() == null || !aviso.getEstado().equalsIgnoreCase("COMPLETADO"))
                .toList();

        return ResponseEntity.ok(avisosDelTecnico);
    }
}