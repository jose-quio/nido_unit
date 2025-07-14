package com.example.departament.Controller;

import com.example.departament.Entity.Company;
import com.example.departament.Entity.Rol;
import com.example.departament.Entity.User;
import com.example.departament.Repository.CompanyRepository;
import com.example.departament.Repository.RolRepository;
import com.example.departament.Repository.UserRepository;
import com.example.departament.config.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
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
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse resp) {
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
        String refreshToken = jwtUtil.generateRefreshToken(user);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Crear un Map mutable para la respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("roles", roles);
        response.put("userId", user.getId());

        // Añadir companyId solo si existe
        if (user.getCompany() != null) {
            response.put("idCompany", user.getCompany().getId());
            response.put("nombreCompany", user.getCompany().getNombre());
        } else {
            response.put("idCompany", null);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO userDTO,HttpServletResponse resp) {
        // Validar si el usuario ya existe
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuario ya existe");
        }

        // Asignar rol
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


        // Generar token y preparar respuesta (igual que en login)
        List<String> roles = savedUser.getRoles().stream()
                .map(rol -> rol.getNombre().name())
                .collect(Collectors.toList());

        String token = jwtUtil.createToken(savedUser, roles);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", savedUser.getUsername());
        response.put("roles", roles);
        response.put("userId", savedUser.getId());

        if (savedUser.getCompany() != null) {
            response.put("idCompany", savedUser.getCompany().getId());
            response.put("nombreCompany", user.getCompany().getNombre());
        } else {
            response.put("idCompany", null);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login/google")
    public ResponseEntity<?> loginWithGoogleAccessToken(@RequestBody Map<String, String> request,HttpServletResponse resp) {
        String accessToken = request.get("access_token");
        String url = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + accessToken;

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> userInfo;

        try {
            userInfo = restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error al validar el token de Google");
        }

        if (userInfo == null || !userInfo.containsKey("email")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o sin email");
        }

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            Rol defaultRol = rolRepository.findByNombre(Rol.RolNombre.ADMIN_COMPANY)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

            user = new User();
            user.setEmail(email);
            user.setUsername(name);
            user.setNombre(name);
            user.setPassword("");
            user.setEnabled(true);
            user.setRoles(Set.of(defaultRol));
            userRepository.save(user);
        }

        // Generar token
        List<String> roles = user.getRoles().stream()
                .map(rol -> rol.getNombre().name())
                .collect(Collectors.toList());

        String jwt = jwtUtil.createToken(user, roles);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("username", user.getUsername());
        response.put("roles", roles);
        response.put("userId", user.getId());
        response.put("idCompany", user.getCompany() != null ? user.getCompany().getId() : null);
        response.put("nombreCompany", user.getCompany() != null ? user.getCompany().getNombre() : null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("refresh");
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No hay cookies");
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token no encontrado");
        }

        try {
            Claims claims = jwtUtil.parseToken(refreshToken);
            String username = claims.getSubject();
            Optional<User> optionalUser = userRepository.findByEmail(username);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no válido");
            }

            User user = optionalUser.get();

            List<String> roles = user.getRoles().stream()
                    .map(rol -> rol.getNombre().name())
                    .collect(Collectors.toList());

            String newAccessToken = jwtUtil.createToken(user, roles);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("token", newAccessToken);
            responseBody.put("username", user.getUsername());
            responseBody.put("roles", roles);
            responseBody.put("userId", user.getId());
            responseBody.put("idCompany", user.getCompany() != null ? user.getCompany().getId() : null);

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token inválido");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Crea una cookie con el mismo nombre y la caduca
        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(0) // Expira inmediatamente
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return ResponseEntity.ok("Sesión cerrada exitosamente");
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
