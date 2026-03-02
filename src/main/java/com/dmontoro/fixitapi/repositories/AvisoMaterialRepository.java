package com.dmontoro.fixitapi.repositories;

import com.dmontoro.fixitapi.models.AvisoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvisoMaterialRepository extends JpaRepository<AvisoMaterial, Long> {

    // Para ver todos los materiales de un aviso concreto
    List<AvisoMaterial> findByAvisoId(Long avisoId);
}