package com.tripplanner.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a category (Kategorija) for activities.
 */
@Entity
@Table(name = "KATEGORIJA")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kategorija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kategorijaId")
    private Integer kategorijaId;

    @Column(name = "naziv", length = 255)
    private String naziv;

    @Column(name = "opis", length = 500)
    private String opis;
}
