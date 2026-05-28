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


@ExtendWith(MockitoExtension.class)
public abstract class ServiceTestBase {

    
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

    
    protected Trosak createTestExpense(Putovanje trip) {
        return Trosak.builder()
                .trosakId(1)
                .iznos(new BigDecimal("100.00"))
                .opis("Test expense")
                .datum(LocalDate.now().plusDays(2))
                .putovanje(trip)
                .build();
    }

    
    protected Trosak createTestExpense(Integer id, Putovanje trip, BigDecimal amount) {
        return Trosak.builder()
                .trosakId(id)
                .iznos(amount)
                .opis("Test expense")
                .datum(LocalDate.now().plusDays(2))
                .putovanje(trip)
                .build();
    }

    
    protected Trosak createTestExpense(Integer id, Putovanje trip, BigDecimal amount, String description, LocalDate date) {
        return Trosak.builder()
                .trosakId(id)
                .iznos(amount)
                .opis(description)
                .datum(date)
                .putovanje(trip)
                .build();
    }

    
    protected Sudionik createTestParticipant(Putovanje trip, Korisnik user) {
        return Sudionik.builder()
                .sudionikId(1)
                .uloga("participant")
                .putovanje(trip)
                .korisnik(user)
                .build();
    }

    
    protected Sudionik createTestParticipant(Integer id, Putovanje trip, Korisnik user, String role) {
        return Sudionik.builder()
                .sudionikId(id)
                .uloga(role)
                .putovanje(trip)
                .korisnik(user)
                .build();
    }

    
    protected Lokacija createTestLocation() {
        return Lokacija.builder()
                .lokacijaId(1)
                .naziv("Test Location")
                .adresa("123 Test Street")
                .grad("Test City")
                .drzava("Test Country")
                .build();
    }

    
    protected Lokacija createTestLocation(Integer id, String name, String address, String city, String country) {
        return Lokacija.builder()
                .lokacijaId(id)
                .naziv(name)
                .adresa(address)
                .grad(city)
                .drzava(country)
                .build();
    }

    
    protected Kategorija createTestCategory() {
        return Kategorija.builder()
                .kategorijaId(1)
                .naziv("Test Category")
                .opis("Test category description")
                .build();
    }

    
    protected Kategorija createTestCategory(Integer id, String name) {
        return Kategorija.builder()
                .kategorijaId(id)
                .naziv(name)
                .opis("Test category description")
                .build();
    }

    
    protected void mockUserRepository(UserRepository mockRepository, Korisnik user) {
        when(mockRepository.findById(user.getKorisnikId())).thenReturn(Optional.of(user));
        when(mockRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(mockRepository.save(any(Korisnik.class))).thenReturn(user);
    }

    
    protected void mockTripRepository(TripRepository mockRepository, Putovanje trip) {
        when(mockRepository.findById(trip.getPutovanjeId())).thenReturn(Optional.of(trip));
        when(mockRepository.save(any(Putovanje.class))).thenReturn(trip);
    }

    
    protected void verifyNoMoreInteractions(Object... mocks) {
        org.mockito.Mockito.verifyNoMoreInteractions(mocks);
    }
}
