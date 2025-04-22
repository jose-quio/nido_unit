package com.example.departament;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.departament.Repository.EdificioRepository;
import com.jayway.jsonpath.JsonPath;
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
public class EdificioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EdificioRepository edificioRepository;

    //Metodos Auxiliares

    private void crearEdificioDePrueba(String nombre, String direccion) throws Exception {
        String json = String.format("""
    {
        "nombre": "%s",
        "direccion": "%s"
    }
    """, nombre, direccion);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/edificio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private Long crearEdificioDePruebaYRetornarId(String nombre, String direccion) throws Exception {
        String json = String.format("""
    {
        "nombre": "%s",
        "direccion": "%s"
    }
    """, nombre, direccion);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/edificio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();


        String responseContent = result.getResponse().getContentAsString();
        return JsonPath.parse(responseContent).read("$.id", Long.class);
    }

    private void crearDepartamentoEnEdificio(Long idEdificio) throws Exception {
        String departamentoJson = """
    {
        "numero": "101",
        "piso": "1",
        "area": 75.5,
        "precioVenta": 150000.00,
        "precioAlquiler": 1200.00,
        "disponible": true
    }
    """;

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/apartamentos/edificio/" + idEdificio)
                .contentType(MediaType.APPLICATION_JSON)
                .content(departamentoJson));
    }

    //Creacion de edificio
    @Test
    void createEdificio_exito() throws Exception {
        String edificioJson = """
    {
        "nombre": "Torre Esmeralda",
        "direccion": "Av. Principal 123"
    }
    """;
        URI uri = new URI("/api/edificio");
        MockHttpServletRequestBuilder req =
                MockMvcRequestBuilders
                        .post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(edificioJson)
                        .accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(req).andReturn();
        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
    }

    @Test
    void createEdificio_exitoNulo() throws Exception {
        String edificioJson = """
    {
        "nombre": "",
        "direccion": null
    }
    """;
        URI uri = new URI("/api/edificio");
        MockHttpServletRequestBuilder req =
                MockMvcRequestBuilders
                        .post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(edificioJson)
                        .accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(req).andReturn();
        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
    }

    //Leer un edificio
    @Test
    void getEdificioById_exito() throws Exception {
        String edificioJson = """
    {
        "nombre": "Torre Esmeralda",
        "direccion": "Av. Principal 123"
    }
    """;
        URI uri = new URI("/api/edificio");
        MockHttpServletRequestBuilder req =
                MockMvcRequestBuilders
                        .post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(edificioJson)
                        .accept(MediaType.APPLICATION_JSON);

        URI uri1 = new URI("/api/edificio/1");
        MockHttpServletRequestBuilder req1 =
                MockMvcRequestBuilders.get(uri1).accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(req1).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    void getEdificioById_RequestNoExiste() throws Exception {
        URI uri = new URI("/api/edificio/999999");
        MockHttpServletRequestBuilder req =
                MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(req).andReturn();
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
    }


    @Test public void getEdificioById_RequestInvalido() throws Exception {
        URI uri = new URI("/api/edificio/abc");
        MockHttpServletRequestBuilder req =
                MockMvcRequestBuilders.get(uri).accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(req).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    //Leer todos los edificios

    @Test public void getAllEdificios_retornaLista() throws Exception {

        edificioRepository.deleteAll();
        crearEdificioDePrueba("Edificio Z","Calle las penurias");
        crearEdificioDePrueba("Edificio L","Calle las noches");

        URI uri = new URI("/api/edificio");
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Edificio Z"))
                .andExpect(jsonPath("$[1].nombre").value("Edificio L"));
    }

    @Test
    void getAllEdificios_retornaListaVacia() throws Exception {
        edificioRepository.deleteAll();
        URI uri = new URI("/api/edificio");
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    //Actualizacion de Edificio

    @Test
    void updateEdificio_exito() throws Exception {
        Long id = crearEdificioDePruebaYRetornarId("Edificio Original", "Dirección Original");

        String edificioActualizadoJson = """
    {
        "nombre": "Edificio Actualizado",
        "direccion": "Nueva Dirección"
    }
    """;

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/edificio/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(edificioActualizadoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Edificio Actualizado"))
                .andExpect(jsonPath("$.direccion").value("Nueva Dirección"));
    }

    @Test
    void updateEdificio_noExiste() throws Exception {
        String edificioJson = """
    {
        "nombre": "Edificio Inexistente",
        "direccion": "Dirección"
    }
    """;

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/edificio/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(edificioJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEdificio_InvalidoOK() throws Exception {
        edificioRepository.deleteAll();
        Long id = crearEdificioDePruebaYRetornarId("Edificio Valido", "Dirección Valida");

        String edificioInvalidoJson = """
    {
        "nombre": "",
        "direccion": null
    }
    """;

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/edificio/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(edificioInvalidoJson))
                .andExpect(status().isOk());
    }

    //Eliminar Edificio

    @Test
    void deleteEdificio_SinContenidoYNoExiste() throws Exception {
        Long id = crearEdificioDePruebaYRetornarId("Edificio a Eliminar", "Dirección");

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/edificio/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/edificio/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEdificio_NoExiste() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/edificio/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEdificio_conDepartamentosSinContenido() throws Exception {

        Long idEdificio = crearEdificioDePruebaYRetornarId("Edificio con Deptos", "Dirección");
        crearDepartamentoEnEdificio(idEdificio);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/edificio/" + idEdificio))
                .andExpect(status().isNoContent());
    }

}
