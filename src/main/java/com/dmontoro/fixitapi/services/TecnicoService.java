package com.dmontoro.fixitapi.services;

import com.dmontoro.fixitapi.models.Tecnico;
import com.dmontoro.fixitapi.repositories.TecnicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Clase de servicio que centraliza la gestion de los empleados de la empresa.
 * Actua como un puente de confianza entre los controladores y la base de datos para asegurar
 * que la informacion de los tecnicos se maneje de forma correcta y segura.
 */
@Service
public class TecnicoService {

    @Autowired
    private TecnicoRepository tecnicoRepository;

    // Obtener todos los técnicos para que el administrador los gestione
    /**
     * Recupera la lista completa de todos los trabajadores registrados en la plantilla.
     * @return Un listado con todos los tecnicos guardados en el sistema.
     */
    public List<Tecnico> getAllTecnicos() {
        return tecnicoRepository.findAll();
    }

    // Buscar un técnico por su ID
    /**
     * Localiza la ficha personal de un trabajador especifico mediante su numero de identificacion.
     * @param id El numero unico que identifica al empleado.
     * @return Un objeto opcional con los datos del tecnico si se encuentra en el registro.
     */
    public Optional<Tecnico> getTecnicoById(Long id) {
        return tecnicoRepository.findById(id);
    }

    // Guardar o actualizar un técnico
    /**
     * Registra a un nuevo trabajador en el equipo o actualiza los datos de uno que ya existe.
     * @param tecnico El objeto con el nombre, correo y especialidades del empleado.
     * @return El tecnico guardado con su numero de identificador oficial.
     */
    public Tecnico saveTecnico(Tecnico tecnico) {
        return tecnicoRepository.save(tecnico);
    }

    // Eliminar un técnico
    /**
     * Borra de forma definitiva la ficha de un empleado del sistema de gestion.
     * @param id El numero identificador del tecnico que se desea eliminar.
     */
    public void deleteTecnico(Long id) {
        tecnicoRepository.deleteById(id);
    }
}