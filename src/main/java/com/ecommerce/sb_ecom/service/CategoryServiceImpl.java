package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exceptions.APIException;
import com.ecommerce.sb_ecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sb_ecom.model.Category;
import com.ecommerce.sb_ecom.payload.dto.CategoryDTO;
import com.ecommerce.sb_ecom.payload.response.CategoryResponse;
import com.ecommerce.sb_ecom.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public CategoryResponse getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        if(categories.isEmpty())
            throw new APIException("No category created till now");

        return CategoryResponse.builder()
                .content(categories)
                .build();
    }

    @Override
    public void createNewCategory(CategoryDTO category) {
        Category savedCategory =categoryRepository.findByCategoryName(category.getCategoryName());
        if(savedCategory != null)
            throw new APIException("Category with name " + category.getCategoryName() + " already exist!");
        savedCategory.setCategoryName(category.getCategoryName());
        categoryRepository.save(savedCategory);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);

        Category saveCategory = optionalCategory
                .orElseThrow(() -> new ResourceNotFoundException("Category","categoryId",categoryId));


        categoryRepository.delete(saveCategory);
        return "Category with categoryId: " + categoryId + " deleted successfully";
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);

        Category saveCategory = optionalCategory
                .orElseThrow(() -> new ResourceNotFoundException("Category","categoryId",categoryId));

        saveCategory.setCategoryName(category.getCategoryName());
        categoryRepository.save(saveCategory);
        return saveCategory;
    }
}
