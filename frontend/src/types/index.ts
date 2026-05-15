// Type definitions for the Trip Planner application

export interface User {
  korisnikId: number;
  ime: string;
  prezime: string;
  email: string;
  oauthProvider: string;
}

export interface Trip {
  putovanjeId: number;
  naziv: string;
  opis: string;
  datumPoc: string;
  datumKraj: string;
  ukTrosak: number;
}

export interface Activity {
  aktivnostId: number;
  naziv: string;
  opis: string;
  datumVrijemePoc: string;
  datumVrijemeKraj: string;
  putovanjeId: number;
  lokacijaId: number;
}

export interface Location {
  lokacijaId: number;
  naziv: string;
  adresa?: string;
  grad: string;
  drzava: string;
}

export interface Expense {
  trosakId: number;
  iznos: number;
  opis: string;
  datum: string;
  putovanjeId: number;
}

export interface Participant {
  sudionikId: number;
  uloga: string;
  putovanjeId: number;
  korisnikId: number;
}

export interface Category {
  kategorijaId: number;
  naziv: string;
  opis: string;
}

// Re-export for convenience
export type { User, Trip, Activity, Location, Expense, Participant, Category };
