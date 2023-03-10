package dev.srebootcamp.entity.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test_entity_endpoint() throws Exception {
        mockMvc.perform(get(""))
                .andExpect(status().isOk());
    }
}