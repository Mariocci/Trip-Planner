package com.tripplanner.business.integration;

import com.tripplanner.business.TestBusinessApplication;
import com.tripplanner.business.service.ExpenseService;
import com.tripplanner.business.service.TripService;
import com.tripplanner.dataaccess.repository.ExpenseRepository;
import com.tripplanner.dataaccess.repository.ParticipantRepository;
import com.tripplanner.dataaccess.repository.TripRepository;
import com.tripplanner.dataaccess.repository.UserRepository;
import com.tripplanner.domain.dto.CreateExpenseDTO;
import com.tripplanner.domain.dto.ExpenseResponseDTO;
import com.tripplanner.domain.entity.Korisnik;
import com.tripplanner.domain.entity.Putovanje;
import com.tripplanner.domain.entity.Sudionik;
import com.tripplanner.domain.entity.Trosak;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for {@link ExpenseService} with real repository implementations
 * backed by an H2 in-memory database.
 *
 * <p>This test verifies the integration between the service and data access layers
 * by exercising real Spring Data JPA repositories rather than mocks. It covers:</p>
 * <ul>
 *     <li>Expense persistence via {@code ExpenseRepository}</li>
 *     <li>Trip total recalculation triggered through {@code TripService}</li>
 *     <li>Updates and deletions correctly recompute the trip total</li>
 * </ul>
 *
 * <p>Validates Requirements: 4.1, 4.3, 4.6, 4.9</p>
 */
