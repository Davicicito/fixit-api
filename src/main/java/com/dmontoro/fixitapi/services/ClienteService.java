package com.dmontoro.fixitapi.services;

import com.dmontoro.fixitapi.models.Cliente;
import com.dmontoro.fixitapi.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> getClienteById(Long id) {
        return clienteRepository.findById(id);
    }

    public Cliente saveCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void deleteCliente(Long id) {
        clienteRepository.deleteById(id);
    }
    public Cliente actualizarCliente(Long id, Cliente clienteDetails) {
        // 1. Comprobamos si existe
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente con ID " + id + " no encontrado"));

        // 2. Actualizamos los datos (Lógica de negocio en la capa correcta)
        clienteExistente.setNombre(clienteDetails.getNombre());
        clienteExistente.setDireccion(clienteDetails.getDireccion());
        clienteExistente.setTelefono(clienteDetails.getTelefono());

        // 3. Guardamos
        return clienteRepository.save(clienteExistente);
    }
}