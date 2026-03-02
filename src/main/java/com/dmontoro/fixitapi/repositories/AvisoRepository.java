package com.dmontoro.fixitapi.repositories;

import com.dmontoro.fixitapi.models.Aviso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    // 1. PARA LA APP MÓVIL: Saca todos los avisos de un técnico con los datos del cliente y la categoría de golpe.
    @Query("SELECT a FROM Aviso a " +
            "JOIN a.tecnico t " +
            "JOIN a.cliente c " +
            "JOIN a.categoria cat " +
            "WHERE t.id = :tecnicoId")
    List<Aviso> findAvisosDetalladosPorTecnico(@Param("tecnicoId") Long tecnicoId);

    // 2. PARA EL PC DEL JEFE (Nivel Experto): Busca qué avisos han gastado un material concreto (ej: "Tubería PVC").
    // Fíjate que cruza 3 tablas: Aviso -> AvisoMaterial -> Material
    @Query("SELECT a FROM Aviso a " +
            "JOIN a.avisoMateriales am " +
            "JOIN am.material m " +
            "WHERE m.nombre LIKE %:nombreMaterial%")
    List<Aviso> findAvisosPorUsoDeMaterial(@Param("nombreMaterial") String nombreMaterial);

    // 3. PARA EL PC DEL JEFE: Filtra los avisos que están "PENDIENTES" de una categoría específica (ej: "Electricidad").
    @Query("SELECT a FROM Aviso a " +
            "JOIN a.categoria c " +
            "WHERE a.estado = 'PENDIENTE' AND c.nombre = :nombreCategoria")
    List<Aviso> findAvisosPendientesPorCategoria(@Param("nombreCategoria") String nombreCategoria);
}