@SpringBootTest(classes = TestBusinessApplication.class)
@Transactional
@DisplayName("ExpenseService Integration Tests")
@Tag("integration")
class ExpenseServiceIntegrationTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private TripService tripService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private UserRepository userRepository;

    private Korisnik organizer;
    private Putovanje trip;

    @BeforeEach
    void setUp() {
        // Persist a user that will act as the organizer/participant of the trip.
        organizer = userRepository.save(Korisnik.builder()
                .ime("Integration")
                .prezime("Tester")
                .email("integration.tester@example.com")
                .oauthProvider("google")
                .oauthId("google-integration-tester")
                .build());

        // Persist a trip with an initial zero total expense.
        trip = tripRepository.save(Putovanje.builder()
                .naziv("Integration Test Trip")
                .opis("Trip used for ExpenseService integration tests")
                .datumPoc(LocalDate.now().plusDays(1))
                .datumKraj(LocalDate.now().plusDays(7))
                .ukTrosak(BigDecimal.ZERO)
                .build());

        // Add the user as an organizer so authorization checks pass.
        participantRepository.save(Sudionik.builder()
                .uloga("organizer")
                .putovanje(trip)
                .korisnik(organizer)
                .build());
    }

    // ========== Requirement 4.3: Expense creation persists and triggers recalculation ==========

    @Test
    @DisplayName("createExpense persists expense in ExpenseRepository")
    void createExpense_withValidData_persistsExpenseInRepository() {
        // Given
        CreateExpenseDTO createDTO = CreateExpenseDTO.builder()
                .iznos(new BigDecimal("125.50"))
                .opis("Hotel booking")
                .datum(LocalDate.now().plusDays(2))
                .build();

        // When
        ExpenseResponseDTO response = expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(), createDTO);

        // Then - response carries the new expense data
        assertThat(response).isNotNull();
        assertThat(response.getTrosakId()).isNotNull();
        assertThat(response.getIznos()).isEqualByComparingTo(new BigDecimal("125.50"));
        assertThat(response.getOpis()).isEqualTo("Hotel booking");
        assertThat(response.getPutovanjeId()).isEqualTo(trip.getPutovanjeId());

        // Then - expense is actually persisted via the repository
        Optional<Trosak> persisted = expenseRepository.findById(response.getTrosakId());
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getIznos()).isEqualByComparingTo(new BigDecimal("125.50"));
        assertThat(persisted.get().getOpis()).isEqualTo("Hotel booking");
        assertThat(persisted.get().getPutovanje().getPutovanjeId()).isEqualTo(trip.getPutovanjeId());
    }

    @Test
    @DisplayName("createExpense triggers trip total recalculation and updates trip ukTrosak")
    void createExpense_withSingleExpense_updatesTripTotal() {
        // Given
        CreateExpenseDTO createDTO = CreateExpenseDTO.builder()
                .iznos(new BigDecimal("200.00"))
                .opis("Flights")
                .datum(LocalDate.now().plusDays(2))
                .build();

        // When
        expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(), createDTO);

        // Then - trip total has been recalculated to match the single expense
        Putovanje refreshed = tripRepository.findById(trip.getPutovanjeId()).orElseThrow();
        assertThat(refreshed.getUkTrosak()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("createExpense with multiple expenses sums them into trip total")
    void createExpense_withMultipleExpenses_sumsAllIntoTripTotal() {
        // Given - create two expenses sequentially
        CreateExpenseDTO firstExpense = CreateExpenseDTO.builder()
                .iznos(new BigDecimal("100.00"))
                .opis("Food")
                .datum(LocalDate.now().plusDays(2))
                .build();

        CreateExpenseDTO secondExpense = CreateExpenseDTO.builder()
                .iznos(new BigDecimal("75.25"))
                .opis("Transport")
                .datum(LocalDate.now().plusDays(3))
                .build();

        // When
        expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(), firstExpense);
        expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(), secondExpense);

        // Then - both expenses are persisted
        List<Trosak> persistedExpenses =
                expenseRepository.findByPutovanje_PutovanjeId(trip.getPutovanjeId());
        assertThat(persistedExpenses).hasSize(2);

        // Then - trip total reflects the sum of both expenses
        Putovanje refreshed = tripRepository.findById(trip.getPutovanjeId()).orElseThrow();
        assertThat(refreshed.getUkTrosak()).isEqualByComparingTo(new BigDecimal("175.25"));
    }

    // ========== Requirement 4.3: Update triggers recalculation ==========

    @Test
    @DisplayName("updateExpense recalculates trip total based on new amount")
    void updateExpense_withNewAmount_recalculatesTripTotal() {
        // Given - an existing expense
        CreateExpenseDTO createDTO = CreateExpenseDTO.builder()
                .iznos(new BigDecimal("50.00"))
                .opis("Initial")
                .datum(LocalDate.now().plusDays(2))
                .build();
        ExpenseResponseDTO created = expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(), createDTO);

        // Sanity check
        assertThat(tripRepository.findById(trip.getPutovanjeId()).orElseThrow()
                .getUkTrosak()).isEqualByComparingTo(new BigDecimal("50.00"));

        // When - update the expense amount
        com.tripplanner.domain.dto.UpdateExpenseDTO updateDTO =
                com.tripplanner.domain.dto.UpdateExpenseDTO.builder()
                        .iznos(new BigDecimal("300.00"))
                        .build();
        expenseService.updateExpense(
                created.getTrosakId(), organizer.getKorisnikId(), updateDTO);

        // Then - trip total reflects the updated expense amount
        Putovanje refreshed = tripRepository.findById(trip.getPutovanjeId()).orElseThrow();
        assertThat(refreshed.getUkTrosak()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    // ========== Requirement 4.3: Deletion triggers recalculation ==========

    @Test
    @DisplayName("deleteExpense recalculates trip total after removal")
    void deleteExpense_afterCreation_recalculatesTripTotal() {
        // Given - two expenses totaling 150.00
        ExpenseResponseDTO first = expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(),
                CreateExpenseDTO.builder()
                        .iznos(new BigDecimal("100.00"))
                        .opis("Keep me")
                        .datum(LocalDate.now().plusDays(2))
                        .build());

        ExpenseResponseDTO toDelete = expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(),
                CreateExpenseDTO.builder()
                        .iznos(new BigDecimal("50.00"))
                        .opis("Delete me")
                        .datum(LocalDate.now().plusDays(3))
                        .build());

        assertThat(tripRepository.findById(trip.getPutovanjeId()).orElseThrow()
                .getUkTrosak()).isEqualByComparingTo(new BigDecimal("150.00"));

        // When - delete the second expense
        expenseService.deleteExpense(toDelete.getTrosakId(), organizer.getKorisnikId());

        // Then - the deleted expense is gone from the repository
        assertThat(expenseRepository.findById(toDelete.getTrosakId())).isEmpty();
        // And the kept expense is still there
        assertThat(expenseRepository.findById(first.getTrosakId())).isPresent();

        // Then - trip total only reflects the remaining expense
        Putovanje refreshed = tripRepository.findById(trip.getPutovanjeId()).orElseThrow();
        assertThat(refreshed.getUkTrosak()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("deleteExpense for the only expense resets trip total to zero")
    void deleteExpense_onlyExpense_resetsTripTotalToZero() {
        // Given - one expense persisted
        ExpenseResponseDTO created = expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(),
                CreateExpenseDTO.builder()
                        .iznos(new BigDecimal("80.00"))
                        .opis("Only expense")
                        .datum(LocalDate.now().plusDays(2))
                        .build());

        // When - delete it
        expenseService.deleteExpense(created.getTrosakId(), organizer.getKorisnikId());

        // Then - trip total recalculates to zero
        Putovanje refreshed = tripRepository.findById(trip.getPutovanjeId()).orElseThrow();
        assertThat(refreshed.getUkTrosak()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ========== Authorization: non-participants cannot create expenses ==========

    @Test
    @DisplayName("createExpense rejects users who are not trip participants")
    void createExpense_byNonParticipant_throwsAndDoesNotPersist() {
        // Given - another user who is not a participant of the trip
        Korisnik outsider = userRepository.save(Korisnik.builder()
                .ime("Outside")
                .prezime("User")
                .email("outsider@example.com")
                .oauthProvider("google")
                .oauthId("google-outsider")
                .build());

        CreateExpenseDTO createDTO = CreateExpenseDTO.builder()
                .iznos(new BigDecimal("99.99"))
                .opis("Should not persist")
                .datum(LocalDate.now().plusDays(2))
                .build();

        // When/Then
        assertThatThrownBy(() -> expenseService.createExpense(
                trip.getPutovanjeId(), outsider.getKorisnikId(), createDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied");

        // Then - nothing was persisted and trip total is unchanged
        assertThat(expenseRepository.findByPutovanje_PutovanjeId(trip.getPutovanjeId())).isEmpty();
        Putovanje refreshed = tripRepository.findById(trip.getPutovanjeId()).orElseThrow();
        assertThat(refreshed.getUkTrosak()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
