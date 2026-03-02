package com.dmontoro.fixitapi.repositories;

import com.dmontoro.fixitapi.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // Métodos CRUD básicos heredados automáticamente
}