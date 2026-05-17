package com.tripplanner.domain.util;

import com.tripplanner.domain.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class providing fluent builders for creating test entities.
 * Provides a convenient API for building test data with sensible defaults.
 */
public class TestDataBuilder {

    /**
     * Start building a Korisnik (User) entity.
     *
     * @return UserBuilder instance
     */
    public static UserBuilder user() {
        return new UserBuilder();
    }

    /**
     * Start building a Putovanje (Trip) entity.
     *
     * @return TripBuilder instance
     */
    public static TripBuilder trip() {
        return new TripBuilder();
    }

    /**
     * Start building an Aktivnost (Activity) entity.
     *
     * @return ActivityBuilder instance
     */
    public static ActivityBuilder activity() {
        return new ActivityBuilder();
    }

    /**
     * Start building a Trosak (Expense) entity.
     *
     * @return ExpenseBuilder instance
     */
    public static ExpenseBuilder expense() {
        return new ExpenseBuilder();
    }

    /**
     * Start building a Sudionik (Participant) entity.
     *
     * @return ParticipantBuilder instance
     */
    public static ParticipantBuilder participant() {
        return new ParticipantBuilder();
    }

    /**
     * Start building a Lokacija (Location) entity.
     *
     * @return LocationBuilder instance
     */
    public static LocationBuilder location() {
        return new LocationBuilder();
    }

    /**
     * Start building a Kategorija (Category) entity.
     *
     * @return CategoryBuilder instance
     */
    public static CategoryBuilder category() {
        return new CategoryBuilder();
    }

    /**
     * Fluent builder for Korisnik entities.
     */
    public static class UserBuilder {
        private Integer korisnikId;
        private String ime = "John";
        private String prezime = "Doe";
        private String email = "john.doe@example.com";
        private String oauthProvider = "google";
        private String oauthId = "google-123";

        public UserBuilder withId(Integer id) {
            this.korisnikId = id;
            return this;
        }

        public UserBuilder withName(String firstName, String lastName) {
            this.ime = firstName;
            this.prezime = lastName;
            return this;
        }

        public UserBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder withOAuthProvider(String provider, String oauthId) {
            this.oauthProvider = provider;
            this.oauthId = oauthId;
            return this;
        }

        public Korisnik build() {
            return Korisnik.builder()
                    .korisnikId(korisnikId)
                    .ime(ime)
                    .prezime(prezime)
                    .email(email)
                    .oauthProvider(oauthProvider)
                    .oauthId(oauthId)
                    .build();
        }
    }

    /**
     * Fluent builder for Putovanje entities.
     */
    public static class TripBuilder {
        private Integer putovanjeId;
        private String naziv = "Test Trip";
        private String opis = "Test trip description";
        private LocalDate datumPoc = LocalDate.now().plusDays(1);
        private LocalDate datumKraj = LocalDate.now().plusDays(7);
        private BigDecimal ukTrosak = BigDecimal.ZERO;
        private List<Aktivnost> activities = new ArrayList<>();
        private List<Trosak> expenses = new ArrayList<>();
        private List<Sudionik> participants = new ArrayList<>();

        public TripBuilder withId(Integer id) {
            this.putovanjeId = id;
            return this;
        }

        public TripBuilder withName(String name) {
            this.naziv = name;
            return this;
        }

        public TripBuilder withDescription(String description) {
            this.opis = description;
            return this;
        }

        public TripBuilder withDates(LocalDate startDate, LocalDate endDate) {
            this.datumPoc = startDate;
            this.datumKraj = endDate;
            return this;
        }

        public TripBuilder withTotalExpense(BigDecimal total) {
            this.ukTrosak = total;
            return this;
        }

        public TripBuilder withActivities(List<Aktivnost> activities) {
            this.activities = activities;
            return this;
        }

        public TripBuilder withExpenses(List<Trosak> expenses) {
            this.expenses = expenses;
            return this;
        }

        public TripBuilder withParticipants(List<Sudionik> participants) {
            this.participants = participants;
            return this;
        }

        public Putovanje build() {
            return Putovanje.builder()
                    .putovanjeId(putovanjeId)
                    .naziv(naziv)
                    .opis(opis)
                    .datumPoc(datumPoc)
                    .datumKraj(datumKraj)
                    .ukTrosak(ukTrosak)
                    .activities(activities)
                    .expenses(expenses)
                    .participants(participants)
                    .build();
        }
    }

    /**
     * Fluent builder for Aktivnost entities.
     */
    public static class ActivityBuilder {
        private Integer aktivnostId;
        private String naziv = "Test Activity";
        private String opis = "Test activity description";
        private LocalDateTime datumVrijemePoc = LocalDateTime.now().plusDays(2);
        private LocalDateTime datumVrijemeKraj = LocalDateTime.now().plusDays(2).plusHours(2);
        private Putovanje putovanje;
        private Lokacija lokacija;
        private List<Kategorija> categories = new ArrayList<>();

        public ActivityBuilder withId(Integer id) {
            this.aktivnostId = id;
            return this;
        }

