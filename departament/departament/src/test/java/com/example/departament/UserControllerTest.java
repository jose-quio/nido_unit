package com.example.departament;

import com.example.departament.Controller.UserController;
import com.example.departament.Entity.Company;
import com.example.departament.Entity.Rol;
import com.example.departament.Entity.User;
import com.example.departament.Repository.CompanyRepository;
import com.example.departament.Repository.RolRepository;
import com.example.departament.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

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
        // No eliminamos los roles porque son constantes
    }

    // Métodos auxiliares
    private Long crearCompanyDePrueba(String nombre) {
        Company company = new Company();
        company.setNombre(nombre);
        company.setDireccion("Dirección " + nombre);
        company.setTelefono("123456789");
        company.setRuc("12345678901");
        return companyRepository.save(company).getId();
    }

    private Long crearUsuarioDePrueba(String username, Long companyId, Set<Rol.RolNombre> roleNombres) throws Exception {
        Set<Long> roleIds = roleNombres.stream()
                .map(rolNombre -> rolRepository.findByNombre(rolNombre).get().getId())
                .collect(Collectors.toSet());

        UserController.UserCreateDTO userDTO = new UserController.UserCreateDTO();
        userDTO.setUsername(username);
        userDTO.setPassword("password");
        userDTO.setNombre("Usuario " + username);
        userDTO.setEmail(username + "@test.com");
        userDTO.setCompanyId(companyId);
        userDTO.setRoleIds(roleIds);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), User.class).getId();
    }

    // CREATE USER
    @Test
    void createUser_exito() throws Exception {
        Long companyId = crearCompanyDePrueba("Empresa Test");

        String userJson = String.format("""
        {
            "username": "testuser",
            "password": "password123",
            "nombre": "Usuario Test",
            "email": "test@example.com",
            "companyId": %d,
            "roleIds": [%d]
        }
        """, companyId, rolRepository.findByNombre(Rol.RolNombre.ADMIN_COMPANY).get().getId());

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.company.id").value(companyId));
    }

    @Test
    void createUser_usernameExistente() throws Exception {
        Long companyId = crearCompanyDePrueba("Empresa Test");
        crearUsuarioDePrueba("usuario1", companyId, Set.of(Rol.RolNombre.ADMIN_COMPANY));

        String userJson = String.format("""
        {
            "username": "usuario1",
            "password": "password123",
            "nombre": "Usuario Test",
            "email": "test2@example.com",
            "companyId": %d,
            "roleIds": [%d]
        }
        """, companyId, rolRepository.findByNombre(Rol.RolNombre.ADMIN_COMPANY).get().getId());

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("El nombre de usuario ya existe"));
    }

    @Test
    void createUser_emailExistente() throws Exception {
        Long companyId = crearCompanyDePrueba("Empresa Test");
        crearUsuarioDePrueba("usuario1", companyId, Set.of(Rol.RolNombre.ADMIN_COMPANY));

        String userJson = String.format("""
        {
            "username": "usuario2",
            "password": "password123",
            "nombre": "Usuario Test",
            "email": "usuario1@test.com",
            "companyId": %d,
            "roleIds": [%d]
        }
        """, companyId, rolRepository.findByNombre(Rol.RolNombre.ADMIN_COMPANY).get().getId());

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("El email ya está registrado"));
    }

    @Test
    void createUser_sinCompanyId() throws Exception {
        String userJson = String.format("""
        {
            "username": "testuser",
            "password": "password123",
            "nombre": "Usuario Test",
            "email": "test@example.com",
            "roleIds": [%d]
        }
        """, rolRepository.findByNombre(Rol.RolNombre.ADMIN_COMPANY).get().getId());

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Se requiere companyId"));
    }

    @Test
    void createUser_sinRoles() throws Exception {
        Long companyId = crearCompanyDePrueba("Empresa Test");

        String userJson = String.format("""
        {
            "username": "testuser",
            "password": "password123",
            "nombre": "Usuario Test",
            "email": "test@example.com",
            "companyId": %d
        }
        """, companyId);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Se requiere al menos un rol"));
    }

    // GET ALL USERS
    @Test
    void getAllUsers_retornaLista() throws Exception {
        Long companyId = crearCompanyDePrueba("Empresa Test");
        crearUsuarioDePrueba("usuario1", companyId, Set.of(Rol.RolNombre.ADMIN_COMPANY));
        crearUsuarioDePrueba("usuario2", companyId, Set.of(Rol.RolNombre.MANAGER_EDIFICIO));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // GET USER BY ID
    @Test
    void getUserById_exito() throws Exception {
        Long companyId = crearCompanyDePrueba("Empresa Test");
        Long userId = crearUsuarioDePrueba("usuario1", companyId, Set.of(Rol.RolNombre.ADMIN_COMPANY));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/users/" + userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("usuario1"));
    }

    // UPDATE USER COMPANY
    @Test
    void updateUserCompany_exito() throws Exception {
        Long company1Id = crearCompanyDePrueba("Empresa Original");
        Long company2Id = crearCompanyDePrueba("Empresa Nueva");
        Long userId = crearUsuarioDePrueba("usuario1", company1Id, Set.of(Rol.RolNombre.ADMIN_COMPANY));

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/users/" + userId + "/company/" + company2Id))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/users/" + userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.id").value(company2Id));
    }

    // DELETE USER
    @Test
    void deleteUser_exito() throws Exception {
        Long companyId = crearCompanyDePrueba("Empresa Test");
        Long userId = crearUsuarioDePrueba("usuario1", companyId, Set.of(Rol.RolNombre.ADMIN_COMPANY));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/users/" + userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/users/" + userId))
                .andExpect(status().isNotFound());
    }
}
