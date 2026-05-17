package com.tripplanner.dataaccess.base;

import com.tripplanner.domain.entity.*;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Abstract base class for repository tests.
 * Provides common setup for H2 database configuration and TestEntityManager access.
 * Includes helper methods for creating test entities.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public abstract class RepositoryTestBase {

    @Autowired
    protected TestEntityManager entityManager;

    /**
     * Create a test user with default values.
     *
     * @return Korisnik entity
     */
    protected Korisnik createTestUser() {
        return Korisnik.builder()
                .ime("John")
                .prezime("Doe")
                .email("john.doe@example.com")
                .oauthProvider("google")
                .oauthId("google-123")
                .build();
    }

    /**
     * Create a test user with custom email.
     *
     * @param email User email
     * @return Korisnik entity
     */
    protected Korisnik createTestUser(String email) {
        return Korisnik.builder()
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
     * @param firstName First name
     * @param lastName  Last name
     * @param email     Email
     * @return Korisnik entity
     */
    protected Korisnik createTestUser(String firstName, String lastName, String email) {
        return Korisnik.builder()
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
                .naziv("Test Trip")
                .opis("Test trip description")
                .datumPoc(LocalDate.now().plusDays(1))
                .datumKraj(LocalDate.now().plusDays(7))
                .ukTrosak(BigDecimal.ZERO)
                .build();
    }

    /**
     * Create a test trip with custom name and dates.
     *
     * @param name      Trip name
     * @param startDate Start date
     * @param endDate   End date
     * @return Putovanje entity
     */
    protected Putovanje createTestTrip(String name, LocalDate startDate, LocalDate endDate) {
        return Putovanje.builder()
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
     * @param name      Activity name
     * @param trip      Associated trip
     * @param location  Associated location
     * @param startTime Start time
     * @param endTime   End time
     * @return Aktivnost entity
     */
    protected Aktivnost createTestActivity(String name, Putovanje trip, Lokacija location,
                                           LocalDateTime startTime, LocalDateTime endTime) {
        return Aktivnost.builder()
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
                .iznos(new BigDecimal("100.00"))
                .opis("Test expense")
                .datum(LocalDate.now().plusDays(2))
                .putovanje(trip)
                .build();
    }

    /**
     * Create a test expense with custom amount.
     *
     * @param trip   Associated trip
     * @param amount Expense amount
     * @return Trosak entity
     */
    protected Trosak createTestExpense(Putovanje trip, BigDecimal amount) {
        return Trosak.builder()
                .iznos(amount)
                .opis("Test expense")
                .datum(LocalDate.now().plusDays(2))
                .putovanje(trip)
                .build();
    }

    /**
     * Create a test expense with custom values.
     *
     * @param trip        Associated trip
     * @param amount      Expense amount
     * @param description Expense description
     * @param date        Expense date
     * @return Trosak entity
     */
    protected Trosak createTestExpense(Putovanje trip, BigDecimal amount, String description, LocalDate date) {
        return Trosak.builder()
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
                .uloga("participant")
                .putovanje(trip)
                .korisnik(user)
                .build();
    }

    /**
     * Create a test participant with custom role.
     *
     * @param trip Associated trip
     * @param user Associated user
     * @param role Participant role
     * @return Sudionik entity
     */
    protected Sudionik createTestParticipant(Putovanje trip, Korisnik user, String role) {
        return Sudionik.builder()
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
                .naziv("Test Location")
                .adresa("123 Test Street")
                .grad("Test City")
                .drzava("Test Country")
                .build();
    }

    /**
     * Create a test location with custom values.
     *
     * @param name    Location name
     * @param address Address
     * @param city    City
     * @param country Country
     * @return Lokacija entity
     */
    protected Lokacija createTestLocation(String name, String address, String city, String country) {
        return Lokacija.builder()
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
                .naziv("Test Category")
                .opis("Test category description")
                .build();
    }

    /**
     * Create a test category with custom name.
     *
     * @param name Category name
     * @return Kategorija entity
     */
    protected Kategorija createTestCategory(String name) {
        return Kategorija.builder()
                .naziv(name)
                .opis("Test category description")
                .build();
    }

    /**
     * Clear all test data from the database.
     * Useful for cleanup between tests if not using @Transactional.
     */
    protected void clearDatabase() {
        EntityManager em = entityManager.getEntityManager();
        em.createQuery("DELETE FROM Aktivnost").executeUpdate();
        em.createQuery("DELETE FROM Trosak").executeUpdate();
        em.createQuery("DELETE FROM Sudionik").executeUpdate();
        em.createQuery("DELETE FROM Putovanje").executeUpdate();
        em.createQuery("DELETE FROM Korisnik").executeUpdate();
        em.createQuery("DELETE FROM Lokacija").executeUpdate();
        em.createQuery("DELETE FROM Kategorija").executeUpdate();
        em.flush();
        em.clear();
    }
}
