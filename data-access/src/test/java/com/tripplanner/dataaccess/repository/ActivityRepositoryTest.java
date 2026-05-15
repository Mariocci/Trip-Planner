package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Aktivnost;
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
import java.util.List;

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
    private Aktivnost activity1;
    private Aktivnost activity2;
    private Aktivnost activity3;

    @BeforeEach
    void setUp() {
        // Create test location
        testLocation = Lokacija.builder()
                .naziv("Eiffel Tower")
                .adresa("Champ de Mars")
                .grad("Paris")
                .drzava("France")
                .build();
        entityManager.persist(testLocation);

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
        activity1 = Aktivnost.builder()
                .naziv("Visit Eiffel Tower")
                .opis("Morning visit")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 2, 9, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 2, 12, 0))
                .putovanje(testTrip1)
                .lokacija(testLocation)
                .build();

        activity2 = Aktivnost.builder()
                .naziv("Louvre Museum")
                .opis("Afternoon visit")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 3, 14, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 3, 18, 0))
                .putovanje(testTrip1)
                .lokacija(testLocation)
                .build();

        activity3 = Aktivnost.builder()
                .naziv("Seine River Cruise")
                .opis("Evening cruise")
                .datumVrijemePoc(LocalDateTime.of(2024, 6, 1, 19, 0))
                .datumVrijemeKraj(LocalDateTime.of(2024, 6, 1, 21, 0))
                .putovanje(testTrip1)
                .lokacija(testLocation)
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
                .build();

        // When
        Aktivnost saved = activityRepository.save(newActivity);

        // Then
        assertThat(saved.getAktivnostId()).isNotNull();
        assertThat(activityRepository.findById(saved.getAktivnostId())).isPresent();
    }
}
