-- Insert a test user for development (H2 compatible)
MERGE INTO korisnik (korisnik_id, ime, prezime, email, oauth_provider, oauth_id) 
KEY (email)
VALUES (1, 'Test', 'User', 'test@example.com', 'google', 'test123');

-- Insert a test location (H2 compatible)
MERGE INTO lokacija (lokacija_id, naziv, adresa, grad, drzava)
KEY (naziv, grad)
VALUES (1, 'Default Location', '123 Main St', 'Zagreb', 'Croatia');
