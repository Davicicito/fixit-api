package com.dmontoro.fixitapi.services;

import com.dmontoro.fixitapi.models.Tecnico;
import com.dmontoro.fixitapi.repositories.TecnicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service //
public class TecnicoService {

    @Autowired //
    private TecnicoRepository tecnicoRepository;

    // Obtener todos los técnicos para que el administrador los gestione
    public List<Tecnico> getAllTecnicos() {
        return tecnicoRepository.findAll();
    }

    // Buscar un técnico por su ID
    public Optional<Tecnico> getTecnicoById(Long id) {
        return tecnicoRepository.findById(id);
    }

    // Guardar o actualizar un técnico (Aquí se define el rol al crearlo)
    public Tecnico saveTecnico(Tecnico tecnico) {
        return tecnicoRepository.save(tecnico);
    }

    // Eliminar un técnico
    public void deleteTecnico(Long id) {
        tecnicoRepository.deleteById(id);
    }
}