package com.example.departament.Controller;

import com.example.departament.Entity.Company;
import com.example.departament.Entity.Rol;
import com.example.departament.Entity.User;
import com.example.departament.Repository.CompanyRepository;
import com.example.departament.Repository.RolRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Buscar usuario por nombre de usuario
        Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado");
        }

        User user = optionalUser.get();

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

        // Crear un Map mutable para la respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("roles", roles);
        response.put("userId", user.getId());

        // Añadir companyId solo si existe
        if (user.getCompany() != null) {
            response.put("idCompany", user.getCompany().getId());
        } else {
            response.put("idCompany", null);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO userDTO) {
        // Validar si el usuario ya existe
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuario ya existe");
        }

        // Asignar rol (búsqueda sin relación bidireccional)
        Rol adminRol = rolRepository.findByNombre(Rol.RolNombre.ADMIN_COMPANY)
                .orElseThrow(() -> new RuntimeException("Rol ADMIN_COMPANY no encontrado"));

        // Crear nuevo usuario
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        user.setNombre(userDTO.getNombre());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(true);
        user.setRoles(Set.of(adminRol));

        // Si hay companyId en el DTO, asignar compañía
        if (userDTO.getCompanyId() != null) {
            Company company = companyRepository.findById(userDTO.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Compañía no encontrada"));
            user.setCompany(company);
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    // DTO interno para el login
    public static class LoginRequest {
        private String email;
        private String password;

        // Getters y Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class UserRegistrationDTO {
        private String username;
        private String password;
        private String nombre;
        private String email;
        private Long companyId; // Opcional, para asignar compañía al registrar

        // Getters y Setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public void setCompanyId(Long companyId) {
            this.companyId = companyId;
        }
    }
}
