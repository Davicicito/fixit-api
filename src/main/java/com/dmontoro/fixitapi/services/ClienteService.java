package com.dmontoro.fixitapi.services;

import com.dmontoro.fixitapi.models.Cliente;
import com.dmontoro.fixitapi.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Clase de servicio que gestiona toda la logica relacionada con los clientes de la empresa.
 * Centraliza las operaciones de registro y consulta de las fichas de los usuarios para que
 * los controladores no tengan que hablar directamente con la base de datos.
 */
@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Recupera la lista completa de todos los clientes que han contratado algun servicio con la empresa.
     * @return Un listado con todas las fichas de clientes disponibles.
     */
    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

    /**
     * Busca la informacion detallada de un cliente concreto utilizando su numero identificador.
     * @param id El numero de identificacion del cliente.
     * @return Un objeto opcional que contiene los datos del cliente si se encuentra registrado.
     */
    public Optional<Cliente> getClienteById(Long id) {
        return clienteRepository.findById(id);
    }

    /**
     * Guarda un cliente nuevo en el sistema o registra los cambios basicos de una ficha.
     * @param cliente El objeto con la informacion de contacto del cliente.
     * @return El cliente guardado con su identificador oficial.
     */
    public Cliente saveCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    /**
     * Elimina definitivamente la ficha de un cliente del sistema de gestion.
     * @param id El numero identificador del cliente que se desea borrar.
     */
    public void deleteCliente(Long id) {
        clienteRepository.deleteById(id);
    }

    /**
     * Realiza una actualizacion controlada de los datos de un cliente existente.
     * Antes de guardar los cambios verifica que el cliente realmente exista en el sistema
     * para evitar errores en la base de datos.
     *
     * @param id El numero identificador del cliente que se quiere modificar.
     * @param clienteDetails El paquete con los nuevos datos de nombre, direccion y telefono.
     * @return La ficha del cliente ya actualizada y guardada.
     */
    public Cliente actualizarCliente(Long id, Cliente clienteDetails) {
        // 1. Comprobamos si existe
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente con ID " + id + " no encontrado"));

        // 2. Actualizamos los datos
        clienteExistente.setNombre(clienteDetails.getNombre());
        clienteExistente.setDireccion(clienteDetails.getDireccion());
        clienteExistente.setTelefono(clienteDetails.getTelefono());

        // 3. Guardamos
        return clienteRepository.save(clienteExistente);
    }
}