        public ActivityBuilder withName(String name) {
            this.naziv = name;
            return this;
        }

        public ActivityBuilder withDescription(String description) {
            this.opis = description;
            return this;
        }

        public ActivityBuilder withDateTime(LocalDateTime startTime, LocalDateTime endTime) {
            this.datumVrijemePoc = startTime;
            this.datumVrijemeKraj = endTime;
            return this;
        }

        public ActivityBuilder withTrip(Putovanje trip) {
            this.putovanje = trip;
            return this;
        }

        public ActivityBuilder withLocation(Lokacija location) {
            this.lokacija = location;
            return this;
        }

        public ActivityBuilder withCategories(List<Kategorija> categories) {
            this.categories = categories;
            return this;
        }

        public Aktivnost build() {
            return Aktivnost.builder()
                    .aktivnostId(aktivnostId)
                    .naziv(naziv)
                    .opis(opis)
                    .datumVrijemePoc(datumVrijemePoc)
                    .datumVrijemeKraj(datumVrijemeKraj)
                    .putovanje(putovanje)
                    .lokacija(lokacija)
                    .categories(categories)
                    .build();
        }
    }

    /**
     * Fluent builder for Trosak entities.
     */
    public static class ExpenseBuilder {
        private Integer trosakId;
        private BigDecimal iznos = new BigDecimal("100.00");
        private String opis = "Test expense";
        private LocalDate datum = LocalDate.now().plusDays(2);
        private Putovanje putovanje;

        public ExpenseBuilder withId(Integer id) {
            this.trosakId = id;
            return this;
        }

        public ExpenseBuilder withAmount(BigDecimal amount) {
            this.iznos = amount;
            return this;
        }

        public ExpenseBuilder withAmount(String amount) {
            this.iznos = new BigDecimal(amount);
            return this;
        }

        public ExpenseBuilder withDescription(String description) {
            this.opis = description;
            return this;
        }

        public ExpenseBuilder withDate(LocalDate date) {
            this.datum = date;
            return this;
        }

        public ExpenseBuilder withTrip(Putovanje trip) {
            this.putovanje = trip;
            return this;
        }

        public Trosak build() {
            return Trosak.builder()
                    .trosakId(trosakId)
                    .iznos(iznos)
                    .opis(opis)
                    .datum(datum)
                    .putovanje(putovanje)
                    .build();
        }
    }

    /**
     * Fluent builder for Sudionik entities.
     */
    public static class ParticipantBuilder {
        private Integer sudionikId;
        private String uloga = "participant";
        private Putovanje putovanje;
        private Korisnik korisnik;

        public ParticipantBuilder withId(Integer id) {
            this.sudionikId = id;
            return this;
        }

        public ParticipantBuilder withRole(String role) {
            this.uloga = role;
            return this;
        }

        public ParticipantBuilder asOrganizer() {
            this.uloga = "organizer";
            return this;
        }

        public ParticipantBuilder asParticipant() {
            this.uloga = "participant";
            return this;
        }

        public ParticipantBuilder withTrip(Putovanje trip) {
            this.putovanje = trip;
            return this;
        }

        public ParticipantBuilder withUser(Korisnik user) {
            this.korisnik = user;
            return this;
        }

        public Sudionik build() {
            return Sudionik.builder()
                    .sudionikId(sudionikId)
                    .uloga(uloga)
                    .putovanje(putovanje)
                    .korisnik(korisnik)
                    .build();
        }
    }

    /**
     * Fluent builder for Lokacija entities.
     */
    public static class LocationBuilder {
        private Integer lokacijaId;
        private String naziv = "Test Location";
        private String adresa = "123 Test Street";
        private String grad = "Test City";
        private String drzava = "Test Country";

        public LocationBuilder withId(Integer id) {
            this.lokacijaId = id;
            return this;
        }

        public LocationBuilder withName(String name) {
            this.naziv = name;
            return this;
        }

        public LocationBuilder withAddress(String address) {
            this.adresa = address;
            return this;
        }

        public LocationBuilder withCity(String city) {
            this.grad = city;
            return this;
        }

        public LocationBuilder withCountry(String country) {
            this.drzava = country;
            return this;
        }

        public Lokacija build() {
            return Lokacija.builder()
                    .lokacijaId(lokacijaId)
                    .naziv(naziv)
                    .adresa(adresa)
                    .grad(grad)
                    .drzava(drzava)
                    .build();
        }
    }

    /**
     * Fluent builder for Kategorija entities.
     */
    public static class CategoryBuilder {
        private Integer kategorijaId;
        private String naziv = "Test Category";
        private String opis = "Test category description";

        public CategoryBuilder withId(Integer id) {
            this.kategorijaId = id;
            return this;
        }

        public CategoryBuilder withName(String name) {
            this.naziv = name;
            return this;
        }

        public CategoryBuilder withDescription(String description) {
            this.opis = description;
            return this;
        }

        public Kategorija build() {
            return Kategorija.builder()
                    .kategorijaId(kategorijaId)
                    .naziv(naziv)
                    .opis(opis)
                    .build();
        }
    }
}
