package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Lokacija;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LocationRepository}.
 * <p>
 * Uses @DataJpaTest to configure an in-memory H2 database for testing
 * repository operations without requiring a full application context.
 * </p>
 * <p>
 * Tests cover:
 * - CRUD operations (save, findById, findAll, delete)
 * - Edge cases (empty results, non-existent IDs, null values)
 * - Update operations
 * </p>
 * <p>
 * Note: Google Places ID uniqueness testing is not implemented as the Lokacija entity
 * does not currently have a Google Places ID field. This should be added when
 * Google Places API integration is implemented.
 * </p>
 * 
 * @see LocationRepository
 * @see Lokacija
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class LocationRepositoryTest {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Lokacija testLocation1;
    private Lokacija testLocation2;

    @BeforeEach
    void setUp() {
        // Create test locations
        testLocation1 = Lokacija.builder()
                .naziv("Eiffel Tower")
                .adresa("Champ de Mars, 5 Avenue Anatole France")
                .grad("Paris")
                .drzava("France")
                .build();

        testLocation2 = Lokacija.builder()
                .naziv("Colosseum")
                .adresa("Piazza del Colosseo, 1")
                .grad("Rome")
                .drzava("Italy")
                .build();

        // Persist test locations
        entityManager.persist(testLocation1);
        entityManager.persist(testLocation2);
        entityManager.flush();
    }

    // ========== CRUD Operations Tests ==========

    @Test
    void save_WithValidLocation_ShouldPersistNewLocation() {
        // Given
        Lokacija newLocation = Lokacija.builder()
                .naziv("Big Ben")
                .adresa("Westminster")
                .grad("London")
                .drzava("United Kingdom")
                .build();

        // When
        Lokacija saved = locationRepository.save(newLocation);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(saved.getLokacijaId()).isNotNull();
        Optional<Lokacija> retrieved = locationRepository.findById(saved.getLokacijaId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getNaziv()).isEqualTo("Big Ben");
        assertThat(retrieved.get().getAdresa()).isEqualTo("Westminster");
        assertThat(retrieved.get().getGrad()).isEqualTo("London");
        assertThat(retrieved.get().getDrzava()).isEqualTo("United Kingdom");
    }

    @Test
    void save_WithMinimalData_ShouldPersistLocation() {
        // Given - Location with only name
        Lokacija minimalLocation = Lokacija.builder()
                .naziv("Minimal Location")
                .build();

        // When
        Lokacija saved = locationRepository.save(minimalLocation);
        entityManager.flush();

        // Then
        assertThat(saved.getLokacijaId()).isNotNull();
        assertThat(saved.getNaziv()).isEqualTo("Minimal Location");
        assertThat(saved.getAdresa()).isNull();
        assertThat(saved.getGrad()).isNull();
        assertThat(saved.getDrzava()).isNull();
    }

    @Test
    void findById_WithExistingId_ShouldReturnLocation() {
        // When
        Optional<Lokacija> result = locationRepository.findById(testLocation1.getLokacijaId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNaziv()).isEqualTo("Eiffel Tower");
        assertThat(result.get().getAdresa()).isEqualTo("Champ de Mars, 5 Avenue Anatole France");
        assertThat(result.get().getGrad()).isEqualTo("Paris");
        assertThat(result.get().getDrzava()).isEqualTo("France");
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<Lokacija> result = locationRepository.findById(99999);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findById_WithNullId_ShouldThrowException() {
        // When/Then - Spring Data JPA throws IllegalArgumentException for null IDs
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.InvalidDataAccessApiUsageException.class,
                () -> locationRepository.findById(null)
        );
    }

    @Test
    void findAll_WithMultipleLocations_ShouldReturnAllLocations() {
        // When
        List<Lokacija> results = locationRepository.findAll();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Lokacija::getNaziv)
                .containsExactlyInAnyOrder("Eiffel Tower", "Colosseum");
    }

    @Test
    void findAll_WithEmptyDatabase_ShouldReturnEmptyList() {
        // Given - Clear all locations
        locationRepository.deleteAll();
        entityManager.flush();

        // When
        List<Lokacija> results = locationRepository.findAll();

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void update_WithExistingLocation_ShouldUpdateFields() {
        // Given
        Integer locationId = testLocation1.getLokacijaId();
        
        // When - Update location fields
        testLocation1.setNaziv("Tour Eiffel");
        testLocation1.setAdresa("5 Avenue Anatole France");
        testLocation1.setGrad("Paris 7e");
        locationRepository.save(testLocation1);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Lokacija> updated = locationRepository.findById(locationId);
        assertThat(updated).isPresent();
        assertThat(updated.get().getNaziv()).isEqualTo("Tour Eiffel");
        assertThat(updated.get().getAdresa()).isEqualTo("5 Avenue Anatole France");
        assertThat(updated.get().getGrad()).isEqualTo("Paris 7e");
        assertThat(updated.get().getDrzava()).isEqualTo("France");
    }

    @Test
    void delete_WithExistingLocation_ShouldRemoveLocation() {
        // Given
        Integer locationId = testLocation1.getLokacijaId();
        assertThat(locationRepository.findById(locationId)).isPresent();

        // When
        locationRepository.deleteById(locationId);
        entityManager.flush();

        // Then
        assertThat(locationRepository.findById(locationId)).isEmpty();
        assertThat(locationRepository.findAll()).hasSize(1);
    }

    @Test
    void delete_WithEntity_ShouldRemoveLocation() {
        // Given
        Integer locationId = testLocation1.getLokacijaId();

        // When
        locationRepository.delete(testLocation1);
        entityManager.flush();

        // Then
        assertThat(locationRepository.findById(locationId)).isEmpty();
    }

    @Test
    void deleteAll_ShouldRemoveAllLocations() {
        // Given
        assertThat(locationRepository.findAll()).hasSize(2);

        // When
        locationRepository.deleteAll();
        entityManager.flush();

        // Then
        assertThat(locationRepository.findAll()).isEmpty();
    }

    // ========== Edge Cases Tests ==========

    @Test
    void save_WithNullFields_ShouldPersistWithNulls() {
        // Given
        Lokacija locationWithNulls = Lokacija.builder()
                .naziv("Location with nulls")
                .adresa(null)
                .grad(null)
                .drzava(null)
                .build();

        // When
        Lokacija saved = locationRepository.save(locationWithNulls);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Lokacija> retrieved = locationRepository.findById(saved.getLokacijaId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getNaziv()).isEqualTo("Location with nulls");
        assertThat(retrieved.get().getAdresa()).isNull();
        assertThat(retrieved.get().getGrad()).isNull();
        assertThat(retrieved.get().getDrzava()).isNull();
    }

    @Test
    void save_WithEmptyStrings_ShouldPersistEmptyStrings() {
        // Given
        Lokacija locationWithEmptyStrings = Lokacija.builder()
                .naziv("")
                .adresa("")
                .grad("")
                .drzava("")
                .build();

        // When
        Lokacija saved = locationRepository.save(locationWithEmptyStrings);
        entityManager.flush();

        // Then
        assertThat(saved.getLokacijaId()).isNotNull();
        assertThat(saved.getNaziv()).isEmpty();
        assertThat(saved.getAdresa()).isEmpty();
        assertThat(saved.getGrad()).isEmpty();
        assertThat(saved.getDrzava()).isEmpty();
    }

    @Test
    void save_WithLongStrings_ShouldPersistWithinLimits() {
        // Given - Test with strings at field length limits
        String longName = "A".repeat(255);
        String longAddress = "B".repeat(255);
        String longCity = "C".repeat(100);
        String longCountry = "D".repeat(100);

        Lokacija locationWithLongStrings = Lokacija.builder()
                .naziv(longName)
                .adresa(longAddress)
                .grad(longCity)
                .drzava(longCountry)
                .build();

        // When
        Lokacija saved = locationRepository.save(locationWithLongStrings);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Lokacija> retrieved = locationRepository.findById(saved.getLokacijaId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getNaziv()).hasSize(255);
        assertThat(retrieved.get().getAdresa()).hasSize(255);
        assertThat(retrieved.get().getGrad()).hasSize(100);
        assertThat(retrieved.get().getDrzava()).hasSize(100);
    }

    @Test
    void count_WithMultipleLocations_ShouldReturnCorrectCount() {
        // When
        long count = locationRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void count_WithEmptyDatabase_ShouldReturnZero() {
        // Given
        locationRepository.deleteAll();
        entityManager.flush();

        // When
        long count = locationRepository.count();

        // Then
        assertThat(count).isZero();
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        // When
        boolean exists = locationRepository.existsById(testLocation1.getLokacijaId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        // When
        boolean exists = locationRepository.existsById(99999);

        // Then
        assertThat(exists).isFalse();
    }
}
