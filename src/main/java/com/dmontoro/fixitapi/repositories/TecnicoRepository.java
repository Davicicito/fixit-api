package com.dmontoro.fixitapi.repositories;

import com.dmontoro.fixitapi.models.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Interfaz que gestiona la comunicacion con la tabla de tecnicos en la base de datos.
 * Ademas de las operaciones tipicas de guardado y borrado, permite realizar busquedas
 * especificas para los procesos de seguridad y acceso al sistema.
 */
@Repository
public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    // Consulta extra para el login: buscar técnico por email
    /**
     * Busca en el registro de empleados a un trabajador utilizando su direccion de correo electronico.
     * Esta funcion es vital para el proceso de inicio de sesion tanto en el movil como en el ordenador.
     *
     * @param email La direccion de correo que el usuario escribe al intentar acceder.
     * @return Un objeto opcional que contendra los datos del tecnico si el correo existe en el sistema.
     */
    Optional<Tecnico> findByEmail(String email);
}