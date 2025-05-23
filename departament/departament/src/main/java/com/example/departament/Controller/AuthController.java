package com.example.departament.Controller;

import com.example.departament.Entity.User;
import com.example.departament.Repository.UserRepository;
import com.example.departament.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Buscar usuario por nombre de usuario
        User user = userRepository.findByUsername(loginRequest.getUsername());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado");
        }

        // Comparar contraseñas directamente (sin codificar)
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Contraseña incorrecta");
        }

        // Obtener roles como lista de Strings
        List<String> roles = user.getRoles().stream()
                .map(rol -> rol.getNombre().name())
                .collect(Collectors.toList());

        // Generar el token
        String token = jwtUtil.createToken(user, roles);

        // Puedes retornar el token directamente o envolverlo en un objeto
        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "roles", roles
        ));
    }

    // DTO interno para el login
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters y Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
