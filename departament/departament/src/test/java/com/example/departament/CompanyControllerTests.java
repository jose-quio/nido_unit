package com.example.departament;

import com.example.departament.Entity.Company;
import com.example.departament.Repository.CompanyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CompanyControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void limpiarBaseDeDatos() {
        companyRepository.deleteAll();
    }

    // CREATE
    @Test
    void createCompany_exito() throws Exception {
        String companyJson = """
        {
            "nombre": "Tech Solutions SA",
            "direccion": "Av. Tecnológica 123",
            "telefono": "987654321",
            "ruc": "20123456789"
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Tech Solutions SA"))
                .andExpect(jsonPath("$.ruc").value("20123456789"));
    }

    @Test
    void createCompany_camposFaltantes() throws Exception {
        String companyJson = """
        {
            "nombre": "Empresa Incompleta",
            "telefono": "987654321"
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyJson))
                .andExpect(status().isCreated());
    }

    // GET ALL
    @Test
    void getAllCompanies_retornaLista() throws Exception {
        // Crear 2 compañías de prueba
        Company company1 = new Company();
        company1.setNombre("Empresa 1");
        company1.setDireccion("Dirección 1");
        company1.setTelefono("111111111");
        company1.setRuc("11111111111");
        companyRepository.save(company1);

        Company company2 = new Company();
        company2.setNombre("Empresa 2");
        company2.setDireccion("Dirección 2");
        company2.setTelefono("222222222");
        company2.setRuc("22222222222");
        companyRepository.save(company2);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/company")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getAllCompanies_retornaListaVacia() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/company")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // GET COMPANY BY ID
    @Test
    void getCompanyById_exito() throws Exception {
        Company company = new Company();
        company.setNombre("Empresa para Buscar");
        company.setDireccion("Dirección de prueba");
        company.setTelefono("999999999");
        company.setRuc("99999999999");
        Company savedCompany = companyRepository.save(company);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/company/" + savedCompany.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCompany.getId()))
                .andExpect(jsonPath("$.nombre").value("Empresa para Buscar"));
    }

    @Test
    void getCompanyById_noExiste() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/company/9999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // UPDATE COMPANY TESTS
    @Test
    void updateCompany_exito() throws Exception {
        Company company = new Company();
        company.setNombre("Empresa Original");
        company.setDireccion("Dirección Original");
        company.setTelefono("555555555");
        company.setRuc("55555555555");
        Company savedCompany = companyRepository.save(company);

        String companyActualizadaJson = """
        {
            "nombre": "Empresa Actualizada",
            "direccion": "Nueva Dirección",
            "telefono": "666666666",
            "ruc": "66666666666"
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/company/" + savedCompany.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyActualizadaJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Empresa Actualizada"))
                .andExpect(jsonPath("$.telefono").value("666666666"));
    }

    @Test
    void updateCompany_noExiste() throws Exception {
        String companyJson = """
        {
            "nombre": "Empresa Inexistente",
            "direccion": "Dirección",
            "telefono": "777777777",
            "ruc": "77777777777"
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/company/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCompany_camposFaltantes() throws Exception {
        Company company = new Company();
        company.setNombre("Empresa para Actualizar");
        company.setDireccion("Dirección");
        company.setTelefono("888888888");
        company.setRuc("88888888888");
        Company savedCompany = companyRepository.save(company);

        String companyJson = """
        {
            "nombre": "Empresa con campos faltantes",
            "telefono": "999999999"
        }
        """;

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/company/" + savedCompany.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyJson))
                .andExpect(status().isOk());
    }
}
