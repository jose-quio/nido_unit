package com.example.departament;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
public class TokenTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getAdminToken() throws Exception {
        String loginRequest = """
    {
        "email": "admin@compania.com",
        "password": "admin123"
    }
    """;

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Login response body: " + responseBody);  // Debug temporal

        JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
        return jsonNode.get("token").asText();
    }


    @Test
    void accesoExitosoConTokenYRolCorrecto() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/company")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void accesoDenegadoSinToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/company"))
                .andExpect(status().isForbidden());
    }

    @Test
    void accesoDenegadoConTokenInvalido() throws Exception {
        String tokenFalso = "Bearer faketoken.jwt.string";

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/company")
                        .header("Authorization", tokenFalso))
                .andExpect(status().isForbidden());
    }
}

