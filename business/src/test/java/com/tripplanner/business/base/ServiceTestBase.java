package com.tripplanner.business.base;

import com.tripplanner.dataaccess.repository.*;
import com.tripplanner.domain.entity.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Abstract base class for service tests.
 * Provides common setup for Mockito configuration and mock behaviors.
 * Includes helper methods for creating test entities and configuring common mock behaviors.
 */
@ExtendWith(MockitoExtension.class)
public abstract class ServiceTestBase {

    /**
     * Create a test user with default values.
     *
     * @return Korisnik entity
     */
    protected Korisnik createTestUser() {
        return Korisnik.builder()
                .korisnikId(1)
                .ime("John")
                .prezime("Doe")
                .email("john.doe@example.com")
                .oauthProvider("google")
                .oauthId("google-123")
                .build();
    }

    /**
     * Create a test user with custom ID and email.
     *
     * @param id    User ID
     * @param email User email
     * @return Korisnik entity
     */
    protected Korisnik createTestUser(Integer id, String email) {
        return Korisnik.builder()
                .korisnikId(id)
                .ime("John")
                .prezime("Doe")
                .email(email)
                .oauthProvider("google")
                .oauthId("google-" + email.hashCode())
                .build();
    }

    /**
     * Create a test user with custom values.
     *
     * @param id        User ID
     * @param firstName First name
     * @param lastName  Last name
     * @param email     Email
     * @return Korisnik entity
     */
    protected Korisnik createTestUser(Integer id, String firstName, String lastName, String email) {
        return Korisnik.builder()
                .korisnikId(id)
                .ime(firstName)
                .prezime(lastName)
                .email(email)
                .oauthProvider("google")
                .oauthId("google-" + email.hashCode())
                .build();
    }

    /**
     * Create a test trip with default values.
     *
     * @return Putovanje entity
     */
    protected Putovanje createTestTrip() {
        return Putovanje.builder()
                .putovanjeId(1)
                .naziv("Test Trip")
                .opis("Test trip description")
                .datumPoc(LocalDate.now().plusDays(1))
                .datumKraj(LocalDate.now().plusDays(7))
                .ukTrosak(BigDecimal.ZERO)
                .build();
    }

    /**
     * Create a test trip with custom ID, name, and dates.
     *
     * @param id        Trip ID
     * @param name      Trip name
     * @param startDate Start date
     * @param endDate   End date
     * @return Putovanje entity
     */
    protected Putovanje createTestTrip(Integer id, String name, LocalDate startDate, LocalDate endDate) {
        return Putovanje.builder()
                .putovanjeId(id)
                .naziv(name)
                .opis("Test trip description")
                .datumPoc(startDate)
                .datumKraj(endDate)
                .ukTrosak(BigDecimal.ZERO)
                .build();
    }

    /**
     * Create a test activity with default values.
     *
     * @param trip     Associated trip
     * @param location Associated location
     * @return Aktivnost entity
     */
    protected Aktivnost createTestActivity(Putovanje trip, Lokacija location) {
        return Aktivnost.builder()
                .aktivnostId(1)
                .naziv("Test Activity")
                .opis("Test activity description")
                .datumVrijemePoc(LocalDateTime.now().plusDays(2))
                .datumVrijemeKraj(LocalDateTime.now().plusDays(2).plusHours(2))
                .putovanje(trip)
                .lokacija(location)
                .build();
    }

    /**
     * Create a test activity with custom values.
     *
     * @param id        Activity ID
     * @param name      Activity name
     * @param trip      Associated trip
     * @param location  Associated location
     * @param startTime Start time
     * @param endTime   End time
     * @return Aktivnost entity
     */
    protected Aktivnost createTestActivity(Integer id, String name, Putovanje trip, Lokacija location,
                                           LocalDateTime startTime, LocalDateTime endTime) {
        return Aktivnost.builder()
                .aktivnostId(id)
                .naziv(name)
                .opis("Test activity description")
                .datumVrijemePoc(startTime)
                .datumVrijemeKraj(endTime)
                .putovanje(trip)
                .lokacija(location)
                .build();
    }

    /**
     * Create a test expense with default values.
     *
     * @param trip Associated trip
     * @return Trosak entity
     */
    protected Trosak createTestExpense(Putovanje trip) {
        return Trosak.builder()
                .trosakId(1)
                .iznos(new BigDecimal("100.00"))
                .opis("Test expense")
                .datum(LocalDate.now().plusDays(2))
                .putovanje(trip)
                .build();
    }

