package com.dmontoro.fixitapi.controllers;

import com.dmontoro.fixitapi.models.LoginRequest;
import com.dmontoro.fixitapi.models.Tecnico;
import com.dmontoro.fixitapi.repositories.TecnicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Controlador exclusivo para gestionar la entrada desde la aplicacion movil.
 * Funciona como un vigilante de seguridad que pide el correo y la clave al tecnico
 * antes de dejarle pasar a ver sus trabajos asignados.
 */
@RestController
@RequestMapping("/api/auth")
public class LoginRestController {

    @Autowired
    private TecnicoRepository tecnicoRepository;

    // Este método escucha cuando el móvil hace un POST a /api/auth/login
    /**
     * Recibe la peticion del telefono movil cuando un empleado intenta iniciar sesion.
     * Busca en el registro de la empresa si existe un trabajador con ese correo y si su clave es correcta.
     * Si acierta le envia toda su ficha personal para que la aplicacion arranque de forma personalizada,
     * y si falla le bloquea la puerta devolviendo un error de seguridad.
     *
     * @param request El paquete con el correo y la contraseña que el empleado ha escrito en su pantalla.
     * @return El perfil completo del trabajador si acierta o un texto de aviso si se ha equivocado.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // Buscamos en la base de datos si coincide el email y la contraseña
        Optional<Tecnico> tecnicoOpt = tecnicoRepository.findAll().stream()
                .filter(t -> t.getEmail() != null && t.getEmail().equalsIgnoreCase(request.getEmail())
                        && t.getPassword() != null && t.getPassword().equals(request.getPassword()))
                .findFirst();

        if (tecnicoOpt.isPresent()) {
            // Si existe y la contraseña está bien, devolvemos el técnico entero al móvil
            return ResponseEntity.ok(tecnicoOpt.get());
        } else {
            // Si falla, devolvemos un error 401 (No autorizado)
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }
    }
}