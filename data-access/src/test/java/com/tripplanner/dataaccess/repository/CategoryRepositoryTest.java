package com.tripplanner.dataaccess.repository;

import com.tripplanner.dataaccess.TestDataAccessApplication;
import com.tripplanner.domain.entity.Kategorija;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ContextConfiguration(classes = TestDataAccessApplication.class)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Kategorija testCategory1;
    private Kategorija testCategory2;

    @BeforeEach
    void setUp() {
        
        testCategory1 = Kategorija.builder()
                .naziv("Sightseeing")
                .opis("Tourist attractions and landmarks")
                .build();

        testCategory2 = Kategorija.builder()
                .naziv("Food & Dining")
                .opis("Restaurants and culinary experiences")
                .build();

        
        entityManager.persist(testCategory1);
        entityManager.persist(testCategory2);
        entityManager.flush();
    }

    @Test
    void findById_WithExistingId_ShouldReturnCategory() {
        
        Optional<Kategorija> result = categoryRepository.findById(testCategory1.getKategorijaId());

        
        assertThat(result).isPresent();
        assertThat(result.get().getNaziv()).isEqualTo("Sightseeing");
        assertThat(result.get().getOpis()).isEqualTo("Tourist attractions and landmarks");
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        
        Optional<Kategorija> result = categoryRepository.findById(99999);

        
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllCategories() {
        
        var results = categoryRepository.findAll();

        
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Kategorija::getNaziv)
                .containsExactlyInAnyOrder("Sightseeing", "Food & Dining");
    }

    @Test
    void save_ShouldPersistNewCategory() {
        
        Kategorija newCategory = Kategorija.builder()
                .naziv("Adventure")
                .opis("Outdoor and adventure activities")
                .build();

        
        Kategorija saved = categoryRepository.save(newCategory);

        
        assertThat(saved.getKategorijaId()).isNotNull();
        assertThat(categoryRepository.findById(saved.getKategorijaId())).isPresent();
    }

    @Test
    void delete_ShouldRemoveCategory() {
        
        Integer categoryId = testCategory1.getKategorijaId();

        
        categoryRepository.deleteById(categoryId);
        entityManager.flush();

        
        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    @Test
    void update_ShouldModifyExistingCategory() {
        
        testCategory1.setNaziv("Updated Sightseeing");
        testCategory1.setOpis("Updated description");

        
        Kategorija updated = categoryRepository.save(testCategory1);
        entityManager.flush();

        
        assertThat(updated.getKategorijaId()).isEqualTo(testCategory1.getKategorijaId());
        assertThat(updated.getNaziv()).isEqualTo("Updated Sightseeing");
        assertThat(updated.getOpis()).isEqualTo("Updated description");
    }

    @Test
    void save_WithDuplicateName_ShouldAllowDuplicates() {
        
        Kategorija duplicateNameCategory = Kategorija.builder()
                .naziv("Sightseeing")
                .opis("Another sightseeing category")
                .build();

        
        Kategorija saved = categoryRepository.save(duplicateNameCategory);
        entityManager.flush();

        
        assertThat(saved.getKategorijaId()).isNotNull();
        assertThat(saved.getKategorijaId()).isNotEqualTo(testCategory1.getKategorijaId());
        assertThat(categoryRepository.findAll()).hasSize(3);
    }

    @Test
    void findAll_WithNoCategories_ShouldReturnEmptyList() {
        
        categoryRepository.deleteAll();
        entityManager.flush();

        
        var results = categoryRepository.findAll();

        
        assertThat(results).isEmpty();
    }

    @Test
    void save_WithNullName_ShouldPersist() {
        
        Kategorija categoryWithNullName = Kategorija.builder()
                .naziv(null)
                .opis("Category without name")
                .build();

        
        Kategorija saved = categoryRepository.save(categoryWithNullName);
        entityManager.flush();

        
        assertThat(saved.getKategorijaId()).isNotNull();
        assertThat(saved.getNaziv()).isNull();
        assertThat(saved.getOpis()).isEqualTo("Category without name");
    }

    @Test
    void save_WithNullDescription_ShouldPersist() {
        
        Kategorija categoryWithNullDescription = Kategorija.builder()
                .naziv("No Description Category")
                .opis(null)
                .build();

        
        Kategorija saved = categoryRepository.save(categoryWithNullDescription);
        entityManager.flush();

        
        assertThat(saved.getKategorijaId()).isNotNull();
        assertThat(saved.getNaziv()).isEqualTo("No Description Category");
        assertThat(saved.getOpis()).isNull();
    }

    @Test
    void save_WithEmptyName_ShouldPersist() {
        
        Kategorija categoryWithEmptyName = Kategorija.builder()
                .naziv("")
                .opis("Category with empty name")
                .build();

        
        Kategorija saved = categoryRepository.save(categoryWithEmptyName);
        entityManager.flush();

        
        assertThat(saved.getKategorijaId()).isNotNull();
        assertThat(saved.getNaziv()).isEmpty();
    }

    @Test
    void deleteById_WithNonExistentId_ShouldNotThrowException() {
        
        Integer nonExistentId = 99999;

        
        categoryRepository.deleteById(nonExistentId);
        entityManager.flush();
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        
        long count = categoryRepository.count();

        
        assertThat(count).isEqualTo(2);
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        
        boolean exists = categoryRepository.existsById(testCategory1.getKategorijaId());

        
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        
        boolean exists = categoryRepository.existsById(99999);

        
        assertThat(exists).isFalse();
    }
}
