package com.dmontoro.fixitapi.controllers;

import com.dmontoro.fixitapi.models.Cliente;
import com.dmontoro.fixitapi.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de la API encargado de gestionar a los clientes.
 * Sirve como punto de acceso para que el programa del jefe o la aplicacion movil
 * puedan leer, crear, modificar o borrar la informacion de las personas y empresas que contratan servicios.
 */
@RestController
@RequestMapping("/clientes")
@CrossOrigin
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    /**
     * Pide a la base de datos la lista completa de todos los clientes registrados en la empresa
     * y la devuelve al instante para mostrarla en el directorio principal.
     *
     * @return La lista con todas las fichas de los clientes.
     */
    @GetMapping
    public ResponseEntity<List<Cliente>> getAllClientes() {
        return new ResponseEntity<>(clienteService.getAllClientes(), HttpStatus.OK);
    }

    /**
     * Busca la ficha de un cliente especifico utilizando su numero identificador unico.
     * Si lo encuentra devuelve toda su informacion y si no existe avisa de que no hay resultados.
     *
     * @param id El numero de identificacion del cliente que queremos buscar.
     * @return La informacion detallada de ese cliente.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
        Optional<Cliente> cliente = clienteService.getClienteById(id);
        return cliente.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Recibe los datos rellenados en el formulario de nuevo cliente y los guarda de forma segura
     * en la base de datos para poder asignarle averias en el futuro.
     *
     * @param cliente El objeto que contiene el nombre, direccion y contacto del nuevo cliente.
     * @return El cliente recien creado confirmando que el guardado ha sido un exito.
     */
    @PostMapping
    public ResponseEntity<Cliente> createCliente(@RequestBody Cliente cliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.saveCliente(cliente));
    }

    /**
     * Recibe la informacion modificada de un cliente que ya existia y sobrescribe sus datos antiguos.
     * Ideal para cuando una persona cambia de numero de telefono, de direccion o actualiza su correo.
     *
     * @param id El identificador del cliente que se va a modificar.
     * @param clienteDetails La informacion nueva que va a sustituir a la antigua.
     * @return La ficha del cliente ya actualizada y guardada en el sistema.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> updateCliente(@PathVariable Long id, @RequestBody Cliente clienteDetails) {
        try {
            return ResponseEntity.ok(clienteService.actualizarCliente(id, clienteDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Busca a un cliente por su numero identificador y lo borra del sistema.
     * Si intentan borrar a alguien que no existe responde de forma segura avisando del fallo.
     *
     * @param id El numero del cliente que queremos eliminar de la agenda.
     * @return Una respuesta vacia indicando que el borrado se ha realizado correctamente.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        if (clienteService.getClienteById(id).isPresent()) {
            clienteService.deleteCliente(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}