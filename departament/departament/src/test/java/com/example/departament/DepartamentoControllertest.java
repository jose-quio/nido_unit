package com.example.departament;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.departament.Entity.Departamento;
import com.example.departament.Entity.Edificio;
import com.example.departament.Repository.DepartamentoRepository;
import com.example.departament.Repository.EdificioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;

@SpringBootTest
@AutoConfigureMockMvc
public class DepartamentoControllertest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private EdificioRepository edificioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Métodos auxiliares
    private Long crearEdificioDePrueba(String nombre, String direccion) throws Exception {
        Edificio edificio = new Edificio();
        edificio.setNombre(nombre);
        edificio.setDireccion(direccion);
        return edificioRepository.save(edificio).getId();
    }

    private Long crearDepartamentoDePruebaYRetornarId(Long edificioId) throws Exception {
        String departamentoJson = """
        {
            "numero": "101",
            "piso": "1",
            "nroHabitaciones": 3,
            "area": 75.5,
            "precioVenta": 150000.00,
            "precioAlquiler": 1200.00,
            "disponible": true
        }
        """;

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/apartamentos/edificio/" + edificioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(departamentoJson))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), Departamento.class).getId();
    }

    @AfterEach
    void limpiarBaseDeDatos() {
        departamentoRepository.deleteAll();
        edificioRepository.deleteAll();
    }

    // crear departamento
    @Test
    void createApartamento_exito() throws Exception {
        Long edificioId = crearEdificioDePrueba("Torre Central", "Av. Principal");

        String departamentoJson = String.format("""
        {
            "numero": "101",
            "piso": "1",
            "nroHabitaciones": 3,
            "area": 75.5,
            "precioVenta": 150000.00,
            "precioAlquiler": 1200.00,
            "disponible": true,
            "edificio": {
                "id": %d
            }
        }
        """, edificioId);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/apartamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(departamentoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("101"))
                .andExpect(jsonPath("$.edificio.id").value(edificioId));
    }

    @Test
    void createApartamento_sinEdificio() throws Exception {
        String departamentoJson = """
        {
            "numero": "101",
            "piso": "1",
            "nroHabitaciones": 3,
            "area": 75.5,
            "precioVenta": 150000.00,
            "precioAlquiler": 1200.00,
            "disponible": true
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/apartamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(departamentoJson))
                .andExpect(status().isBadRequest());
    }

    // departamento por ID
    @Test
    void getApartamentoById_exito() throws Exception {
        Long edificioId = crearEdificioDePrueba("Torre Norte", "Calle Secundaria");
        Long departamentoId = crearDepartamentoDePruebaYRetornarId(edificioId);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/apartamentos/" + departamentoId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(departamentoId))
                .andExpect(jsonPath("$.numero").value("101"));
    }

    @Test
    void getApartamentoById_noExiste() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/apartamentos/9999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // todos los departamentos
    @Test
    void getAllApartamentos_retornaLista() throws Exception {
        Long edificioId = crearEdificioDePrueba("Edificio Test", "Calle Test");
        crearDepartamentoDePruebaYRetornarId(edificioId);
        crearDepartamentoDePruebaYRetornarId(edificioId);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/apartamentos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllApartamentos_retornaListaVacia() throws Exception {
        departamentoRepository.deleteAll();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/apartamentos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // actualizar departamento
    @Test
    void updateApartamento_exito() throws Exception {
        Long edificioId = crearEdificioDePrueba("Edificio Original", "Dirección Original");
        Long departamentoId = crearDepartamentoDePruebaYRetornarId(edificioId);

        String departamentoActualizadoJson = """
        {
            "numero": "202",
            "piso": "2",
            "nroHabitaciones": 4,
            "area": 85.0,
            "precioVenta": 180000.00,
            "precioAlquiler": 1500.00,
            "disponible": false
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/apartamentos/" + departamentoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(departamentoActualizadoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("202"))
                .andExpect(jsonPath("$.disponible").value(false));
    }

    @Test
    void updateApartamento_noExiste() throws Exception {
        String departamentoJson = """
        {
            "numero": "101",
            "piso": "1",
            "nroHabitaciones": 3,
            "area": 75.5,
            "precioVenta": 150000.00,
            "precioAlquiler": 1200.00,
            "disponible": true
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/apartamentos/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(departamentoJson))
                .andExpect(status().isNotFound());
    }

    // eliminar departamento
    @Test
    void deleteApartamento_exito() throws Exception {
        Long edificioId = crearEdificioDePrueba("Edificio Test", "Calle Test");
        Long departamentoId = crearDepartamentoDePruebaYRetornarId(edificioId);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/apartamentos/" + departamentoId))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/apartamentos/" + departamentoId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteApartamento_noExiste() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/apartamentos/9999"))
                .andExpect(status().isNotFound());
    }

    //departamentos disponibles
    @Test
    void getApartamentosDisponibles_retornaLista() throws Exception {
        Long edificioId = crearEdificioDePrueba("Edificio Test", "Calle Test");
        crearDepartamentoDePruebaYRetornarId(edificioId);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/apartamentos/disponibles")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getApartamentosDisponibles_retornaListaVacia() throws Exception {
        departamentoRepository.deleteAll();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/apartamentos/disponibles")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

}
