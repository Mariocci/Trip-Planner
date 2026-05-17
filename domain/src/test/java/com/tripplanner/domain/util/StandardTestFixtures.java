package com.tripplanner.domain.util;

import com.tripplanner.domain.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Provides pre-configured test data fixtures for common testing scenarios.
 * These fixtures can be used across different test classes to ensure consistency.
 */
public class StandardTestFixtures {

    // User Fixtures
    public static final Korisnik ORGANIZER_USER = TestDataBuilder.user()
            .withId(1)
            .withName("Alice", "Organizer")
            .withEmail("alice.organizer@example.com")
            .withOAuthProvider("google", "google-alice-123")
            .build();

    public static final Korisnik PARTICIPANT_USER = TestDataBuilder.user()
            .withId(2)
            .withName("Bob", "Participant")
            .withEmail("bob.participant@example.com")
            .withOAuthProvider("google", "google-bob-456")
            .build();

    public static final Korisnik ANOTHER_USER = TestDataBuilder.user()
            .withId(3)
            .withName("Charlie", "User")
            .withEmail("charlie.user@example.com")
            .withOAuthProvider("google", "google-charlie-789")
            .build();

    // Trip Fixtures
    public static final Putovanje STANDARD_TRIP = TestDataBuilder.trip()
            .withId(1)
            .withName("Paris Adventure")
            .withDescription("A wonderful trip to Paris")
            .withDates(LocalDate.now().plusDays(30), LocalDate.now().plusDays(37))
            .withTotalExpense(BigDecimal.ZERO)
            .build();

    public static final Putovanje PAST_TRIP = TestDataBuilder.trip()
            .withId(2)
            .withName("Rome Historical Tour")
            .withDescription("A trip to Rome that already happened")
            .withDates(LocalDate.now().minusDays(30), LocalDate.now().minusDays(23))
            .withTotalExpense(new BigDecimal("1500.00"))
            .build();

    public static final Putovanje FUTURE_TRIP = TestDataBuilder.trip()
            .withId(3)
            .withName("Tokyo Experience")
            .withDescription("An upcoming trip to Tokyo")
            .withDates(LocalDate.now().plusDays(90), LocalDate.now().plusDays(104))
            .withTotalExpense(BigDecimal.ZERO)
            .build();

    public static final Putovanje CURRENT_TRIP = TestDataBuilder.trip()
            .withId(4)
            .withName("Barcelona Beach Week")
            .withDescription("Currently happening trip to Barcelona")
            .withDates(LocalDate.now().minusDays(2), LocalDate.now().plusDays(5))
            .withTotalExpense(new BigDecimal("800.00"))
            .build();

    // Location Fixtures
    public static final Lokacija EIFFEL_TOWER = TestDataBuilder.location()
            .withId(1)
            .withName("Eiffel Tower")
            .withAddress("Champ de Mars, 5 Avenue Anatole France")
            .withCity("Paris")
            .withCountry("France")
            .build();

    public static final Lokacija LOUVRE_MUSEUM = TestDataBuilder.location()
            .withId(2)
            .withName("Louvre Museum")
            .withAddress("Rue de Rivoli")
            .withCity("Paris")
            .withCountry("France")
            .build();

    public static final Lokacija COLOSSEUM = TestDataBuilder.location()
            .withId(3)
            .withName("Colosseum")
            .withAddress("Piazza del Colosseo, 1")
            .withCity("Rome")
            .withCountry("Italy")
            .build();

    // Category Fixtures
    public static final Kategorija SIGHTSEEING_CATEGORY = TestDataBuilder.category()
            .withId(1)
            .withName("Sightseeing")
            .withDescription("Tourist attractions and landmarks")
            .build();

    public static final Kategorija DINING_CATEGORY = TestDataBuilder.category()
            .withId(2)
            .withName("Dining")
            .withDescription("Restaurants and food experiences")
            .build();

    public static final Kategorija ENTERTAINMENT_CATEGORY = TestDataBuilder.category()
            .withId(3)
            .withName("Entertainment")
            .withDescription("Shows, concerts, and entertainment")
            .build();

    public static final Kategorija SHOPPING_CATEGORY = TestDataBuilder.category()
            .withId(4)
            .withName("Shopping")
            .withDescription("Shopping and markets")
            .build();

    // Activity Fixtures
    public static final Aktivnost STANDARD_ACTIVITY = TestDataBuilder.activity()
            .withId(1)
            .withName("Visit Eiffel Tower")
            .withDescription("Tour the iconic Eiffel Tower")
            .withDateTime(
                    LocalDateTime.now().plusDays(31).withHour(10).withMinute(0),
                    LocalDateTime.now().plusDays(31).withHour(12).withMinute(0)
            )
            .withTrip(STANDARD_TRIP)
            .withLocation(EIFFEL_TOWER)
            .build();

