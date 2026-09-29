package com.nttdata.vitrina.service;

import com.nttdata.vitrina.dto.FiltersResponse;
import com.nttdata.vitrina.dto.PageResponse;
import com.nttdata.vitrina.dto.ProductDetailDto;
import com.nttdata.vitrina.dto.ProductSummaryDto;

import java.util.Optional;

public interface ProductService {

    PageResponse<ProductSummaryDto> getProducts(int page, int size, String search, String category, String format);

    Optional<ProductDetailDto> getProductById(String id);

    FiltersResponse getAvailableFilters();
}
