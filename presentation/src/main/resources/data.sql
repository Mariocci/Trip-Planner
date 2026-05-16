-- Insert a test user for development (PostgreSQL compatible)
INSERT INTO korisnik (korisnik_id, ime, prezime, email, oauth_provider, oauth_id) 
VALUES (1, 'Test', 'User', 'test@example.com', 'google', 'test123')
ON CONFLICT (email) DO NOTHING;

-- Insert a test location (PostgreSQL compatible)
INSERT INTO lokacija (lokacija_id, naziv, adresa, grad, drzava)
VALUES (1, 'Default Location', '123 Main St', 'Zagreb', 'Croatia')
ON CONFLICT (lokacija_id) DO NOTHING;
