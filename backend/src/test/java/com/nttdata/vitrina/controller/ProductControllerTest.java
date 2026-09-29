package com.nttdata.vitrina.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/products debe retornar paginación por defecto con 8 elementos y total de 4.032")
    void testGetProductsDefault() throws Exception {
        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(8))
                .andExpect(jsonPath("$.totalElements").value(4032))
                .andExpect(jsonPath("$.totalPages").value(504))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false))
                .andExpect(jsonPath("$.items", hasSize(8)))
                .andExpect(jsonPath("$.items[0].id").isNotEmpty())
                .andExpect(jsonPath("$.items[0].name").isNotEmpty())
                .andExpect(jsonPath("$.items[0].price").isNumber())
                .andExpect(jsonPath("$.items[0].currency").value("CLP"));
    }

    @Test
    @DisplayName("GET /api/products con búsqueda por nombre debe filtrar correctamente")
    void testGetProductsWithSearch() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("search", "chocolate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThan(0)))
                .andExpect(jsonPath("$.items[0].name", containsStringIgnoringCase("chocolate")));
    }

    @Test
    @DisplayName("GET /api/products con filtro de categoría debe devolver solo esa categoría")
    void testGetProductsWithCategoryFilter() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("category", "Bodega")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThan(0)))
                .andExpect(jsonPath("$.items[0].categoryGroup").value("Bodega"));
    }

    @Test
    @DisplayName("GET /api/products con filtro de formato debe devolver solo ese formato")
    void testGetProductsWithFormatFilter() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("format", "Lata")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThan(0)))
                .andExpect(jsonPath("$.items[0].format").value("Lata"));
    }

    @Test
    @DisplayName("GET /api/products/{id} con ID existente retorna 200 y detalle completo")
    void testGetProductByIdExisting() throws Exception {
        mockMvc.perform(get("/api/products/10005")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("10005"))
                .andExpect(jsonPath("$.name").value("Chocolate líquido a la taza Hacendado"))
                .andExpect(jsonPath("$.description").isNotEmpty())
                .andExpect(jsonPath("$.format").value("Brick"))
                .andExpect(jsonPath("$.price").value(2551))
                .andExpect(jsonPath("$.originalPrice").value(2551))
                .andExpect(jsonPath("$.currency").value("CLP"))
                .andExpect(jsonPath("$.extractedAt").value("2025-11-06 20:29:53"));
    }

    @Test
    @DisplayName("GET /api/products/{id} con ID inexistente retorna 404 Not Found")
    void testGetProductByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/products/ID_NO_EXISTE_99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/products/filters retorna listas de categorías y formatos")
    void testGetFilters() throws Exception {
        mockMvc.perform(get("/api/products/filters")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", not(empty())))
                .andExpect(jsonPath("$.formats", not(empty())))
                .andExpect(jsonPath("$.categories", hasItem("Bodega")))
                .andExpect(jsonPath("$.formats", hasItem("Lata")));
    }
}
