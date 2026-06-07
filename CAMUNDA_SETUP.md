# Camunda Workflow - Upute za pokretanje

## Pokretanje

### 1. Build projekta
```bash
.\mvnw.cmd clean install -DskipTests
```

### 2. Pokreni backend
```bash
.\mvnw.cmd spring-boot:run -pl presentation
```

**Pričekaj da vidis:**
```
✓ Successfully deployed Trip Approval Process
Started TripPlannerApplication in X.XXX seconds
```

Backend: `http://localhost:8080`  
Camunda Cockpit: `http://localhost:8080/camunda` (admin/admin)

### 3. Pokreni frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend: `http://localhost:5173`

---

## Testiranje workflow-a

1. **Otvori aplikaciju** → Klikni na **"Workflow (Camunda)"** tab
2. **Pokreni proces:**
   - Unesi Trip ID (npr. `1`)
   - Unesi User ID (npr. `1`)
   - Klikni "Pokreni proces"
   - Zapiši Process Instance ID koji dobiješ!
3. **Manager pregledava:**
   - Pojavi se task "Manager pregledava zahtjev"
   - Klikni "Odobri" ili "Odbij"
4. **Ako je odobren:**
   - Proces čeka na booking potvrdu
   - Klikni "Potvrdi Booking" ili "Booking neuspješan"
5. **Ako je booking neuspješan:**
   - Proces se vraća na Manager (petlja) ♻️

---

## Provjera u Camunda Cockpit

1. Otvori `http://localhost:8080/camunda`
2. Login: `admin` / `admin`
3. Klikni "Cockpit"
4. Vidi sve aktivne procese i njihovo stanje
5. Vizualiziraj gdje se proces trenutno nalazi

---

## Elementi procesa

✅ **Grananje** - Odobren/Odbijen gateway  
✅ **Čekanje na poruku** - Message Event za booking potvrdu  
✅ **Petlja** - Vraćanje na Manager ako booking nije potvrđen  
✅ **Persistence** - Sve se čuva u PostgreSQL bazi  
✅ **Reference** - Čuvaju se samo ID-evi, ne cijeli objekti
