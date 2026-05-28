package com.tripplanner.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "KORISNIK")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Korisnik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "korisnikId")
    private Integer korisnikId;

    @Column(name = "ime")
    private String ime;

    @Column(name = "prezime")
    private String prezime;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "oauthProvider")
    private String oauthProvider;

    @Column(name = "oauthId")
    private String oauthId;
}
