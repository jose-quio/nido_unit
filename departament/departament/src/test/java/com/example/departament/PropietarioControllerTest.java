package com.example.departament;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.departament.Entity.Departamento;
import com.example.departament.Entity.Propietario;
import com.example.departament.Repository.DepartamentoRepository;
import com.example.departament.Repository.PropietarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PropietarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Métodos auxiliares
    private Long crearPropietarioDePrueba(String nombres, String apellidos, String dni) throws Exception {
        String propietarioJson = String.format("""
        {
            "nombres": "%s",
            "apellidos": "%s",
            "dni": "%s",
            "telefono": "987654321",
            "correo": "test@example.com"
        }
        """, nombres, apellidos, dni);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/propietario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propietarioJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), Propietario.class).getId();
    }

    private Long crearDepartamentoDePrueba() {
        Departamento departamento = new Departamento();
        departamento.setNumero("101");
        departamento.setPiso("1");
        departamento.setDisponible(true);
        return departamentoRepository.save(departamento).getId();
    }

    @AfterEach
    void limpiarBaseDeDatos() {
        propietarioRepository.deleteAll();
        departamentoRepository.deleteAll();
    }

    // crear propietario
    @Test
    void createPropietario_exito() throws Exception {
        String propietarioJson = """
        {
            "nombres": "Juan",
            "apellidos": "Pérez",
            "dni": "12345678",
            "telefono": "987654321",
            "correo": "juan@example.com"
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/propietario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propietarioJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombres").value("Juan"))
                .andExpect(jsonPath("$.dni").value("12345678"));
    }

    @Test
    void createPropietario_datosIncompletos_OK() throws Exception {
        String propietarioJson = """
        {
            "nombres": "",
            "apellidos": null,
            "dni": ""
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/propietario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propietarioJson))
                .andExpect(status().isCreated());
    }

    // obtener propietario por ID
    @Test
    void getPropietarioById_exito() throws Exception {
        Long propietarioId = crearPropietarioDePrueba("María", "Gómez", "87654321");

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/propietario/" + propietarioId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(propietarioId))
                .andExpect(jsonPath("$.nombres").value("María"));
    }

    @Test
    void getPropietarioById_noExiste() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/propietario/9999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // obtener todos los propietarios
    @Test
    void getAllPropietarios_retornaLista() throws Exception {
        crearPropietarioDePrueba("Carlos", "López", "11111111");
        crearPropietarioDePrueba("Ana", "Martínez", "22222222");

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/propietario")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllPropietarios_retornaListaVacia() throws Exception {
        propietarioRepository.deleteAll();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/propietario")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    //  actualizar propietario
    @Test
    void updatePropietario_exito() throws Exception {
        Long propietarioId = crearPropietarioDePrueba("Original", "Original", "12345678");

        String propietarioActualizadoJson = """
        {
            "nombres": "Actualizado",
            "apellidos": "Actualizado",
            "dni": "87654321",
            "telefono": "987654321",
            "correo": "actualizado@example.com"
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/propietario/" + propietarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propietarioActualizadoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Actualizado"))
                .andExpect(jsonPath("$.dni").value("87654321"));
    }

    @Test
    void updatePropietario_noExiste() throws Exception {
        String propietarioJson = """
        {
            "nombres": "No",
            "apellidos": "Existe",
            "dni": "99999999"
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/propietario/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propietarioJson))
                .andExpect(status().isNotFound());
    }

    // eliminar propietario
    @Test
    void deletePropietario_exito() throws Exception {
        Long propietarioId = crearPropietarioDePrueba("Eliminar", "Este", "33333333");

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/propietario/" + propietarioId))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/propietario/" + propietarioId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePropietario_noExiste() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/propietario/9999"))
                .andExpect(status().isNotFound());
    }

    //  obtener departamentos de un propietario
    @Test
    void getDepartamentosByPropietario_exito() throws Exception {
        Long propietarioId = crearPropietarioDePrueba("Con", "Departamentos", "44444444");
        Long departamentoId = crearDepartamentoDePrueba();

        // Asignar departamento
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/propietario/" + propietarioId + "/departamentos/" + departamentoId))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/propietario/" + propietarioId + "/departamentos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(departamentoId));
    }

    @Test
    void getDepartamentosByPropietario_sinDepartamentos() throws Exception {
        Long propietarioId = crearPropietarioDePrueba("Sin", "Departamentos", "55555555");

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/propietario/" + propietarioId + "/departamentos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // asignar departamento a propietario
    @Test
    void asignarDepartamentoAPropietario_exito() throws Exception {
        Long propietarioId = crearPropietarioDePrueba("Para", "Asignar", "66666666");
        Long departamentoId = crearDepartamentoDePrueba();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/propietario/" + propietarioId + "/departamentos/" + departamentoId))
                .andExpect(status().isOk());

        // Verificar la relación
        Propietario propietario = propietarioRepository.findById(propietarioId).get();
        Set<Departamento> departamentos = propietario.getDepartamentos();
        assertEquals(1, departamentos.size());
        assertEquals(departamentoId, departamentos.iterator().next().getId());
    }

    @Test
    void asignarDepartamentoAPropietario_relacionExistente() throws Exception {
        Long propietarioId = crearPropietarioDePrueba("Relacion", "Existente", "77777777");
        Long departamentoId = crearDepartamentoDePrueba();

        // Primera asignación
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/propietario/" + propietarioId + "/departamentos/" + departamentoId))
                .andExpect(status().isOk());

        // Segunda asignación
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/propietario/" + propietarioId + "/departamentos/" + departamentoId))
                .andExpect(status().isConflict());
    }

    @Test
    void asignarDepartamentoAPropietario_propietarioNoExiste() throws Exception {
        Long departamentoId = crearDepartamentoDePrueba();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/propietario/9999/departamentos/" + departamentoId))
                .andExpect(status().isNotFound());
    }

    @Test
    void asignarDepartamentoAPropietario_departamentoNoExiste() throws Exception {
        Long propietarioId = crearPropietarioDePrueba("Departamento", "Inexistente", "88888888");

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/propietario/" + propietarioId + "/departamentos/9999"))
                .andExpect(status().isNotFound());
    }

}
