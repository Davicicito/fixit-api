package com.dmontoro.fixitapi.repositories;

import com.dmontoro.fixitapi.models.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    // Consulta extra para el login: buscar técnico por email
    Optional<Tecnico> findByEmail(String email);
}