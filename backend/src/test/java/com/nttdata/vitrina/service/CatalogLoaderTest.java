package com.nttdata.vitrina.service;

import com.nttdata.vitrina.model.Product;
import com.nttdata.vitrina.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CatalogLoaderTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Debe cargar exactamente 4.032 productos reales desde catalog.csv en la base H2")
    void testCatalogLoadedCompletely() {
        long count = productRepository.count();
        assertEquals(4032, count, "El número de productos cargados debe ser exactamente 4.032");

        Optional<Product> sampleOpt = productRepository.findById("10005");
        assertTrue(sampleOpt.isPresent(), "El producto con id 10005 debe existir");

        Product sample = sampleOpt.get();
        assertEquals("Chocolate líquido a la taza Hacendado", sample.getName());
        assertEquals("Brick", sample.getFormat());
        assertEquals("Cacao, café e infusiones > Cacao soluble y chocolate a la taza", sample.getCategory());
        assertEquals("Cacao, café e infusiones", sample.getCategoryGroup());
        assertEquals(2551L, sample.getPrice());
        assertEquals("CLP", sample.getCurrency());
        assertNotNull(sample.getDescription());
        assertTrue(sample.getDescription().contains("Chocolate líquido a la taza Hacendado"));
    }
}
