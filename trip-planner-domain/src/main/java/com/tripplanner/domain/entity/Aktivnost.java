package com.tripplanner.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an activity (Aktivnost) within a trip.
 */
@Entity
@Table(name = "AKTIVNOST")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aktivnost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aktivnostId")
    private Integer aktivnostId;

    @Column(name = "naziv", length = 255)
    private String naziv;

    @Column(name = "opis", length = 500)
    private String opis;

    @Column(name = "datumVrijemePoc")
    private LocalDateTime datumVrijemePoc;

    @Column(name = "datumVrijemeKraj")
    private LocalDateTime datumVrijemeKraj;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "putovanjeId")
    private Putovanje putovanje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lokacijaId")
    private Lokacija lokacija;

    @ManyToMany
    @JoinTable(
        name = "SPADAU",
        joinColumns = @JoinColumn(name = "aktivnostId"),
        inverseJoinColumns = @JoinColumn(name = "kategorijaId")
    )
    @Builder.Default
    private List<Kategorija> categories = new ArrayList<>();
}
