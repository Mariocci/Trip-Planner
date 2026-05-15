package com.tripplanner.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a participant (Sudionik) in a trip.
 */
@Entity
@Table(name = "SUDIONIK")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sudionik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sudionikId")
    private Integer sudionikId;

    @Column(name = "uloga", length = 100)
    private String uloga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "putovanjeId")
    private Putovanje putovanje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "korisnikId")
    private Korisnik korisnik;
}
