package com.tripplanner.business.service.impl;

import com.tripplanner.business.service.CategoryService;
import com.tripplanner.dataaccess.repository.CategoryRepository;
import com.tripplanner.domain.dto.CategoryResponseDTO;
import com.tripplanner.domain.entity.Kategorija;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link CategoryService}.
 */
@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponseDTO> listAllCategories() {
        List<Kategorija> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDTO getCategoryById(Integer categoryId) {
        Kategorija category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return mapToResponseDTO(category);
    }

    private CategoryResponseDTO mapToResponseDTO(Kategorija category) {
        return CategoryResponseDTO.builder()
                .kategorijaId(category.getKategorijaId())
                .naziv(category.getNaziv())
                .opis(category.getOpis())
                .build();
    }
}
