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


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class RepositoryTestBase {

    @Autowired
    protected TestEntityManager entityManager;

    
    protected Korisnik createTestUser() {
        return Korisnik.builder()
                .ime("John")
                .prezime("Doe")
                .email("john.doe@example.com")
                .oauthProvider("google")
                .oauthId("google-123")
                .build();
    }

    
    protected Korisnik createTestUser(String email) {
        return Korisnik.builder()
                .ime("John")
                .prezime("Doe")
                .email(email)
                .oauthProvider("google")
                .oauthId("google-" + email.hashCode())
                .build();
    }

    
    protected Korisnik createTestUser(String firstName, String lastName, String email) {
        return Korisnik.builder()
                .ime(firstName)
                .prezime(lastName)
                .email(email)
                .oauthProvider("google")
                .oauthId("google-" + email.hashCode())
                .build();
    }

    
    protected Putovanje createTestTrip() {
        return Putovanje.builder()
                .naziv("Test Trip")
                .opis("Test trip description")
                .datumPoc(LocalDate.now().plusDays(1))
                .datumKraj(LocalDate.now().plusDays(7))
                .ukTrosak(BigDecimal.ZERO)
                .build();
    }

    
    protected Putovanje createTestTrip(String name, LocalDate startDate, LocalDate endDate) {
        return Putovanje.builder()
                .naziv(name)
                .opis("Test trip description")
                .datumPoc(startDate)
                .datumKraj(endDate)
                .ukTrosak(BigDecimal.ZERO)
                .build();
    }

    
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

    
    protected Trosak createTestExpense(Putovanje trip) {
        return Trosak.builder()
                .iznos(new BigDecimal("100.00"))
                .opis("Test expense")
                .datum(LocalDate.now().plusDays(2))
                .putovanje(trip)
                .build();
    }

    
    protected Trosak createTestExpense(Putovanje trip, BigDecimal amount) {
        return Trosak.builder()
                .iznos(amount)
                .opis("Test expense")
                .datum(LocalDate.now().plusDays(2))
                .putovanje(trip)
                .build();
    }

    
    protected Trosak createTestExpense(Putovanje trip, BigDecimal amount, String description, LocalDate date) {
        return Trosak.builder()
                .iznos(amount)
                .opis(description)
                .datum(date)
                .putovanje(trip)
                .build();
    }

    
    protected Sudionik createTestParticipant(Putovanje trip, Korisnik user) {
        return Sudionik.builder()
                .uloga("participant")
                .putovanje(trip)
                .korisnik(user)
                .build();
    }

    
    protected Sudionik createTestParticipant(Putovanje trip, Korisnik user, String role) {
        return Sudionik.builder()
                .uloga(role)
                .putovanje(trip)
                .korisnik(user)
                .build();
    }

    
    protected Lokacija createTestLocation() {
        return Lokacija.builder()
                .naziv("Test Location")
                .adresa("123 Test Street")
                .grad("Test City")
                .drzava("Test Country")
                .build();
    }

    
    protected Lokacija createTestLocation(String name, String address, String city, String country) {
        return Lokacija.builder()
                .naziv(name)
                .adresa(address)
                .grad(city)
                .drzava(country)
                .build();
    }

    
    protected Kategorija createTestCategory() {
        return Kategorija.builder()
                .naziv("Test Category")
                .opis("Test category description")
                .build();
    }

    
    protected Kategorija createTestCategory(String name) {
        return Kategorija.builder()
                .naziv(name)
                .opis("Test category description")
                .build();
    }

    
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
