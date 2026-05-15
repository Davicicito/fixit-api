package com.dmontoro.fixitapi.repositories;

import com.dmontoro.fixitapi.models.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interfaz encargada de la comunicacion con la tabla de materiales en la base de datos MySQL.
 * Proporciona las herramientas necesarias para que el sistema pueda consultar las existencias del almacen,
 * actualizar los niveles de stock y dar de alta nuevas piezas o repuestos.
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

}