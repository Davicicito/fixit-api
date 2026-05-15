package com.dmontoro.fixitapi.repositories;

import com.dmontoro.fixitapi.models.AvisoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Interfaz que gestiona el acceso a la tabla intermedia donde se guardan los materiales de cada trabajo.
 * Gracias a este repositorio podemos realizar consultas a la base de datos de forma automatica
 * sin tener que escribir codigo SQL complejo.
 */
@Repository
public interface AvisoMaterialRepository extends JpaRepository<AvisoMaterial, Long> {

    // Para ver todos los materiales de un aviso concreto
    /**
     * Busca en la base de datos y devuelve todos los registros de materiales que pertenecen
     * a un mismo parte de trabajo.
     *
     * @param avisoId El numero identificador del trabajo que queremos consultar.
     * @return Una lista con todos los materiales vinculados a ese aviso especifico.
     */
    List<AvisoMaterial> findByAvisoId(Long avisoId);
}