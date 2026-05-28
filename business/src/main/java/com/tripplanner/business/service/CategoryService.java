package com.tripplanner.business.service;

import com.tripplanner.domain.dto.CategoryResponseDTO;

import java.util.List;


public interface CategoryService {

    
    List<CategoryResponseDTO> listAllCategories();

    
    CategoryResponseDTO getCategoryById(Integer categoryId);
}
