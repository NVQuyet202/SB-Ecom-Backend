package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exceptions.APIException;
import com.ecommerce.sb_ecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sb_ecom.model.Category;
import com.ecommerce.sb_ecom.model.Product;
import com.ecommerce.sb_ecom.payload.dto.CategoryDTO;
import com.ecommerce.sb_ecom.payload.dto.ProductDTO;
import com.ecommerce.sb_ecom.payload.response.ProductResponse;
import com.ecommerce.sb_ecom.repository.CategoryRepository;
import com.ecommerce.sb_ecom.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        boolean isProductPresent = category.getProducts().stream()
                .anyMatch(p -> p.getProductName().equals(productDTO.getProductName()));

        if (isProductPresent) {
            throw new APIException("Product already exists with name: " + productDTO.getProductName());
        }


        double specialPrice = productDTO.getPrice() - (productDTO.getDiscount() * 0.01) * productDTO.getPrice();
        Product newProduct = Product.builder()
                .productName(productDTO.getProductName())
                .description(productDTO.getDescription())
                .quantity(productDTO.getQuantity())
                .price(productDTO.getPrice())
                .image(productDTO.getImage() != null ? productDTO.getImage() : "default.png")
                .discount(productDTO.getDiscount())
                .specialPrice(specialPrice)
                .category(category)
                .build();

        Product savedProduct = productRepository.save(newProduct);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAnOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAnOrder);
        Page<Product> productPage = productRepository.findAll(pageDetails);
        List<Product> products = productPage.getContent();
        if (productPage.getTotalElements() == 0)
            throw new APIException("No product created till now");

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        return ProductResponse.builder()
                .content(productDTOS)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .lastPage(productPage.isLast())
                .build();

    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        Sort sortByAnOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAnOrder);
        Page<Product> productPage = productRepository.findByCategory_CategoryId(category.getCategoryId(), pageDetails);
        List<Product> products = productPage.getContent();
        if (productPage.getTotalElements() == 0)
            throw new APIException("No product created till now");

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        return ProductResponse.builder()
                .content(productDTOS)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .lastPage(productPage.isLast())
                .build();
    }

    @Override
    public ProductResponse searchProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAnOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAnOrder);
        Page<Product> productPage = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);

        List<Product> products = productPage.getContent();
        if (productPage.getTotalElements() == 0)
            throw new APIException("No product created till now");

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        return ProductResponse.builder()
                .content(productDTOS)
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .lastPage(productPage.isLast())
                .build();
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {

        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (productDTO.getProductName() != null) {
            productFromDb.setProductName(productDTO.getProductName());
        }

        if (productDTO.getDescription() != null) {
            productFromDb.setDescription(productDTO.getDescription());
        }

        if (productDTO.getQuantity() != null) {
            productFromDb.setQuantity(productDTO.getQuantity());
        }

        if (productDTO.getPrice() != null) {
            productFromDb.setPrice(productDTO.getPrice());
        }

        if (productDTO.getDiscount() != null) {
            productFromDb.setDiscount(productDTO.getDiscount());
        }

        if (productDTO.getPrice() != null || productDTO.getDiscount() != null) {

            double price = productFromDb.getPrice();
            double discount = productFromDb.getDiscount();

            double specialPrice = price * (1 - discount / 100);
            productFromDb.setSpecialPrice(specialPrice);
        }

        Product savedProduct = productRepository.save(productFromDb);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        productRepository.delete(productFromDb);
        return modelMapper.map(productFromDb, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        //Get the product from DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        //Upload image to server
        //Get the file name of uploaded image
        String fileName = fileService.uploadImage(path, image);

        //Updating the new file name to the product
        productFromDb.setImage(fileName);

        //Save Product
        Product savedProduct = productRepository.save(productFromDb);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }
}


