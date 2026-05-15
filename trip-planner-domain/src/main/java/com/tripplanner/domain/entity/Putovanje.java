package com.tripplanner.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a trip (Putovanje).
 */
@Entity
@Table(name = "PUTOVANJE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Putovanje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "putovanjeId")
    private Integer putovanjeId;

    @Column(name = "naziv", length = 255)
    private String naziv;

    @Column(name = "opis", length = 500)
    private String opis;

    @Column(name = "datumPoc")
    private LocalDate datumPoc;

    @Column(name = "datumKraj")
    private LocalDate datumKraj;

    @Column(name = "ukTrosak", precision = 10, scale = 2)
    private BigDecimal ukTrosak;

    @OneToMany(mappedBy = "putovanje", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Aktivnost> activities = new ArrayList<>();

    @OneToMany(mappedBy = "putovanje", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Trosak> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "putovanje", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Sudionik> participants = new ArrayList<>();
}
