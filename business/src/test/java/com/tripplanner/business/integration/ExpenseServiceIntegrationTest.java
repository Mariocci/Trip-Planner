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
        
        organizer = userRepository.save(Korisnik.builder()
                .ime("Integration")
                .prezime("Tester")
                .email("integration.tester@example.com")
                .oauthProvider("google")
                .oauthId("google-integration-tester")
                .build());

        
        trip = tripRepository.save(Putovanje.builder()
                .naziv("Integration Test Trip")
                .opis("Trip used for ExpenseService integration tests")
                .datumPoc(LocalDate.now().plusDays(1))
                .datumKraj(LocalDate.now().plusDays(7))
                .ukTrosak(BigDecimal.ZERO)
                .build());

        
        participantRepository.save(Sudionik.builder()
                .uloga("organizer")
                .putovanje(trip)
                .korisnik(organizer)
                .build());
    }

    

    @Test
    @DisplayName("createExpense persists expense in ExpenseRepository")
    void createExpense_withValidData_persistsExpenseInRepository() {
        
        CreateExpenseDTO createDTO = CreateExpenseDTO.builder()
                .iznos(new BigDecimal("125.50"))
                .opis("Hotel booking")
                .datum(LocalDate.now().plusDays(2))
                .build();

        
        ExpenseResponseDTO response = expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(), createDTO);

        
        assertThat(response).isNotNull();
        assertThat(response.getTrosakId()).isNotNull();
        assertThat(response.getIznos()).isEqualByComparingTo(new BigDecimal("125.50"));
        assertThat(response.getOpis()).isEqualTo("Hotel booking");
        assertThat(response.getPutovanjeId()).isEqualTo(trip.getPutovanjeId());

        
        Optional<Trosak> persisted = expenseRepository.findById(response.getTrosakId());
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getIznos()).isEqualByComparingTo(new BigDecimal("125.50"));
        assertThat(persisted.get().getOpis()).isEqualTo("Hotel booking");
        assertThat(persisted.get().getPutovanje().getPutovanjeId()).isEqualTo(trip.getPutovanjeId());
    }

    @Test
    @DisplayName("createExpense triggers trip total recalculation and updates trip ukTrosak")
    void createExpense_withSingleExpense_updatesTripTotal() {
        
        CreateExpenseDTO createDTO = CreateExpenseDTO.builder()
                .iznos(new BigDecimal("200.00"))
                .opis("Flights")
                .datum(LocalDate.now().plusDays(2))
                .build();

        
        expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(), createDTO);

        
        Putovanje refreshed = tripRepository.findById(trip.getPutovanjeId()).orElseThrow();
        assertThat(refreshed.getUkTrosak()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("createExpense with multiple expenses sums them into trip total")
    void createExpense_withMultipleExpenses_sumsAllIntoTripTotal() {
        
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

        
        expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(), firstExpense);
        expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(), secondExpense);

        
        List<Trosak> persistedExpenses =
                expenseRepository.findByPutovanje_PutovanjeId(trip.getPutovanjeId());
        assertThat(persistedExpenses).hasSize(2);

        
        Putovanje refreshed = tripRepository.findById(trip.getPutovanjeId()).orElseThrow();
        assertThat(refreshed.getUkTrosak()).isEqualByComparingTo(new BigDecimal("175.25"));
    }

    

    @Test
    @DisplayName("updateExpense recalculates trip total based on new amount")
    void updateExpense_withNewAmount_recalculatesTripTotal() {
        
        CreateExpenseDTO createDTO = CreateExpenseDTO.builder()
                .iznos(new BigDecimal("50.00"))
                .opis("Initial")
                .datum(LocalDate.now().plusDays(2))
                .build();
        ExpenseResponseDTO created = expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(), createDTO);

        
        assertThat(tripRepository.findById(trip.getPutovanjeId()).orElseThrow()
                .getUkTrosak()).isEqualByComparingTo(new BigDecimal("50.00"));

        
        com.tripplanner.domain.dto.UpdateExpenseDTO updateDTO =
                com.tripplanner.domain.dto.UpdateExpenseDTO.builder()
                        .iznos(new BigDecimal("300.00"))
                        .build();
        expenseService.updateExpense(
                created.getTrosakId(), organizer.getKorisnikId(), updateDTO);

        
        Putovanje refreshed = tripRepository.findById(trip.getPutovanjeId()).orElseThrow();
        assertThat(refreshed.getUkTrosak()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    

    @Test
    @DisplayName("deleteExpense recalculates trip total after removal")
    void deleteExpense_afterCreation_recalculatesTripTotal() {
        
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

        
        expenseService.deleteExpense(toDelete.getTrosakId(), organizer.getKorisnikId());

        
        assertThat(expenseRepository.findById(toDelete.getTrosakId())).isEmpty();
        
        assertThat(expenseRepository.findById(first.getTrosakId())).isPresent();

        
        Putovanje refreshed = tripRepository.findById(trip.getPutovanjeId()).orElseThrow();
        assertThat(refreshed.getUkTrosak()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("deleteExpense for the only expense resets trip total to zero")
    void deleteExpense_onlyExpense_resetsTripTotalToZero() {
        
        ExpenseResponseDTO created = expenseService.createExpense(
                trip.getPutovanjeId(), organizer.getKorisnikId(),
                CreateExpenseDTO.builder()
                        .iznos(new BigDecimal("80.00"))
                        .opis("Only expense")
                        .datum(LocalDate.now().plusDays(2))
                        .build());

        
        expenseService.deleteExpense(created.getTrosakId(), organizer.getKorisnikId());

        
        Putovanje refreshed = tripRepository.findById(trip.getPutovanjeId()).orElseThrow();
        assertThat(refreshed.getUkTrosak()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    

    @Test
    @DisplayName("createExpense rejects users who are not trip participants")
    void createExpense_byNonParticipant_throwsAndDoesNotPersist() {
        
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

        
        assertThatThrownBy(() -> expenseService.createExpense(
                trip.getPutovanjeId(), outsider.getKorisnikId(), createDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied");

        
        assertThat(expenseRepository.findByPutovanje_PutovanjeId(trip.getPutovanjeId())).isEmpty();
        Putovanje refreshed = tripRepository.findById(trip.getPutovanjeId()).orElseThrow();
        assertThat(refreshed.getUkTrosak()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
