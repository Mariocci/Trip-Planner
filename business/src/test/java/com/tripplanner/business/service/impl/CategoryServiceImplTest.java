package com.tripplanner.business.service.impl;

import com.tripplanner.business.base.ServiceTestBase;
import com.tripplanner.dataaccess.repository.CategoryRepository;
import com.tripplanner.domain.dto.CategoryResponseDTO;
import com.tripplanner.domain.entity.Kategorija;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CategoryServiceImpl}.
 * Tests category creation, retrieval, name uniqueness validation, and error handling.
 */
class CategoryServiceImplTest extends ServiceTestBase {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Kategorija testCategory1;
    private Kategorija testCategory2;

    @BeforeEach
    void setUp() {
        testCategory1 = createTestCategory(1, "Sightseeing");
        testCategory2 = createTestCategory(2, "Food & Dining");
    }

    // ========== Category Retrieval Tests ==========

    @Test
    void listAllCategories_WithExistingCategories_ShouldReturnAllCategories() {
        // Given
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory1, testCategory2));

        // When
        List<CategoryResponseDTO> result = categoryService.listAllCategories();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getKategorijaId()).isEqualTo(1);
        assertThat(result.get(0).getNaziv()).isEqualTo("Sightseeing");
        assertThat(result.get(1).getKategorijaId()).isEqualTo(2);
        assertThat(result.get(1).getNaziv()).isEqualTo("Food & Dining");

        verify(categoryRepository).findAll();
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void listAllCategories_WithNoCategories_ShouldReturnEmptyList() {
        // Given
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<CategoryResponseDTO> result = categoryService.listAllCategories();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(categoryRepository).findAll();
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void getCategoryById_WithValidId_ShouldReturnCategory() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory1));

        // When
        CategoryResponseDTO result = categoryService.getCategoryById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getKategorijaId()).isEqualTo(1);
        assertThat(result.getNaziv()).isEqualTo("Sightseeing");
        assertThat(result.getOpis()).isEqualTo("Test category description");

        verify(categoryRepository).findById(1);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void getCategoryById_WithNonExistentId_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.getCategoryById(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");

        verify(categoryRepository).findById(999);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void getCategoryById_WithNullId_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(null)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.getCategoryById(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");

        verify(categoryRepository).findById(null);
        verifyNoMoreInteractions(categoryRepository);
    }

    // ========== Category Name Uniqueness Tests ==========

    @Test
    void listAllCategories_ShouldReturnCategoriesWithUniqueNames() {
        // Given
        Kategorija category1 = createTestCategory(1, "Unique Name 1");
        Kategorija category2 = createTestCategory(2, "Unique Name 2");
        Kategorija category3 = createTestCategory(3, "Unique Name 3");

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2, category3));

        // When
        List<CategoryResponseDTO> result = categoryService.listAllCategories();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        
        // Verify all names are unique
        List<String> names = result.stream()
                .map(CategoryResponseDTO::getNaziv)
                .toList();
        assertThat(names).containsExactlyInAnyOrder("Unique Name 1", "Unique Name 2", "Unique Name 3");
        assertThat(names).doesNotHaveDuplicates();

        verify(categoryRepository).findAll();
        verifyNoMoreInteractions(categoryRepository);
    }

    // ========== DTO Mapping Tests ==========

    @Test
    void getCategoryById_ShouldMapAllFieldsCorrectly() {
        // Given
        Kategorija category = Kategorija.builder()
                .kategorijaId(10)
                .naziv("Adventure")
                .opis("Outdoor and adventure activities")
                .build();

        when(categoryRepository.findById(10)).thenReturn(Optional.of(category));

        // When
        CategoryResponseDTO result = categoryService.getCategoryById(10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getKategorijaId()).isEqualTo(10);
        assertThat(result.getNaziv()).isEqualTo("Adventure");
        assertThat(result.getOpis()).isEqualTo("Outdoor and adventure activities");

        verify(categoryRepository).findById(10);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void listAllCategories_ShouldMapAllFieldsCorrectly() {
        // Given
        Kategorija category1 = Kategorija.builder()
                .kategorijaId(1)
                .naziv("Culture")
                .opis("Cultural activities and museums")
                .build();

        Kategorija category2 = Kategorija.builder()
                .kategorijaId(2)
                .naziv("Sports")
                .opis("Sports and fitness activities")
                .build();

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));

        // When
        List<CategoryResponseDTO> result = categoryService.listAllCategories();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        CategoryResponseDTO dto1 = result.get(0);
        assertThat(dto1.getKategorijaId()).isEqualTo(1);
        assertThat(dto1.getNaziv()).isEqualTo("Culture");
        assertThat(dto1.getOpis()).isEqualTo("Cultural activities and museums");

        CategoryResponseDTO dto2 = result.get(1);
        assertThat(dto2.getKategorijaId()).isEqualTo(2);
        assertThat(dto2.getNaziv()).isEqualTo("Sports");
        assertThat(dto2.getOpis()).isEqualTo("Sports and fitness activities");

        verify(categoryRepository).findAll();
        verifyNoMoreInteractions(categoryRepository);
    }

    // ========== Edge Case Tests ==========

    @Test
    void getCategoryById_WithZeroId_ShouldAttemptToFindCategory() {
        // Given
        when(categoryRepository.findById(0)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.getCategoryById(0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");

        verify(categoryRepository).findById(0);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void getCategoryById_WithNegativeId_ShouldAttemptToFindCategory() {
        // Given
        when(categoryRepository.findById(-1)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.getCategoryById(-1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");

        verify(categoryRepository).findById(-1);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void listAllCategories_WithSingleCategory_ShouldReturnSingleElementList() {
        // Given
        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(testCategory1));

        // When
        List<CategoryResponseDTO> result = categoryService.listAllCategories();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKategorijaId()).isEqualTo(1);
        assertThat(result.get(0).getNaziv()).isEqualTo("Sightseeing");

        verify(categoryRepository).findAll();
        verifyNoMoreInteractions(categoryRepository);
    }

    // ========== Mock Interaction Verification Tests ==========

    @Test
    void getCategoryById_ShouldCallRepositoryOnce() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory1));

        // When
        categoryService.getCategoryById(1);

        // Then
        verify(categoryRepository, times(1)).findById(1);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void listAllCategories_ShouldCallRepositoryOnce() {
        // Given
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory1, testCategory2));

        // When
        categoryService.listAllCategories();

        // Then
        verify(categoryRepository, times(1)).findAll();
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void getCategoryById_WithMultipleCalls_ShouldCallRepositoryMultipleTimes() {
        // Given
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory1));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(testCategory2));

        // When
        categoryService.getCategoryById(1);
        categoryService.getCategoryById(2);

        // Then
        verify(categoryRepository).findById(1);
        verify(categoryRepository).findById(2);
        verifyNoMoreInteractions(categoryRepository);
    }
}
