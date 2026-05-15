package com.dmontoro.fixitapi.repositories;

import com.dmontoro.fixitapi.models.Aviso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interfaz encargada de realizar las operaciones de lectura y escritura en la tabla de avisos.
 * Ademas de las funciones basicas, incluye consultas personalizadas para obtener detalles complejos
 * mezclando datos de clientes, tecnicos y materiales.
 */
@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    // 1. Saca todos los avisos de un técnico con los datos del cliente y la categoría.
    /**
     * Realiza una consulta a la base de datos para obtener todos los trabajos asignados a un empleado.
     * Cruza la informacion con las tablas de clientes y categorias para traer el parte de trabajo completo.
     *
     * @param tecnicoId Numero identificador del trabajador.
     * @return Una lista de avisos con toda la informacion detallada.
     */
    @Query("SELECT a FROM Aviso a " +
            "JOIN a.tecnico t " +
            "JOIN a.cliente c " +
            "JOIN a.categoria cat " +
            "WHERE t.id = :tecnicoId")
    List<Aviso> findAvisosDetalladosPorTecnico(@Param("tecnicoId") Long tecnicoId);

    // 2. Busca qué avisos han gastado un material concreto.
    /**
     * Busca en el historial de reparaciones cualquier aviso donde se haya utilizado un material especifico.
     * Es muy util para rastrear el uso de piezas por su nombre.
     *
     * @param nombreMaterial El nombre o parte del nombre del producto que queremos rastrear.
     * @return El listado de avisos donde aparece registrado ese material.
     */
    @Query("SELECT a FROM Aviso a " +
            "JOIN a.avisoMateriales am " +
            "JOIN am.material m " +
            "WHERE m.nombre LIKE %:nombreMaterial%")
    List<Aviso> findAvisosPorUsoDeMaterial(@Param("nombreMaterial") String nombreMaterial);

    // 3. Filtra los avisos que están "PENDIENTES" de una categoría específica.
    /**
     * Obtiene una lista de trabajos que todavia no se han empezado y que pertenecen a una rama tecnica concreta.
     *
     * @param nombreCategoria El nombre de la especialidad como por ejemplo fontaneria.
     * @return Los avisos pendientes de esa categoria.
     */
    @Query("SELECT a FROM Aviso a " +
            "JOIN a.categoria c " +
            "WHERE a.estado = 'PENDIENTE' AND c.nombre = :nombreCategoria")
    List<Aviso> findAvisosPendientesPorCategoria(@Param("nombreCategoria") String nombreCategoria);
}