package com.tripplanner.dataaccess.base;

import com.tripplanner.domain.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


class RepositoryTestBaseTest {

    
    private final RepositoryTestBase testBase = new RepositoryTestBase() {};

    @Test
    void createTestUser_shouldCreateUserWithDefaultValues() {
        
        Korisnik user = testBase.createTestUser();

        
        assertThat(user).isNotNull();
        assertThat(user.getIme()).isEqualTo("John");
        assertThat(user.getPrezime()).isEqualTo("Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(user.getOauthProvider()).isEqualTo("google");
        assertThat(user.getOauthId()).isEqualTo("google-123");
    }

    @Test
    void createTestUser_withEmail_shouldCreateUserWithCustomEmail() {
        
        Korisnik user = testBase.createTestUser("custom@example.com");

        
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("custom@example.com");
    }

    @Test
    void createTestTrip_shouldCreateTripWithDefaultValues() {
        
        Putovanje trip = testBase.createTestTrip();

        
        assertThat(trip).isNotNull();
        assertThat(trip.getNaziv()).isEqualTo("Test Trip");
        assertThat(trip.getOpis()).isEqualTo("Test trip description");
        assertThat(trip.getDatumPoc()).isAfter(LocalDate.now());
        assertThat(trip.getDatumKraj()).isAfter(trip.getDatumPoc());
        assertThat(trip.getUkTrosak()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void createTestLocation_shouldCreateLocationWithDefaultValues() {
        
        Lokacija location = testBase.createTestLocation();

        
        assertThat(location).isNotNull();
        assertThat(location.getNaziv()).isEqualTo("Test Location");
        assertThat(location.getAdresa()).isEqualTo("123 Test Street");
        assertThat(location.getGrad()).isEqualTo("Test City");
        assertThat(location.getDrzava()).isEqualTo("Test Country");
    }

    @Test
    void createTestCategory_shouldCreateCategoryWithDefaultValues() {
        
        Kategorija category = testBase.createTestCategory();

        
        assertThat(category).isNotNull();
        assertThat(category.getNaziv()).isEqualTo("Test Category");
        assertThat(category.getOpis()).isEqualTo("Test category description");
    }
}
