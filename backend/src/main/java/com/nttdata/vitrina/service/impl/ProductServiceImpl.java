package com.nttdata.vitrina.service.impl;

import com.nttdata.vitrina.dto.FiltersResponse;
import com.nttdata.vitrina.dto.PageResponse;
import com.nttdata.vitrina.dto.ProductDetailDto;
import com.nttdata.vitrina.dto.ProductSummaryDto;
import com.nttdata.vitrina.model.Product;
import com.nttdata.vitrina.repository.ProductRepository;
import com.nttdata.vitrina.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public PageResponse<ProductSummaryDto> getProducts(int page, int size, String search, String category, String format) {
        int pageNumber = page < 1 ? 1 : page;
        int pageSize = size <= 0 ? 8 : Math.min(size, 100);

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("id").ascending());

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchPattern));
            }

            if (category != null && !category.trim().isEmpty()) {
                String cat = category.trim();
                Predicate matchGroup = cb.equal(root.get("categoryGroup"), cat);
                Predicate matchCategory = cb.equal(root.get("category"), cat);
                predicates.add(cb.or(matchGroup, matchCategory));
            }

            if (format != null && !format.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("format"), format.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductSummaryDto> items = productPage.getContent().stream()
                .map(this::toSummaryDto)
                .toList();

        return new PageResponse<>(
                items,
                productPage.getNumber() + 1,
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.hasNext(),
                productPage.hasPrevious()
        );
    }

    @Override
    public Optional<ProductDetailDto> getProductById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return productRepository.findById(id.trim()).map(this::toDetailDto);
    }

    @Override
    public FiltersResponse getAvailableFilters() {
        List<String> categories = productRepository.findDistinctCategoryGroups();
        List<String> formats = productRepository.findDistinctFormats();
        return new FiltersResponse(categories, formats);
    }

    private ProductSummaryDto toSummaryDto(Product p) {
        return new ProductSummaryDto(
                p.getId(),
                p.getName(),
                p.getCategory(),
                p.getCategoryGroup(),
                p.getFormat(),
                p.getPrice(),
                p.getPriceUnit(),
                p.getOriginalPrice(),
                p.getCurrency(),
                p.getImageUrl(),
                p.getProductUrl()
        );
    }

    private ProductDetailDto toDetailDto(Product p) {
        return new ProductDetailDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getCategory(),
                p.getCategoryGroup(),
                p.getFormat(),
                p.getPrice(),
                p.getPriceUnit(),
                p.getOriginalPrice(),
                p.getCurrency(),
                p.getImageUrl(),
                p.getProductUrl(),
                p.getExtractedAt()
        );
    }
}
