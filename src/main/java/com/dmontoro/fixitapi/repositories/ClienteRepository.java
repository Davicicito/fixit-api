package com.dmontoro.fixitapi.repositories;

import com.dmontoro.fixitapi.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interfaz que actua como el motor de gestion para la tabla de clientes en la base de datos.
 * Gracias a esta clase el programa puede realizar todas las operaciones basicas de guardado y consulta
 * de las fichas de los clientes de manera automatica y eficiente.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

}