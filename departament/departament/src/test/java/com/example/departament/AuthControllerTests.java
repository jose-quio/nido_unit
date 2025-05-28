package com.example.departament;


import com.example.departament.Entity.Company;
import com.example.departament.Entity.Rol;
import com.example.departament.Entity.User;
import com.example.departament.Repository.CompanyRepository;
import com.example.departament.Repository.RolRepository;
import com.example.departament.Repository.UserRepository;
import com.example.departament.config.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Asegurar que los roles existan
        for (Rol.RolNombre rolNombre : Rol.RolNombre.values()) {
            if (!rolRepository.existsByNombre(rolNombre)) {
                Rol rol = new Rol();
                rol.setNombre(rolNombre);
                rolRepository.save(rol);
            }
        }
    }

    @AfterEach
    void limpiarBaseDeDatos() {
        userRepository.deleteAll();
        companyRepository.deleteAll();
    }

    // Métodos auxiliares
    private Long crearUsuarioDePrueba(String email, String password, Rol.RolNombre rol) {
        User user = new User();
        user.setUsername(email.split("@")[0]);
        user.setPassword(password);
        user.setNombre("Test User");
        user.setEmail(email);
        user.setEnabled(true);
        user.setRoles(Set.of(rolRepository.findByNombre(rol).get()));
        return userRepository.save(user).getId();
    }

    private Long crearCompanyDePrueba(String nombre) {
        Company company = new Company();
        company.setNombre(nombre);
        company.setDireccion("Dirección " + nombre);
        company.setTelefono("123456789");
        company.setRuc("12345678901");
        return companyRepository.save(company).getId();
    }

    // LOGIN TESTS
    @Test
    void login_exitoso() throws Exception {
        String email = "test@example.com";
        String password = "password123";
        crearUsuarioDePrueba(email, password, Rol.RolNombre.ADMIN_COMPANY);

        String loginJson = """
        {
            "email": "%s",
            "password": "%s"
        }
        """.formatted(email, password);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(email.split("@")[0]))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN_COMPANY"))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    void login_usuarioNoExiste() throws Exception {
        String loginJson = """
        {
            "email": "noexiste@example.com",
            "password": "password123"
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Usuario no encontrado"));
    }

    @Test
    void login_contrasenaIncorrecta() throws Exception {
        String email = "test@example.com";
        crearUsuarioDePrueba(email, "password123", Rol.RolNombre.ADMIN_COMPANY);

        String loginJson = """
        {
            "email": "%s",
            "password": "incorrecta"
        }
        """.formatted(email);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Contraseña incorrecta"));
    }

    // REGISTER TESTS
    @Test
    void register_exitoso() throws Exception {
        String registerJson = """
        {
            "username": "nuevousuario",
            "password": "password123",
            "nombre": "Nuevo Usuario",
            "email": "nuevo@example.com"
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("nuevousuario"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN_COMPANY"))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    void register_conCompany() throws Exception {
        Long companyId = crearCompanyDePrueba("Empresa Test");

        String registerJson = """
        {
            "username": "nuevousuario",
            "password": "password123",
            "nombre": "Nuevo Usuario",
            "email": "nuevo@example.com",
            "companyId": %d
        }
        """.formatted(companyId);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCompany").value(companyId));
    }

    @Test
    void register_emailExistente() throws Exception {
        String email = "existente@example.com";
        crearUsuarioDePrueba(email, "password123", Rol.RolNombre.ADMIN_COMPANY);

        String registerJson = """
        {
            "username": "nuevousuario",
            "password": "password123",
            "nombre": "Nuevo Usuario",
            "email": "%s"
        }
        """.formatted(email);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("Usuario ya existe"));
    }



    // REFRESH TOKEN
    @Test
    void refreshToken_exitoso() throws Exception {
        String email = "refresh@example.com";
        User user = new User();
        user.setUsername("refreshuser");
        user.setPassword("password");
        user.setNombre("Refresh User");
        user.setEmail(email);
        user.setEnabled(true);
        user.setRoles(Set.of(rolRepository.findByNombre(Rol.RolNombre.ADMIN_COMPANY).get()));
        userRepository.save(user);

        String refreshToken = jwtUtil.generateRefreshToken(user);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("refreshuser"));
    }

    @Test
    void refreshToken_sinCookie() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("No hay cookies"));
    }

    @Test
    void refreshToken_tokenInvalido() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "token_invalido")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Refresh token inválido"));
    }

    // LOGOUT
    @Test
    void logout_exitoso() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sesión cerrada exitosamente"))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }
}
