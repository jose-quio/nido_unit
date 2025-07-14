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
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RolRepository rolRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserRepository userRepository,
                          CompanyRepository companyRepository,
                          RolRepository rolRepository,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.rolRepository = rolRepository;
        this.jwtUtil = jwtUtil;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserCreateDTO userDTO) {
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya existe");
        }

        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El email ya está registrado");
        }

        // hay id company
        if (userDTO.getCompanyId() == null) {
            return ResponseEntity.badRequest().body("Se requiere companyId");
        }

        // hay roles
        if (userDTO.getRoleIds() == null || userDTO.getRoleIds().isEmpty()) {
            return ResponseEntity.badRequest().body("Se requiere al menos un rol");
        }

        // obtener companys y validar
        Company company = companyRepository.findById(userDTO.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        // roles y validar
        Set<Rol> roles = userDTO.getRoleIds().stream()
                .map(roleId -> rolRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Rol con ID " + roleId + " no existe")))
                .collect(Collectors.toSet());

        // crear user
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword()); // {noop} solo para desarrollo
        user.setNombre(userDTO.getNombre());
        user.setEmail(userDTO.getEmail());
        user.setCompany(company);
        user.setRoles(roles);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    // READ ALL
    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getAllUsers(@PathVariable Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empresa no encontrada.");
        }
        List<User> usuarios = userRepository.findByCompanyId(companyId);
        return ResponseEntity.ok(usuarios);
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{userId}/company/{companyId}")
    public ResponseEntity<?> updateUserCompany(
            @PathVariable Long userId,
            @PathVariable Long companyId) {

        //buscar usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        //buscar company
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Compañía no encontrada"));

        //relacion
        user.setCompany(company);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }



    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Map<String, Object>>> listarRoles() {
        List<Rol> roles = rolRepository.findAll();

        // Convertimos los roles a un formato simplificado si no quieres enviar toda la entidad
        List<Map<String, Object>> response = roles.stream().map(rol -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", rol.getId());
            map.put("nombre", rol.getNombre().name());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }



    public static class UserCreateDTO {
        private String username;
        private String password;
        private String nombre;
        private String email;
        private Long companyId;
        private Set<Long> roleIds;

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

        public Set<Long> getRoleIds() {
            return roleIds;
        }

        public void setRoleIds(Set<Long> roleIds) {
            this.roleIds = roleIds;
        }
    }
}






