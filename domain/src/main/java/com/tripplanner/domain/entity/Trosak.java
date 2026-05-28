package com.tripplanner.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "TROSAK")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trosak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trosakId")
    private Integer trosakId;

    @Column(name = "iznos", precision = 10, scale = 2)
    private BigDecimal iznos;

    @Column(name = "opis", length = 500)
    private String opis;

    @Column(name = "datum")
    private LocalDate datum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "putovanjeId")
    private Putovanje putovanje;
}
