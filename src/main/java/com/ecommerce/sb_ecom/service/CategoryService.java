package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.model.Category;
import com.ecommerce.sb_ecom.payload.dto.CategoryDTO;
import com.ecommerce.sb_ecom.payload.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse getAllCategories();
    void createNewCategory (CategoryDTO category);
    String deleteCategory(Long categoryId);
    Category updateCategory(Category category,Long categoryId);
}
