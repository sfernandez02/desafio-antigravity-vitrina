package com.nttdata.vitrina.service;

import com.nttdata.vitrina.dto.FiltersResponse;
import com.nttdata.vitrina.dto.PageResponse;
import com.nttdata.vitrina.dto.ProductDetailDto;
import com.nttdata.vitrina.dto.ProductSummaryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("Debe paginar correctamente la primera página (1-based) con 8 elementos por defecto")
    void testGetProductsDefaultPagination() {
        PageResponse<ProductSummaryDto> response = productService.getProducts(1, 8, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getPage());
        assertEquals(8, response.getSize());
        assertEquals(8, response.getItems().size());
        assertEquals(4032, response.getTotalElements());
        assertEquals(504, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    @DisplayName("Debe buscar productos por nombre insensible a mayúsculas/minúsculas")
    void testSearchByName() {
        PageResponse<ProductSummaryDto> response = productService.getProducts(1, 20, "chOcoLate", null, null);

        assertNotNull(response);
        assertTrue(response.getTotalElements() > 0, "Debe encontrar productos con 'chocolate'");
        for (ProductSummaryDto item : response.getItems()) {
            assertTrue(item.getName().toLowerCase().contains("chocolate"),
                    "El nombre '" + item.getName() + "' debe contener 'chocolate'");
        }
    }

    @Test
    @DisplayName("Debe filtrar productos por categoría")
    void testFilterByCategory() {
        PageResponse<ProductSummaryDto> response = productService.getProducts(1, 10, null, "Bodega", null);

        assertNotNull(response);
        assertTrue(response.getTotalElements() > 0);
        for (ProductSummaryDto item : response.getItems()) {
            assertEquals("Bodega", item.getCategoryGroup());
        }
    }

    @Test
    @DisplayName("Debe filtrar productos por formato")
    void testFilterByFormat() {
        PageResponse<ProductSummaryDto> response = productService.getProducts(1, 10, null, null, "Lata");

        assertNotNull(response);
        assertTrue(response.getTotalElements() > 0);
        for (ProductSummaryDto item : response.getItems()) {
            assertEquals("Lata", item.getFormat());
        }
    }

    @Test
    @DisplayName("Debe combinar búsqueda y filtros de categoría y formato")
    void testCombinedSearchAndFilter() {
        PageResponse<ProductSummaryDto> response = productService.getProducts(1, 10, "limón", "Agua y refrescos", "Lata");

        assertNotNull(response);
        assertTrue(response.getTotalElements() > 0);
        for (ProductSummaryDto item : response.getItems()) {
            assertTrue(item.getName().toLowerCase().contains("limón") || item.getName().toLowerCase().contains("limon"));
            assertEquals("Agua y refrescos", item.getCategoryGroup());
            assertEquals("Lata", item.getFormat());
        }
    }

    @Test
    @DisplayName("Debe obtener el detalle completo de un producto existente")
    void testGetProductByIdExisting() {
        Optional<ProductDetailDto> detailOpt = productService.getProductById("10005");

        assertTrue(detailOpt.isPresent());
        ProductDetailDto detail = detailOpt.get();
        assertEquals("10005", detail.getId());
        assertEquals("Chocolate líquido a la taza Hacendado", detail.getName());
        assertNotNull(detail.getDescription());
        assertEquals(2551L, detail.getPrice());
        assertEquals("CLP", detail.getCurrency());
        assertEquals("2025-11-06 20:29:53", detail.getExtractedAt());
    }

    @Test
    @DisplayName("Debe retornar vacío si el id no existe")
    void testGetProductByIdNotFound() {
        Optional<ProductDetailDto> detailOpt = productService.getProductById("ID_INEXISTENTE_99999");
        assertTrue(detailOpt.isEmpty());
    }

    @Test
    @DisplayName("Debe obtener las listas de categorías y formatos disponibles")
    void testGetAvailableFilters() {
        FiltersResponse filters = productService.getAvailableFilters();

        assertNotNull(filters);
        assertNotNull(filters.getCategories());
        assertNotNull(filters.getFormats());
        assertFalse(filters.getCategories().isEmpty());
        assertFalse(filters.getFormats().isEmpty());

        assertTrue(filters.getCategories().contains("Bodega"));
        assertTrue(filters.getCategories().contains("Huevos, leche y mantequilla"));
        assertTrue(filters.getFormats().contains("Lata"));
        assertTrue(filters.getFormats().contains("Tarro"));
    }
}
