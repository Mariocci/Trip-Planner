package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Kategorija;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CategoryRepository}.
 * <p>
 * Uses @DataJpaTest to configure an in-memory H2 database for testing
 * repository operations without requiring a full application context.
 * </p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Kategorija testCategory1;
    private Kategorija testCategory2;

    @BeforeEach
    void setUp() {
        // Create test categories
        testCategory1 = Kategorija.builder()
                .naziv("Sightseeing")
                .opis("Tourist attractions and landmarks")
                .build();

        testCategory2 = Kategorija.builder()
                .naziv("Food & Dining")
                .opis("Restaurants and culinary experiences")
                .build();

        // Persist test categories
        entityManager.persist(testCategory1);
        entityManager.persist(testCategory2);
        entityManager.flush();
    }

    @Test
    void findById_WithExistingId_ShouldReturnCategory() {
        // When
        Optional<Kategorija> result = categoryRepository.findById(testCategory1.getKategorijaId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNaziv()).isEqualTo("Sightseeing");
        assertThat(result.get().getOpis()).isEqualTo("Tourist attractions and landmarks");
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<Kategorija> result = categoryRepository.findById(99999);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllCategories() {
        // When
        var results = categoryRepository.findAll();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Kategorija::getNaziv)
                .containsExactlyInAnyOrder("Sightseeing", "Food & Dining");
    }

    @Test
    void save_ShouldPersistNewCategory() {
        // Given
        Kategorija newCategory = Kategorija.builder()
                .naziv("Adventure")
                .opis("Outdoor and adventure activities")
                .build();

        // When
        Kategorija saved = categoryRepository.save(newCategory);

        // Then
        assertThat(saved.getKategorijaId()).isNotNull();
        assertThat(categoryRepository.findById(saved.getKategorijaId())).isPresent();
    }

    @Test
    void delete_ShouldRemoveCategory() {
        // Given
        Integer categoryId = testCategory1.getKategorijaId();

        // When
        categoryRepository.deleteById(categoryId);
        entityManager.flush();

        // Then
        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }
}
