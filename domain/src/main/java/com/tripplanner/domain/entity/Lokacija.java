package com.tripplanner.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a location (Lokacija).
 */
@Entity
@Table(name = "LOKACIJA")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lokacija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lokacijaId")
    private Integer lokacijaId;

    @Column(name = "naziv", length = 255)
    private String naziv;

    @Column(name = "adresa", length = 255)
    private String adresa;

    @Column(name = "grad", length = 100)
    private String grad;

    @Column(name = "drzava", length = 100)
    private String drzava;
}