    /**
     * Create a test expense with custom ID and amount.
     *
     * @param id     Expense ID
     * @param trip   Associated trip
     * @param amount Expense amount
     * @return Trosak entity
     */
    protected Trosak createTestExpense(Integer id, Putovanje trip, BigDecimal amount) {
        return Trosak.builder()
                .trosakId(id)
                .iznos(amount)
                .opis("Test expense")
                .datum(LocalDate.now().plusDays(2))
                .putovanje(trip)
                .build();
    }

    /**
     * Create a test expense with custom values.
     *
     * @param id          Expense ID
     * @param trip        Associated trip
     * @param amount      Expense amount
     * @param description Expense description
     * @param date        Expense date
     * @return Trosak entity
     */
    protected Trosak createTestExpense(Integer id, Putovanje trip, BigDecimal amount, String description, LocalDate date) {
        return Trosak.builder()
                .trosakId(id)
                .iznos(amount)
                .opis(description)
                .datum(date)
                .putovanje(trip)
                .build();
    }

    /**
     * Create a test participant with default role.
     *
     * @param trip Associated trip
     * @param user Associated user
     * @return Sudionik entity
     */
    protected Sudionik createTestParticipant(Putovanje trip, Korisnik user) {
        return Sudionik.builder()
                .sudionikId(1)
                .uloga("participant")
                .putovanje(trip)
                .korisnik(user)
                .build();
    }

    /**
     * Create a test participant with custom ID and role.
     *
     * @param id   Participant ID
     * @param trip Associated trip
     * @param user Associated user
     * @param role Participant role
     * @return Sudionik entity
     */
    protected Sudionik createTestParticipant(Integer id, Putovanje trip, Korisnik user, String role) {
        return Sudionik.builder()
                .sudionikId(id)
                .uloga(role)
                .putovanje(trip)
                .korisnik(user)
                .build();
    }

    /**
     * Create a test location with default values.
     *
     * @return Lokacija entity
     */
    protected Lokacija createTestLocation() {
        return Lokacija.builder()
                .lokacijaId(1)
                .naziv("Test Location")
                .adresa("123 Test Street")
                .grad("Test City")
                .drzava("Test Country")
                .build();
    }

    /**
     * Create a test location with custom values.
     *
     * @param id      Location ID
     * @param name    Location name
     * @param address Address
     * @param city    City
     * @param country Country
     * @return Lokacija entity
     */
    protected Lokacija createTestLocation(Integer id, String name, String address, String city, String country) {
        return Lokacija.builder()
                .lokacijaId(id)
                .naziv(name)
                .adresa(address)
                .grad(city)
                .drzava(country)
                .build();
    }

    /**
     * Create a test category with default values.
     *
     * @return Kategorija entity
     */
    protected Kategorija createTestCategory() {
        return Kategorija.builder()
                .kategorijaId(1)
                .naziv("Test Category")
                .opis("Test category description")
                .build();
    }

    /**
     * Create a test category with custom ID and name.
     *
     * @param id   Category ID
     * @param name Category name
     * @return Kategorija entity
     */
    protected Kategorija createTestCategory(Integer id, String name) {
        return Kategorija.builder()
                .kategorijaId(id)
                .naziv(name)
                .opis("Test category description")
                .build();
    }

    /**
     * Setup common UserRepository mock behaviors.
     *
     * @param mockRepository Mock UserRepository
     * @param user           Test user to return
     */
    protected void mockUserRepository(UserRepository mockRepository, Korisnik user) {
        when(mockRepository.findById(user.getKorisnikId())).thenReturn(Optional.of(user));
        when(mockRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(mockRepository.save(any(Korisnik.class))).thenReturn(user);
    }

    /**
     * Setup common TripRepository mock behaviors.
     *
     * @param mockRepository Mock TripRepository
     * @param trip           Test trip to return
     */
    protected void mockTripRepository(TripRepository mockRepository, Putovanje trip) {
        when(mockRepository.findById(trip.getPutovanjeId())).thenReturn(Optional.of(trip));
        when(mockRepository.save(any(Putovanje.class))).thenReturn(trip);
    }

    /**
     * Verify no more interactions on all provided mocks.
     *
     * @param mocks Mock objects to verify
     */
    protected void verifyNoMoreInteractions(Object... mocks) {
        org.mockito.Mockito.verifyNoMoreInteractions(mocks);
    }
}
