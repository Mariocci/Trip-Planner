package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Aktivnost;
import com.tripplanner.domain.entity.Kategorija;
import com.tripplanner.domain.entity.Lokacija;
import com.tripplanner.domain.entity.Putovanje;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ActivityRepository}.
 * <p>
 * Uses @DataJpaTest to configure an in-memory H2 database for testing
 * repository operations without requiring a full application context.
 * </p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ActivityRepositoryTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Putovanje testTrip1;
    private Putovanje testTrip2;
    private Lokacija testLocation;
    private Lokacija testLocation2;
    private Kategorija testCategory1;
    private Kategorija testCategory2;
    private Aktivnost activity1;
    private Aktivnost activity2;
    private Aktivnost activity3;

    @BeforeEach
    void setUp() {
        // Create test locations
        testLocation = Lokacija.builder()
                .naziv("Eiffel Tower")
                .adresa("Champ de Mars")
                .grad("Paris")
                .drzava("France")
                .build();
        entityManager.persist(testLocation);

        testLocation2 = Lokacija.builder()
                .naziv("Louvre Museum")
                .adresa("Rue de Rivoli")
                .grad("Paris")
                .drzava("France")
                .build();
        entityManager.persist(testLocation2);

        // Create test categories
        testCategory1 = Kategorija.builder()
                .naziv("Sightseeing")
                .opis("Tourist attractions")
                .build();
        entityManager.persist(testCategory1);

        testCategory2 = Kategorija.builder()
                .naziv("Culture")
                .opis("Cultural activities")
                .build();
        entityManager.persist(testCategory2);

        // Create test trips
        testTrip1 = Putovanje.builder()
                .naziv("Paris Trip")
                .opis("Exploring Paris")
                .datumPoc(LocalDate.of(2024, 6, 1))
                .datumKraj(LocalDate.of(2024, 6, 10))
                .ukTrosak(BigDecimal.valueOf(1500.00))
                .build();

        testTrip2 = Putovanje.builder()
                .naziv("Rome Trip")
                .opis("Exploring Rome")
                .datumPoc(LocalDate.of(2024, 7, 1))
                .datumKraj(LocalDate.of(2024, 7, 10))
                .ukTrosak(BigDecimal.valueOf(2000.00))
                .build();

        entityManager.persist(testTrip1);
        entityManager.persist(testTrip2);

        // Create test activities for trip1
        List<Kategorija> categories1 = new ArrayList<>();
        categories1.add(testCategory1);

        activity1 = Aktivnost.builder()
                .naziv("Visit Eiffel Tower")
                .opis("Morning visit")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 2, 9, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 2, 12, 0))
                .putovanje(testTrip1)
                .lokacija(testLocation)
                .categories(categories1)
                .build();

        List<Kategorija> categories2 = new ArrayList<>();
        categories2.add(testCategory1);
        categories2.add(testCategory2);

        activity2 = Aktivnost.builder()
                .naziv("Louvre Museum")
                .opis("Afternoon visit")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 3, 14, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 3, 18, 0))
                .putovanje(testTrip1)
                .lokacija(testLocation2)
                .categories(categories2)
                .build();

        activity3 = Aktivnost.builder()
                .naziv("Seine River Cruise")
                .opis("Evening cruise")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 1, 19, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 1, 21, 0))
                .putovanje(testTrip1)
                .lokacija(testLocation)
                .categories(new ArrayList<>())
                .build();

        entityManager.persist(activity1);
        entityManager.persist(activity2);
        entityManager.persist(activity3);
        entityManager.flush();
    }

    @Test
    void findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc_WithExistingTrip_ShouldReturnActivitiesOrderedByDateTime() {
        // When
        List<Aktivnost> results = activityRepository.findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(
                testTrip1.getPutovanjeId());

        // Then
        assertThat(results).hasSize(3);
        // Should be ordered by datumVrijemePoc ascending (earliest first)
        assertThat(results.get(0).getNaziv()).isEqualTo("Seine River Cruise"); // 2024-06-01 19:00
        assertThat(results.get(1).getNaziv()).isEqualTo("Visit Eiffel Tower"); // 2024-06-02 09:00
        assertThat(results.get(2).getNaziv()).isEqualTo("Louvre Museum"); // 2024-06-03 14:00
    }

    @Test
    void findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc_WithTripWithoutActivities_ShouldReturnEmptyList() {
        // When
        List<Aktivnost> results = activityRepository.findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(
                testTrip2.getPutovanjeId());

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc_WithNonExistingTrip_ShouldReturnEmptyList() {
        // When
        List<Aktivnost> results = activityRepository.findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(99999);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void findById_WithExistingId_ShouldReturnActivity() {
        // When
        var result = activityRepository.findById(activity1.getAktivnostId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNaziv()).isEqualTo("Visit Eiffel Tower");
    }

    @Test
    void save_ShouldPersistNewActivity() {
        // Given
        Aktivnost newActivity = Aktivnost.builder()
                .naziv("Arc de Triomphe")
                .opis("Visit monument")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 4, 10, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 4, 11, 30))
                .putovanje(testTrip1)
                .lokacija(testLocation)
                .categories(new ArrayList<>())
                .build();

        // When
        Aktivnost saved = activityRepository.save(newActivity);

        // Then
        assertThat(saved.getAktivnostId()).isNotNull();
        assertThat(activityRepository.findById(saved.getAktivnostId())).isPresent();
    }

    // ========== CRUD Operations Tests ==========

    @Test
    void save_WithAllFields_ShouldPersistActivity() {
        // Given
        List<Kategorija> categories = new ArrayList<>();
        categories.add(testCategory1);
        categories.add(testCategory2);

        Aktivnost newActivity = Aktivnost.builder()
                .naziv("Notre-Dame Cathedral")
                .opis("Visit historic cathedral")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 5, 10, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 5, 12, 0))
                .putovanje(testTrip1)
                .lokacija(testLocation)
                .categories(categories)
                .build();

        // When
        Aktivnost saved = activityRepository.save(newActivity);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Aktivnost> retrieved = activityRepository.findById(saved.getAktivnostId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getNaziv()).isEqualTo("Notre-Dame Cathedral");
        assertThat(retrieved.get().getOpis()).isEqualTo("Visit historic cathedral");
        assertThat(retrieved.get().getDatumVrijemePoc()).isEqualTo(LocalDateTime.of(2024, 6, 5, 10, 0));
        assertThat(retrieved.get().getDatumVrijemeKraj()).isEqualTo(LocalDateTime.of(2024, 6, 5, 12, 0));
    }

    @Test
    void findById_WithExistingId_ShouldReturnActivityWithAllFields() {
        // When
        Optional<Aktivnost> result = activityRepository.findById(activity1.getAktivnostId());

        // Then
        assertThat(result).isPresent();
        Aktivnost activity = result.get();
        assertThat(activity.getNaziv()).isEqualTo("Visit Eiffel Tower");
        assertThat(activity.getOpis()).isEqualTo("Morning visit");
        assertThat(activity.getDatumVrijemePoc()).isEqualTo(LocalDateTime.of(2024, 6, 2, 9, 0));
        assertThat(activity.getDatumVrijemeKraj()).isEqualTo(LocalDateTime.of(2024, 6, 2, 12, 0));
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        // When
        Optional<Aktivnost> result = activityRepository.findById(99999);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllActivities() {
        // When
        List<Aktivnost> results = activityRepository.findAll();

        // Then
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Aktivnost::getNaziv)
                .containsExactlyInAnyOrder("Visit Eiffel Tower", "Louvre Museum", "Seine River Cruise");
    }

    @Test
    void findAll_WithNoActivities_ShouldReturnEmptyList() {
        // Given
        activityRepository.deleteAll();
        entityManager.flush();

        // When
        List<Aktivnost> results = activityRepository.findAll();

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void delete_WithExistingActivity_ShouldRemoveActivity() {
        // Given
        Integer activityId = activity1.getAktivnostId();
        assertThat(activityRepository.findById(activityId)).isPresent();

        // When
        activityRepository.delete(activity1);
        entityManager.flush();

        // Then
        assertThat(activityRepository.findById(activityId)).isEmpty();
    }

    @Test
    void deleteById_WithExistingId_ShouldRemoveActivity() {
        // Given
        Integer activityId = activity2.getAktivnostId();
        assertThat(activityRepository.findById(activityId)).isPresent();

        // When
        activityRepository.deleteById(activityId);
        entityManager.flush();

        // Then
        assertThat(activityRepository.findById(activityId)).isEmpty();
    }

    @Test
    void update_ExistingActivity_ShouldPersistChanges() {
        // Given
        Integer activityId = activity1.getAktivnostId();
        Aktivnost activity = activityRepository.findById(activityId).orElseThrow();

        // When
        activity.setNaziv("Updated Eiffel Tower Visit");
        activity.setOpis("Updated description");
        activity.setDatumVrijemePoc(LocalDateTime.of(2024, 6, 2, 10, 0));
        activityRepository.save(activity);
        entityManager.flush();
        entityManager.clear();

        // Then
        Aktivnost updated = activityRepository.findById(activityId).orElseThrow();
        assertThat(updated.getNaziv()).isEqualTo("Updated Eiffel Tower Visit");
        assertThat(updated.getOpis()).isEqualTo("Updated description");
        assertThat(updated.getDatumVrijemePoc()).isEqualTo(LocalDateTime.of(2024, 6, 2, 10, 0));
    }

    // ========== Custom Query Tests ==========

    @Test
    void findByPutovanje_PutovanjeId_WithMultipleActivities_ShouldReturnAllActivitiesForTrip() {
        // When
        List<Aktivnost> results = activityRepository.findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(
                testTrip1.getPutovanjeId());

        // Then
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Aktivnost::getNaziv)
                .containsExactly("Seine River Cruise", "Visit Eiffel Tower", "Louvre Museum");
    }

    // ========== Entity Relationships Tests ==========

    @Test
    void save_WithTripRelationship_ShouldPersistRelationship() {
        // Given
        Aktivnost newActivity = Aktivnost.builder()
                .naziv("Test Activity")
                .opis("Test description")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 6, 10, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 6, 12, 0))
                .putovanje(testTrip2)
                .lokacija(testLocation)
                .categories(new ArrayList<>())
                .build();

        // When
        Aktivnost saved = activityRepository.save(newActivity);
        entityManager.flush();
        entityManager.clear();

        // Then
        Aktivnost retrieved = activityRepository.findById(saved.getAktivnostId()).orElseThrow();
        assertThat(retrieved.getPutovanje()).isNotNull();
        assertThat(retrieved.getPutovanje().getPutovanjeId()).isEqualTo(testTrip2.getPutovanjeId());
        assertThat(retrieved.getPutovanje().getNaziv()).isEqualTo("Rome Trip");
    }

    @Test
    void save_WithLocationRelationship_ShouldPersistRelationship() {
        // Given
        Aktivnost newActivity = Aktivnost.builder()
                .naziv("Test Activity")
                .opis("Test description")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 6, 10, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 6, 12, 0))
                .putovanje(testTrip1)
                .lokacija(testLocation2)
                .categories(new ArrayList<>())
                .build();

        // When
        Aktivnost saved = activityRepository.save(newActivity);
        entityManager.flush();
        entityManager.clear();

        // Then
        Aktivnost retrieved = activityRepository.findById(saved.getAktivnostId()).orElseThrow();
        assertThat(retrieved.getLokacija()).isNotNull();
        assertThat(retrieved.getLokacija().getLokacijaId()).isEqualTo(testLocation2.getLokacijaId());
        assertThat(retrieved.getLokacija().getNaziv()).isEqualTo("Louvre Museum");
    }

    @Test
    void save_WithCategoryRelationship_ShouldPersistRelationship() {
        // Given
        List<Kategorija> categories = new ArrayList<>();
        categories.add(testCategory1);

        Aktivnost newActivity = Aktivnost.builder()
                .naziv("Test Activity")
                .opis("Test description")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 6, 10, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 6, 12, 0))
                .putovanje(testTrip1)
                .lokacija(testLocation)
                .categories(categories)
                .build();

        // When
        Aktivnost saved = activityRepository.save(newActivity);
        entityManager.flush();
        entityManager.clear();

        // Then
        Aktivnost retrieved = activityRepository.findById(saved.getAktivnostId()).orElseThrow();
        assertThat(retrieved.getCategories()).isNotNull();
        assertThat(retrieved.getCategories()).hasSize(1);
        assertThat(retrieved.getCategories().get(0).getNaziv()).isEqualTo("Sightseeing");
    }

    @Test
    void save_WithMultipleCategoryRelationships_ShouldPersistAllRelationships() {
        // Given
        List<Kategorija> categories = new ArrayList<>();
        categories.add(testCategory1);
        categories.add(testCategory2);

        Aktivnost newActivity = Aktivnost.builder()
                .naziv("Test Activity")
                .opis("Test description")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 6, 10, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 6, 12, 0))
                .putovanje(testTrip1)
                .lokacija(testLocation)
                .categories(categories)
                .build();

        // When
        Aktivnost saved = activityRepository.save(newActivity);
        entityManager.flush();
        entityManager.clear();

        // Then
        Aktivnost retrieved = activityRepository.findById(saved.getAktivnostId()).orElseThrow();
        assertThat(retrieved.getCategories()).isNotNull();
        assertThat(retrieved.getCategories()).hasSize(2);
        assertThat(retrieved.getCategories()).extracting(Kategorija::getNaziv)
                .containsExactlyInAnyOrder("Sightseeing", "Culture");
    }

    @Test
    void findById_ShouldLoadAllRelationships() {
        // When
        Aktivnost retrieved = activityRepository.findById(activity2.getAktivnostId()).orElseThrow();

        // Then
        assertThat(retrieved.getPutovanje()).isNotNull();
        assertThat(retrieved.getPutovanje().getNaziv()).isEqualTo("Paris Trip");
        assertThat(retrieved.getLokacija()).isNotNull();
        assertThat(retrieved.getLokacija().getNaziv()).isEqualTo("Louvre Museum");
        assertThat(retrieved.getCategories()).isNotNull();
        assertThat(retrieved.getCategories()).hasSize(2);
    }

    @Test
    void update_CategoryRelationship_ShouldPersistChanges() {
        // Given
        Aktivnost activity = activityRepository.findById(activity3.getAktivnostId()).orElseThrow();
        assertThat(activity.getCategories()).isEmpty();

        // When
        List<Kategorija> newCategories = new ArrayList<>();
        newCategories.add(testCategory1);
        activity.setCategories(newCategories);
        activityRepository.save(activity);
        entityManager.flush();
        entityManager.clear();

        // Then
        Aktivnost updated = activityRepository.findById(activity3.getAktivnostId()).orElseThrow();
        assertThat(updated.getCategories()).hasSize(1);
        assertThat(updated.getCategories().get(0).getNaziv()).isEqualTo("Sightseeing");
    }

    // ========== Edge Cases Tests ==========

    @Test
    void findByPutovanje_PutovanjeId_WithNullId_ShouldReturnEmptyList() {
        // When
        List<Aktivnost> results = activityRepository.findByPutovanje_PutovanjeIdOrderByDatumVrijemePoc(null);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void save_WithNullOptionalFields_ShouldPersist() {
        // Given
        Aktivnost newActivity = Aktivnost.builder()
                .naziv("Minimal Activity")
                .opis(null) // Optional field
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 6, 10, 0))
                .datumVrijemeKraj(null) // Optional field
                .putovanje(testTrip1)
                .lokacija(testLocation)
                .categories(new ArrayList<>())
                .build();

        // When
        Aktivnost saved = activityRepository.save(newActivity);
        entityManager.flush();
        entityManager.clear();

        // Then
        Aktivnost retrieved = activityRepository.findById(saved.getAktivnostId()).orElseThrow();
        assertThat(retrieved.getNaziv()).isEqualTo("Minimal Activity");
        assertThat(retrieved.getOpis()).isNull();
        assertThat(retrieved.getDatumVrijemeKraj()).isNull();
    }

    @Test
    void save_WithEmptyCategoryList_ShouldPersist() {
        // Given
        Aktivnost newActivity = Aktivnost.builder()
                .naziv("Activity Without Categories")
                .opis("Test description")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 6, 10, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 6, 12, 0))
                .putovanje(testTrip1)
                .lokacija(testLocation)
                .categories(new ArrayList<>())
                .build();

        // When
        Aktivnost saved = activityRepository.save(newActivity);
        entityManager.flush();
        entityManager.clear();

        // Then
        Aktivnost retrieved = activityRepository.findById(saved.getAktivnostId()).orElseThrow();
        assertThat(retrieved.getCategories()).isNotNull();
        assertThat(retrieved.getCategories()).isEmpty();
    }

    @Test
    void delete_NonExistentActivity_ShouldNotThrowException() {
        // Given
        Aktivnost nonExistentActivity = Aktivnost.builder()
                .aktivnostId(99999)
                .naziv("Non-existent")
                .build();

        // When/Then - should not throw exception
        activityRepository.delete(nonExistentActivity);
        entityManager.flush();
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When
        long count = activityRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        // When
        boolean exists = activityRepository.existsById(activity1.getAktivnostId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WithNonExistentId_ShouldReturnFalse() {
        // When
        boolean exists = activityRepository.existsById(99999);

        // Then
        assertThat(exists).isFalse();
    }
}
