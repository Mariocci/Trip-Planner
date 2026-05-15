package com.tripplanner.business.service;

import com.tripplanner.domain.dto.CategoryResponseDTO;

import java.util.List;

/**
 * Service interface for category operations.
 */
public interface CategoryService {

    /**
     * Lists all predefined categories.
     *
     * @return list of all categories
     */
    List<CategoryResponseDTO> listAllCategories();

    /**
     * Retrieves a category by ID.
     *
     * @param categoryId the ID of the category
     * @return the category details
     * @throws RuntimeException if category not found
     */
    CategoryResponseDTO getCategoryById(Integer categoryId);
}
