package com.dmontoro.fixitapi.controllers;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.repositories.AvisoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de la API diseñado exclusivamente para la aplicacion movil.
 * Actua como un recepcionista que atiende las peticiones de los telefonos de los trabajadores
 * y les manda la informacion de sus averias correspondientes.
 */
@RestController
@RequestMapping("/api/avisos")
public class AvisoRestController {

    @Autowired
    private AvisoRepository avisoRepository;

    /**
     * Recibe el numero de identificacion de un empleado y busca en la base de datos todos los trabajos que tiene asignados.
     * Para que el trabajador no tenga una lista infinita en su movil el sistema filtra las averias y le manda solamente
     * las que estan pendientes o en progreso dejando fuera las que ya han sido solucionadas.
     *
     * @param idTecnico El numero unico que identifica al trabajador que esta usando la aplicacion movil.
     * @return La lista de tareas sin terminar preparadas para mostrarse en la pantalla del telefono.
     */
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