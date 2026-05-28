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


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LocationRepositoryTest {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Lokacija testLocation1;
    private Lokacija testLocation2;

    @BeforeEach
    void setUp() {
        
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

        
        entityManager.persist(testLocation1);
        entityManager.persist(testLocation2);
        entityManager.flush();
    }

    

    @Test
    void save_WithValidLocation_ShouldPersistNewLocation() {
        
        Lokacija newLocation = Lokacija.builder()
                .naziv("Big Ben")
                .adresa("Westminster")
                .grad("London")
                .drzava("United Kingdom")
                .build();

        
        Lokacija saved = locationRepository.save(newLocation);
        entityManager.flush();
        entityManager.clear();

        
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
        
        Lokacija minimalLocation = Lokacija.builder()
                .naziv("Minimal Location")
                .build();

        
        Lokacija saved = locationRepository.save(minimalLocation);
        entityManager.flush();

        
        assertThat(saved.getLokacijaId()).isNotNull();
        assertThat(saved.getNaziv()).isEqualTo("Minimal Location");
        assertThat(saved.getAdresa()).isNull();
        assertThat(saved.getGrad()).isNull();
        assertThat(saved.getDrzava()).isNull();
    }

    @Test
    void findById_WithExistingId_ShouldReturnLocation() {
        
        Optional<Lokacija> result = locationRepository.findById(testLocation1.getLokacijaId());

        
        assertThat(result).isPresent();
        assertThat(result.get().getNaziv()).isEqualTo("Eiffel Tower");
        assertThat(result.get().getAdresa()).isEqualTo("Champ de Mars, 5 Avenue Anatole France");
        assertThat(result.get().getGrad()).isEqualTo("Paris");
        assertThat(result.get().getDrzava()).isEqualTo("France");
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        
        Optional<Lokacija> result = locationRepository.findById(99999);

        
        assertThat(result).isEmpty();
    }

    @Test
    void findById_WithNullId_ShouldThrowException() {
        
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.InvalidDataAccessApiUsageException.class,
                () -> locationRepository.findById(null)
        );
    }

    @Test
    void findAll_WithMultipleLocations_ShouldReturnAllLocations() {
        
        List<Lokacija> results = locationRepository.findAll();

        
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Lokacija::getNaziv)
                .containsExactlyInAnyOrder("Eiffel Tower", "Colosseum");
    }

    @Test
    void findAll_WithEmptyDatabase_ShouldReturnEmptyList() {
        
        locationRepository.deleteAll();
        entityManager.flush();

        
        List<Lokacija> results = locationRepository.findAll();

        
        assertThat(results).isEmpty();
    }

    @Test
    void update_WithExistingLocation_ShouldUpdateFields() {
        
        Integer locationId = testLocation1.getLokacijaId();
        
        
        testLocation1.setNaziv("Tour Eiffel");
        testLocation1.setAdresa("5 Avenue Anatole France");
        testLocation1.setGrad("Paris 7e");
        locationRepository.save(testLocation1);
        entityManager.flush();
        entityManager.clear();

        
        Optional<Lokacija> updated = locationRepository.findById(locationId);
        assertThat(updated).isPresent();
        assertThat(updated.get().getNaziv()).isEqualTo("Tour Eiffel");
        assertThat(updated.get().getAdresa()).isEqualTo("5 Avenue Anatole France");
        assertThat(updated.get().getGrad()).isEqualTo("Paris 7e");
        assertThat(updated.get().getDrzava()).isEqualTo("France");
    }

    @Test
    void delete_WithExistingLocation_ShouldRemoveLocation() {
        
        Integer locationId = testLocation1.getLokacijaId();
        assertThat(locationRepository.findById(locationId)).isPresent();

        
        locationRepository.deleteById(locationId);
        entityManager.flush();

        
        assertThat(locationRepository.findById(locationId)).isEmpty();
        assertThat(locationRepository.findAll()).hasSize(1);
    }

    @Test
    void delete_WithEntity_ShouldRemoveLocation() {
        
        Integer locationId = testLocation1.getLokacijaId();

        
        locationRepository.delete(testLocation1);
        entityManager.flush();

        
        assertThat(locationRepository.findById(locationId)).isEmpty();
    }

    @Test
    void deleteAll_ShouldRemoveAllLocations() {
        
        assertThat(locationRepository.findAll()).hasSize(2);

        
        locationRepository.deleteAll();
        entityManager.flush();

        
        assertThat(locationRepository.findAll()).isEmpty();
    }

    

    @Test
    void save_WithNullFields_ShouldPersistWithNulls() {
        
        Lokacija locationWithNulls = Lokacija.builder()
                .naziv("Location with nulls")
                .adresa(null)
                .grad(null)
                .drzava(null)
                .build();

        
        Lokacija saved = locationRepository.save(locationWithNulls);
        entityManager.flush();
        entityManager.clear();

        
        Optional<Lokacija> retrieved = locationRepository.findById(saved.getLokacijaId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getNaziv()).isEqualTo("Location with nulls");
        assertThat(retrieved.get().getAdresa()).isNull();
        assertThat(retrieved.get().getGrad()).isNull();
        assertThat(retrieved.get().getDrzava()).isNull();
    }

    @Test
    void save_WithEmptyStrings_ShouldPersistEmptyStrings() {
        
        Lokacija locationWithEmptyStrings = Lokacija.builder()
                .naziv("")
                .adresa("")
                .grad("")
                .drzava("")
                .build();

        
        Lokacija saved = locationRepository.save(locationWithEmptyStrings);
        entityManager.flush();

        
        assertThat(saved.getLokacijaId()).isNotNull();
        assertThat(saved.getNaziv()).isEmpty();
        assertThat(saved.getAdresa()).isEmpty();
        assertThat(saved.getGrad()).isEmpty();
        assertThat(saved.getDrzava()).isEmpty();
    }

    @Test
    void save_WithLongStrings_ShouldPersistWithinLimits() {
        
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

        
        Lokacija saved = locationRepository.save(locationWithLongStrings);
        entityManager.flush();
        entityManager.clear();

        
        Optional<Lokacija> retrieved = locationRepository.findById(saved.getLokacijaId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getNaziv()).hasSize(255);
        assertThat(retrieved.get().getAdresa()).hasSize(255);
        assertThat(retrieved.get().getGrad()).hasSize(100);
        assertThat(retrieved.get().getDrzava()).hasSize(100);
    }

    @Test
    void count_WithMultipleLocations_ShouldReturnCorrectCount() {
        
        long count = locationRepository.count();

        
        assertThat(count).isEqualTo(2);
    }

    @Test
    void count_WithEmptyDatabase_ShouldReturnZero() {
        
        locationRepository.deleteAll();
        entityManager.flush();

        
        long count = locationRepository.count();

        
        assertThat(count).isZero();
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        
        boolean exists = locationRepository.existsById(testLocation1.getLokacijaId());

        
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        
        boolean exists = locationRepository.existsById(99999);

        
        assertThat(exists).isFalse();
    }
}