    public static final Aktivnost MUSEUM_ACTIVITY = TestDataBuilder.activity()
            .withId(2)
            .withName("Explore Louvre Museum")
            .withDescription("Visit the world's largest art museum")
            .withDateTime(
                    LocalDateTime.now().plusDays(32).withHour(9).withMinute(0),
                    LocalDateTime.now().plusDays(32).withHour(17).withMinute(0)
            )
            .withTrip(STANDARD_TRIP)
            .withLocation(LOUVRE_MUSEUM)
            .build();

    // Expense Fixtures
    public static final Trosak STANDARD_EXPENSE = TestDataBuilder.expense()
            .withId(1)
            .withAmount("100.00")
            .withDescription("Hotel accommodation")
            .withDate(LocalDate.now().plusDays(30))
            .withTrip(STANDARD_TRIP)
            .build();

    public static final Trosak FLIGHT_EXPENSE = TestDataBuilder.expense()
            .withId(2)
            .withAmount("450.00")
            .withDescription("Round-trip flight tickets")
            .withDate(LocalDate.now().plusDays(30))
            .withTrip(STANDARD_TRIP)
            .build();

    public static final Trosak MEAL_EXPENSE = TestDataBuilder.expense()
            .withId(3)
            .withAmount("75.50")
            .withDescription("Dinner at local restaurant")
            .withDate(LocalDate.now().plusDays(31))
            .withTrip(STANDARD_TRIP)
            .build();

    // Participant Fixtures
    public static final Sudionik ORGANIZER_PARTICIPANT = TestDataBuilder.participant()
            .withId(1)
            .asOrganizer()
            .withTrip(STANDARD_TRIP)
            .withUser(ORGANIZER_USER)
            .build();

    public static final Sudionik REGULAR_PARTICIPANT = TestDataBuilder.participant()
            .withId(2)
            .asParticipant()
            .withTrip(STANDARD_TRIP)
            .withUser(PARTICIPANT_USER)
            .build();

    /**
     * Create a copy of ORGANIZER_USER with a different ID.
     * Useful for tests that need multiple distinct users.
     *
     * @param id New user ID
     * @return Korisnik entity
     */
    public static Korisnik createOrganizerUser(Integer id) {
        return TestDataBuilder.user()
                .withId(id)
                .withName("Alice", "Organizer")
                .withEmail("alice.organizer" + id + "@example.com")
                .withOAuthProvider("google", "google-alice-" + id)
                .build();
    }

    /**
     * Create a copy of PARTICIPANT_USER with a different ID.
     * Useful for tests that need multiple distinct users.
     *
     * @param id New user ID
     * @return Korisnik entity
     */
    public static Korisnik createParticipantUser(Integer id) {
        return TestDataBuilder.user()
                .withId(id)
                .withName("Bob", "Participant")
                .withEmail("bob.participant" + id + "@example.com")
                .withOAuthProvider("google", "google-bob-" + id)
                .build();
    }

    /**
     * Create a copy of STANDARD_TRIP with a different ID.
     * Useful for tests that need multiple distinct trips.
     *
     * @param id New trip ID
     * @return Putovanje entity
     */
    public static Putovanje createStandardTrip(Integer id) {
        return TestDataBuilder.trip()
                .withId(id)
                .withName("Paris Adventure " + id)
                .withDescription("A wonderful trip to Paris")
                .withDates(LocalDate.now().plusDays(30), LocalDate.now().plusDays(37))
                .withTotalExpense(BigDecimal.ZERO)
                .build();
    }

    /**
     * Create a copy of STANDARD_EXPENSE with a different ID and amount.
     * Useful for tests that need multiple distinct expenses.
     *
     * @param id     New expense ID
     * @param amount Expense amount
     * @param trip   Associated trip
     * @return Trosak entity
     */
    public static Trosak createExpense(Integer id, String amount, Putovanje trip) {
        return TestDataBuilder.expense()
                .withId(id)
                .withAmount(amount)
                .withDescription("Test expense " + id)
                .withDate(LocalDate.now().plusDays(2))
                .withTrip(trip)
                .build();
    }

    /**
     * Create a copy of STANDARD_ACTIVITY with a different ID.
     * Useful for tests that need multiple distinct activities.
     *
     * @param id       New activity ID
     * @param trip     Associated trip
     * @param location Associated location
     * @return Aktivnost entity
     */
    public static Aktivnost createActivity(Integer id, Putovanje trip, Lokacija location) {
        return TestDataBuilder.activity()
                .withId(id)
                .withName("Test Activity " + id)
                .withDescription("Test activity description")
                .withDateTime(
                        LocalDateTime.now().plusDays(2).withHour(10).withMinute(0),
                        LocalDateTime.now().plusDays(2).withHour(12).withMinute(0)
                )
                .withTrip(trip)
                .withLocation(location)
                .build();
    }

    /**
     * Create a participant with custom values.
     *
     * @param id   Participant ID
     * @param trip Associated trip
     * @param user Associated user
     * @param role Participant role
     * @return Sudionik entity
     */
    public static Sudionik createParticipant(Integer id, Putovanje trip, Korisnik user, String role) {
        return TestDataBuilder.participant()
                .withId(id)
                .withRole(role)
                .withTrip(trip)
                .withUser(user)
                .build();
    }
}
