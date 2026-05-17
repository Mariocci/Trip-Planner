package com.tripplanner.domain.util;

import com.tripplanner.domain.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class to verify TestDataBuilder fluent API works correctly.
 */
class TestDataBuilderTest {

    @Test
    void userBuilder_shouldBuildUserWithDefaultValues() {
        // When
        Korisnik user = TestDataBuilder.user().build();

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getIme()).isEqualTo("John");
        assertThat(user.getPrezime()).isEqualTo("Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void userBuilder_shouldBuildUserWithCustomValues() {
        // When
        Korisnik user = TestDataBuilder.user()
                .withId(5)
                .withName("Alice", "Smith")
                .withEmail("alice@example.com")
                .withOAuthProvider("github", "github-456")
                .build();

        // Then
        assertThat(user.getKorisnikId()).isEqualTo(5);
        assertThat(user.getIme()).isEqualTo("Alice");
        assertThat(user.getPrezime()).isEqualTo("Smith");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getOauthProvider()).isEqualTo("github");
        assertThat(user.getOauthId()).isEqualTo("github-456");
    }

    @Test
    void tripBuilder_shouldBuildTripWithDefaultValues() {
        // When
        Putovanje trip = TestDataBuilder.trip().build();

        // Then
        assertThat(trip).isNotNull();
        assertThat(trip.getNaziv()).isEqualTo("Test Trip");
        assertThat(trip.getUkTrosak()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void tripBuilder_shouldBuildTripWithCustomValues() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 6, 1);
        LocalDate endDate = LocalDate.of(2024, 6, 10);

        // When
        Putovanje trip = TestDataBuilder.trip()
                .withId(10)
                .withName("Paris Trip")
                .withDescription("A wonderful trip to Paris")
                .withDates(startDate, endDate)
                .withTotalExpense(new BigDecimal("1500.00"))
                .build();

        // Then
        assertThat(trip.getPutovanjeId()).isEqualTo(10);
        assertThat(trip.getNaziv()).isEqualTo("Paris Trip");
        assertThat(trip.getOpis()).isEqualTo("A wonderful trip to Paris");
        assertThat(trip.getDatumPoc()).isEqualTo(startDate);
        assertThat(trip.getDatumKraj()).isEqualTo(endDate);
        assertThat(trip.getUkTrosak()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    void expenseBuilder_shouldBuildExpenseWithDefaultValues() {
        // When
        Trosak expense = TestDataBuilder.expense().build();

        // Then
        assertThat(expense).isNotNull();
        assertThat(expense.getIznos()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(expense.getOpis()).isEqualTo("Test expense");
    }

    @Test
    void expenseBuilder_shouldBuildExpenseWithCustomValues() {
        // Given
        Putovanje trip = TestDataBuilder.trip().withId(1).build();
        LocalDate date = LocalDate.of(2024, 6, 5);

        // When
        Trosak expense = TestDataBuilder.expense()
                .withId(20)
                .withAmount("250.50")
                .withDescription("Hotel accommodation")
                .withDate(date)
                .withTrip(trip)
                .build();

        // Then
        assertThat(expense.getTrosakId()).isEqualTo(20);
        assertThat(expense.getIznos()).isEqualByComparingTo(new BigDecimal("250.50"));
        assertThat(expense.getOpis()).isEqualTo("Hotel accommodation");
        assertThat(expense.getDatum()).isEqualTo(date);
        assertThat(expense.getPutovanje()).isEqualTo(trip);
    }

    @Test
    void participantBuilder_shouldBuildParticipantAsOrganizer() {
        // Given
        Korisnik user = TestDataBuilder.user().withId(1).build();
        Putovanje trip = TestDataBuilder.trip().withId(1).build();

        // When
        Sudionik participant = TestDataBuilder.participant()
                .withId(1)
                .asOrganizer()
                .withUser(user)
                .withTrip(trip)
                .build();

        // Then
        assertThat(participant.getSudionikId()).isEqualTo(1);
        assertThat(participant.getUloga()).isEqualTo("organizer");
        assertThat(participant.getKorisnik()).isEqualTo(user);
        assertThat(participant.getPutovanje()).isEqualTo(trip);
    }

    @Test
    void locationBuilder_shouldBuildLocationWithCustomValues() {
        // When
        Lokacija location = TestDataBuilder.location()
                .withId(5)
                .withName("Eiffel Tower")
                .withAddress("Champ de Mars")
                .withCity("Paris")
                .withCountry("France")
                .build();

        // Then
        assertThat(location.getLokacijaId()).isEqualTo(5);
        assertThat(location.getNaziv()).isEqualTo("Eiffel Tower");
        assertThat(location.getAdresa()).isEqualTo("Champ de Mars");
        assertThat(location.getGrad()).isEqualTo("Paris");
        assertThat(location.getDrzava()).isEqualTo("France");
    }

    @Test
    void categoryBuilder_shouldBuildCategoryWithCustomValues() {
        // When
        Kategorija category = TestDataBuilder.category()
                .withId(3)
                .withName("Sightseeing")
                .withDescription("Tourist attractions")
                .build();

        // Then
        assertThat(category.getKategorijaId()).isEqualTo(3);
        assertThat(category.getNaziv()).isEqualTo("Sightseeing");
        assertThat(category.getOpis()).isEqualTo("Tourist attractions");
    }

    @Test
    void activityBuilder_shouldBuildActivityWithCustomValues() {
        // Given
        Putovanje trip = TestDataBuilder.trip().withId(1).build();
        Lokacija location = TestDataBuilder.location().withId(1).build();
        LocalDateTime startTime = LocalDateTime.of(2024, 6, 5, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 6, 5, 12, 0);

        // When
        Aktivnost activity = TestDataBuilder.activity()
                .withId(15)
                .withName("Visit Eiffel Tower")
                .withDescription("Tour the iconic landmark")
                .withDateTime(startTime, endTime)
                .withTrip(trip)
                .withLocation(location)
                .build();

        // Then
        assertThat(activity.getAktivnostId()).isEqualTo(15);
        assertThat(activity.getNaziv()).isEqualTo("Visit Eiffel Tower");
        assertThat(activity.getOpis()).isEqualTo("Tour the iconic landmark");
        assertThat(activity.getDatumVrijemePoc()).isEqualTo(startTime);
        assertThat(activity.getDatumVrijemeKraj()).isEqualTo(endTime);
        assertThat(activity.getPutovanje()).isEqualTo(trip);
        assertThat(activity.getLokacija()).isEqualTo(location);
    }
}
