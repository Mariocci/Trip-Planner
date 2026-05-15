package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Lokacija;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LocationRepository}.
 * <p>
 * Uses @DataJpaTest to configure an in-memory H2 database for testing
 * repository operations without requiring a full application context.
 * </p>
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

    @Test
    void findById_WithExistingId_ShouldReturnLocation() {
        // When
        Optional<Lokacija> result = locationRepository.findById(testLocation1.getLokacijaId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNaziv()).isEqualTo("Eiffel Tower");
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
    void findAll_ShouldReturnAllLocations() {
        // When
        var results = locationRepository.findAll();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Lokacija::getNaziv)
                .containsExactlyInAnyOrder("Eiffel Tower", "Colosseum");
    }

    @Test
    void save_ShouldPersistNewLocation() {
        // Given
        Lokacija newLocation = Lokacija.builder()
                .naziv("Big Ben")
                .adresa("Westminster")
                .grad("London")
                .drzava("United Kingdom")
                .build();

        // When
        Lokacija saved = locationRepository.save(newLocation);

        // Then
        assertThat(saved.getLokacijaId()).isNotNull();
        assertThat(locationRepository.findById(saved.getLokacijaId())).isPresent();
    }

    @Test
    void delete_ShouldRemoveLocation() {
        // Given
        Integer locationId = testLocation1.getLokacijaId();

        // When
        locationRepository.deleteById(locationId);
        entityManager.flush();

        // Then
        assertThat(locationRepository.findById(locationId)).isEmpty();
    }
}
