package com.dmontoro.fixitapi.repositories;

import com.dmontoro.fixitapi.models.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    // Hereda findAll(), save(), deleteById(), etc.
}