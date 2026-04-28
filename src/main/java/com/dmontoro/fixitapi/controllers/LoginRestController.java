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

@RestController
@RequestMapping("/api/auth")
public class LoginRestController {

    @Autowired
    private TecnicoRepository tecnicoRepository;

    // Este método escucha cuando el móvil hace un POST a /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // Buscamos en la base de datos si coincide el email y la contraseña
        Optional<Tecnico> tecnicoOpt = tecnicoRepository.findAll().stream()
                .filter(t -> t.getEmail() != null && t.getEmail().equalsIgnoreCase(request.getEmail())
                        && t.getPassword() != null && t.getPassword().equals(request.getPassword()))
                .findFirst();

        if (tecnicoOpt.isPresent()) {
            // Si existe y la contraseña está bien, devolvemos el técnico entero al móvil (Código 200 OK)
            return ResponseEntity.ok(tecnicoOpt.get());
        } else {
            // Si falla, devolvemos un error 401 (No autorizado)
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }
    }
}