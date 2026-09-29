package com.nttdata.vitrina.controller;

import com.nttdata.vitrina.dto.FiltersResponse;
import com.nttdata.vitrina.dto.PageResponse;
import com.nttdata.vitrina.dto.ProductDetailDto;
import com.nttdata.vitrina.dto.ProductSummaryDto;
import com.nttdata.vitrina.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductSummaryDto>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String format) {

        String searchTerm = (search != null && !search.isBlank()) ? search : q;
        PageResponse<ProductSummaryDto> response = productService.getProducts(page, size, searchTerm, category, format);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filters")
    public ResponseEntity<FiltersResponse> getFilters() {
        FiltersResponse filters = productService.getAvailableFilters();
        return ResponseEntity.ok(filters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDto> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
