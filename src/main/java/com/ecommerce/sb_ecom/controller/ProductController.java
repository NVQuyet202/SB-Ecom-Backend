package com.ecommerce.sb_ecom.controller;

import com.ecommerce.sb_ecom.config.AppConstants;
import com.ecommerce.sb_ecom.model.Product;
import com.ecommerce.sb_ecom.payload.dto.ProductDTO;
import com.ecommerce.sb_ecom.payload.response.ProductResponse;
import com.ecommerce.sb_ecom.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO product,
                                                 @PathVariable Long categoryId) {
        ProductDTO productDTO = productService.addProduct(categoryId,product);
        return new ResponseEntity<ProductDTO>(productDTO, HttpStatus.CREATED);
    }

    @GetMapping("/public/product")
    public ResponseEntity<ProductResponse> getAllCategories(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCT_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        ProductResponse products = productService.getAllProducts(pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategory (
            @PathVariable Long categoryId,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCT_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        ProductResponse productsByCate = productService.getProductsByCategory(categoryId,pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productsByCate,HttpStatus.OK);
    }

    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductsByKeyword (
            @PathVariable String keyword,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCT_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        ProductResponse productsByKey = productService.searchProductsByKeyword(keyword,pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productsByKey,HttpStatus.OK);
    }

    @PutMapping("/public/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct (
            @Valid @PathVariable Long productId,
            @RequestBody ProductDTO productDTO
    ) {
        ProductDTO saveProduct = productService.updateProduct(productId,productDTO);
        return new ResponseEntity<>(saveProduct,HttpStatus.OK);
    }

    @DeleteMapping("/public/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct (
            @PathVariable Long productId
    ) {
        ProductDTO status = productService.deleteProduct(productId);
        return new ResponseEntity<>(status,HttpStatus.OK);
    }

    @PutMapping("/public/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage (
            @PathVariable Long productId, @RequestParam("Image")MultipartFile image
            ) throws IOException {
        ProductDTO updatedProduct = productService.updateProductImage(productId,image);
        return new ResponseEntity<>(updatedProduct,HttpStatus.OK);
    }
}
