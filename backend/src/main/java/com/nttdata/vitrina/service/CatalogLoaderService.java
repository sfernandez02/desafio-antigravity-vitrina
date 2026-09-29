package com.nttdata.vitrina.service;

import com.nttdata.vitrina.model.Product;
import com.nttdata.vitrina.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class CatalogLoaderService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogLoaderService.class);

    private final ProductRepository productRepository;

    @Value("${catalog.csv.path:../data/catalog.csv}")
    private String configuredCsvPath;

    public CatalogLoaderService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        loadCatalog();
    }

    @Transactional
    public void loadCatalog() {
        Path csvFile = resolveCsvPath();
        if (csvFile == null || !Files.exists(csvFile)) {
            log.error("No se encontro el archivo de catalogo CSV en las rutas configuradas.");
            return;
        }

        log.info("Cargando catalogo desde: {}", csvFile.toAbsolutePath());
        List<Product> products = new ArrayList<>(4100);

        try (BufferedReader reader = Files.newBufferedReader(csvFile, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                log.warn("El archivo CSV esta vacio.");
                return;
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                List<String> fields = parseCsvLine(line);
                if (fields.size() < 12) {
                    log.warn("Linea {} ignorada por numero insuficiente de campos (encontrados {}): {}",
                            lineNumber, fields.size(), line);
                    continue;
                }

                Product product = mapToProduct(fields);
                products.add(product);
            }

            productRepository.saveAll(products);
            log.info("Catalogo cargado exitosamente: {} productos insertados en H2.", products.size());

        } catch (IOException e) {
            log.error("Error al leer el archivo de catalogo CSV: {}", e.getMessage(), e);
        }
    }

    private Path resolveCsvPath() {
        Path path = Paths.get(configuredCsvPath);
        if (Files.exists(path)) {
            return path;
        }
        Path alt1 = Paths.get("data/catalog.csv");
        if (Files.exists(alt1)) {
            return alt1;
        }
        Path alt2 = Paths.get("../data/catalog.csv");
        if (Files.exists(alt2)) {
            return alt2;
        }
        return path;
    }

    private Product mapToProduct(List<String> f) {
        String id = f.get(0);
        String name = f.get(1);
        String description = f.get(2);
        String format = f.get(3);
        String category = f.get(4);
        Long price = parseLongOrNull(f.get(5));
        String priceUnit = f.get(6);
        Long originalPrice = parseLongOrNull(f.get(7));
        String currency = f.get(8);
        String imageUrl = f.get(9);
        String productUrl = f.get(10);
        String extractedAt = f.get(11);

        String categoryGroup = category != null && category.contains(" > ")
                ? category.split(" > ")[0].trim()
                : category;

        return new Product(
                id,
                name,
                description,
                format,
                category,
                categoryGroup,
                price,
                priceUnit,
                originalPrice,
                currency,
                imageUrl,
                productUrl,
                extractedAt
        );
    }

    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    values.add(sb.toString().trim());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
        }
        values.add(sb.toString().trim());
        return values;
    }
}
