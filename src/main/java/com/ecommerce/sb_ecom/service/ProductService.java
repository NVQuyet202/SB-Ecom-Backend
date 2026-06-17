package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.model.Product;
import com.ecommerce.sb_ecom.payload.dto.ProductDTO;
import com.ecommerce.sb_ecom.payload.response.ProductResponse;

public interface ProductService {
    ProductDTO addProduct(Long categoryId, Product product);

    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductResponse getProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}
