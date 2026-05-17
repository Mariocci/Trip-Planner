package com.tripplanner.dataaccess.repository;

import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Trosak;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ExpenseRepository}.
 * <p>
 * Uses @DataJpaTest to configure an in-memory H2 database for testing
 * repository operations without requiring a full application context.
 * </p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Putovanje testTrip1;
    private Putovanje testTrip2;
    private Trosak expense1;
    private Trosak expense2;
    private Trosak expense3;

    @BeforeEach
    void setUp() {
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

        // Create test expenses for trip1
        expense1 = Trosak.builder()
                .iznos(BigDecimal.valueOf(150.00))
                .opis("Hotel accommodation")
                .datum(LocalDate.of(2024, 6, 1))
                .putovanje(testTrip1)
                .build();

        expense2 = Trosak.builder()
                .iznos(BigDecimal.valueOf(75.50))
                .opis("Restaurant dinner")
                .datum(LocalDate.of(2024, 6, 2))
                .putovanje(testTrip1)
                .build();

        expense3 = Trosak.builder()
                .iznos(BigDecimal.valueOf(25.00))
                .opis("Museum tickets")
                .datum(LocalDate.of(2024, 6, 3))
                .putovanje(testTrip1)
                .build();

        entityManager.persist(expense1);
        entityManager.persist(expense2);
        entityManager.persist(expense3);
        entityManager.flush();
    }

    @Test
    void findByPutovanje_PutovanjeId_WithExistingTrip_ShouldReturnAllExpenses() {
        // When
        List<Trosak> results = expenseRepository.findByPutovanje_PutovanjeId(testTrip1.getPutovanjeId());

        // Then
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Trosak::getOpis)
                .containsExactlyInAnyOrder("Hotel accommodation", "Restaurant dinner", "Museum tickets");
    }

    @Test
    void findByPutovanje_PutovanjeId_WithTripWithoutExpenses_ShouldReturnEmptyList() {
        // When
        List<Trosak> results = expenseRepository.findByPutovanje_PutovanjeId(testTrip2.getPutovanjeId());

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void findByPutovanje_PutovanjeId_WithNonExistingTrip_ShouldReturnEmptyList() {
        // When
        List<Trosak> results = expenseRepository.findByPutovanje_PutovanjeId(99999);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void sumByPutovanjeId_WithExistingExpenses_ShouldReturnCorrectSum() {
        // When
        BigDecimal sum = expenseRepository.sumByPutovanjeId(testTrip1.getPutovanjeId());

        // Then
        // 150.00 + 75.50 + 25.00 = 250.50
        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(250.50));
    }

    @Test
    void sumByPutovanjeId_WithNoExpenses_ShouldReturnNull() {
        // When
        BigDecimal sum = expenseRepository.sumByPutovanjeId(testTrip2.getPutovanjeId());

        // Then
        assertThat(sum).isNull();
    }

    @Test
    void sumByPutovanjeId_WithNonExistingTrip_ShouldReturnNull() {
        // When
        BigDecimal sum = expenseRepository.sumByPutovanjeId(99999);

        // Then
        assertThat(sum).isNull();
    }

    @Test
    void findById_WithExistingId_ShouldReturnExpense() {
        // When
        var result = expenseRepository.findById(expense1.getTrosakId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOpis()).isEqualTo("Hotel accommodation");
        assertThat(result.get().getIznos()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
    }

    @Test
    void save_ShouldPersistNewExpense() {
        // Given
        Trosak newExpense = Trosak.builder()
                .iznos(BigDecimal.valueOf(50.00))
                .opis("Transportation")
                .datum(LocalDate.of(2024, 6, 4))
                .putovanje(testTrip1)
                .build();

        // When
        Trosak saved = expenseRepository.save(newExpense);

        // Then
        assertThat(saved.getTrosakId()).isNotNull();
        assertThat(expenseRepository.findById(saved.getTrosakId())).isPresent();
    }

    @Test
    void save_ShouldUpdateExistingExpense() {
        // Given
        expense1.setIznos(BigDecimal.valueOf(200.00));
        expense1.setOpis("Updated hotel accommodation");

        // When
        Trosak updated = expenseRepository.save(expense1);

        // Then
        assertThat(updated.getTrosakId()).isEqualTo(expense1.getTrosakId());
        assertThat(updated.getIznos()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
        assertThat(updated.getOpis()).isEqualTo("Updated hotel accommodation");
    }

    @Test
    void findAll_ShouldReturnAllExpenses() {
        // When
        List<Trosak> results = expenseRepository.findAll();

        // Then
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Trosak::getOpis)
                .containsExactlyInAnyOrder("Hotel accommodation", "Restaurant dinner", "Museum tickets");
    }

    @Test
    void delete_WithExistingExpense_ShouldRemoveExpense() {
        // Given
        Integer expenseId = expense1.getTrosakId();

        // When
        expenseRepository.delete(expense1);
        entityManager.flush();

        // Then
        assertThat(expenseRepository.findById(expenseId)).isEmpty();
        assertThat(expenseRepository.findAll()).hasSize(2);
    }

    @Test
    void deleteById_WithExistingId_ShouldRemoveExpense() {
        // Given
        Integer expenseId = expense2.getTrosakId();

        // When
        expenseRepository.deleteById(expenseId);
        entityManager.flush();

        // Then
        assertThat(expenseRepository.findById(expenseId)).isEmpty();
        assertThat(expenseRepository.findAll()).hasSize(2);
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        var result = expenseRepository.findById(99999);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void save_WithTripRelationship_ShouldPersistRelationship() {
        // Given
        Trosak newExpense = Trosak.builder()
                .iznos(BigDecimal.valueOf(100.00))
                .opis("Shopping")
                .datum(LocalDate.of(2024, 6, 5))
                .putovanje(testTrip1)
                .build();

        // When
        Trosak saved = expenseRepository.save(newExpense);
        entityManager.flush();
        entityManager.clear();

        // Then
        Trosak retrieved = expenseRepository.findById(saved.getTrosakId()).orElseThrow();
        assertThat(retrieved.getPutovanje()).isNotNull();
        assertThat(retrieved.getPutovanje().getPutovanjeId()).isEqualTo(testTrip1.getPutovanjeId());
        assertThat(retrieved.getPutovanje().getNaziv()).isEqualTo("Paris Trip");
    }
}
