# Camunda Workflow - Proces odobrenja putovanja

## Pregled

Implementiran je Camunda BPM sustav za automatizaciju procesa odobrenja putovanja u Trip Planner aplikaciji.

---

## Model procesa

**Proces:** `TripApprovalProcess` - Proces odobrenja zahtjeva za putovanje

### Elementi procesa:

1. **Start Event** - Pokretanje zahtjeva za putovanje
2. **User Task** - Manager pregledava zahtjev
3. **Exclusive Gateway** - Grananje: Odobren/Odbijen
4. **Message Intermediate Catch Event** - Čekanje na poruku o booking potvrdi (VANJSKE DOGAĐAJE)
5. **Exclusive Gateway** - Booking potvrđen ili nije
6. **Loop** - Ako booking nije potvrđen, vraća se na Manager pregled (PETLJA)
7. **End Events** - Završetak procesa (odobreno ili odbijeno)

### Dijagram toka:

```
START
  ↓
Manager pregledava zahtjev (User Task)
  ↓
Odobren? (Gateway - GRANANJE)
  ├─→ NE → END (Odbijeno)
  └─→ DA → Čeka booking potvrdu (Message Event - ČEKANJE NA PORUKU)
            ↓
         Booking potvrđen? (Gateway)
            ├─→ DA → END (Odobreno)
            └─→ NE → ↰ Vraća se na Manager pregled (PETLJA)
```

---

## Ključni elementi (ispunjeni zahtjevi)

### ✅ Postojanost (Persistence)
- Camunda koristi PostgreSQL za perzistenciju stanja procesa
- Procesi mogu trajati danima - svi podaci su pohranjeni u bazi
- Ako se server restarta, procesi nastavljaju od zadnjeg stanja

### ✅ Reagiranje na vanjske događaje
- **Message Event**: `BookingConfirmationMessage` - čeka na vanjsku poruku o booking potvrdi
- Proces pauzira i čeka dok ne primi poruku
- Omogućava asinkronu komunikaciju s vanjskim sustavima

### ✅ Reference umjesto entiteta
- Proces **NE čuva cijeli Trip objekt**
- Čuva samo:
  - `tripId` - referenca na putovanje
  - `userId` - referenca na korisnika
  - `approved` - boolean za odluku
  - `bookingConfirmed` - boolean za booking status
- Stvarni entiteti ostaju u bazi, proces samo referencira

### ✅ Grananje
- Dva Exclusive Gateway-a za donošenje odluka
- Odluke temeljene na process varijablama

### ✅ Petlja
- Ako booking nije potvrđen, proces se vraća na Manager pregled
- Omogućava ponavljanje koraka

---

## API Endpoints

### 1. Pokretanje procesa
```http
POST /api/workflow/start
Content-Type: application/json

{
  "tripId": 1,
  "userId": 1
}
```

**Response:**
```json
{
  "processInstanceId": "abc-123-def",
  "tripId": 1,
  "status": "STARTED"
}
```

### 2. Dohvati sve aktivne taskove
```http
GET /api/workflow/tasks
```

**Response:**
```json
[
  {
    "taskId": "task-123",
    "taskName": "Manager pregledava zahtjev",
    "processInstanceId": "abc-123-def",
    "assignee": "manager",
    "tripId": 1,
    "userId": 1
  }
]
```

### 3. Završi task (Manager odluka)
```http
POST /api/workflow/tasks/{taskId}/complete
Content-Type: application/json

{
  "approved": true
}
```

### 4. Pošalji booking potvrdu (vanjski događaj)
```http
POST /api/workflow/message/booking-confirmation
Content-Type: application/json

{
  "processInstanceId": "abc-123-def",
  "bookingConfirmed": true
}
```

### 5. Provjeri status procesa
```http
GET /api/workflow/process/{processInstanceId}/status
```

**Response:**
```json
{
  "status": "ACTIVE",
  "processInstanceId": "abc-123-def",
  "currentTask": "Manager pregledava zahtjev",
  "variables": {
    "tripId": 1,
    "userId": 1,
    "approved": null,
    "bookingConfirmed": null
  }
}
```

---

## Korisničko sučelje

**Lokacija:** `/workflow` tab u aplikaciji

### Funkcionalnosti UI-a:

1. **Pokretanje novog procesa**
   - Unos Trip ID i User ID
   - Gumb "Pokreni proces"

2. **Lista aktivnih taskova**
   - Prikaz svih taskova koji čekaju na akciju
   - Auto-refresh svakih 3 sekunde
   - Za Manager task: gumbi "Odobri" / "Odbij"
   - Za Booking task: gumbi "Potvrdi Booking" / "Booking neuspješan"

3. **Provjera statusa procesa**
   - Unos Process Instance ID
   - Prikaz trenutnog stanja procesa i varijabli

---

## Primjer korištenja

### Scenarij 1: Uspješno odobrenje

1. Pokreni proces → `tripId: 1, userId: 1`
2. Manager pregleda → **Odobri**
3. Sustav šalje zahtjev booking agenciji (simulacija)
4. Booking agent potvrđuje → **Booking potvrđen**
5. ✅ Proces završava s uspjehom

### Scenarij 2: Odbijanje

1. Pokreni proces → `tripId: 2, userId: 1`
2. Manager pregleda → **Odbij**
3. ✅ Proces završava odmah (odbijeno)

### Scenarij 3: Booking neuspješan (PETLJA)

1. Pokreni proces → `tripId: 3, userId: 1`
2. Manager pregleda → **Odobri**
3. Booking agent → **Booking neuspješan**
4. 🔄 Proces se **vraća na Manager pregled** (petlja)
5. Manager ponovno odlučuje...

---

## Camunda Cockpit

Camunda dolazi s ugrađenim alatima:

**URL:** `http://localhost:8080/camunda`

**Login:**
- Username: `admin`
- Password: `admin`

### Dostupni alati:

- **Cockpit** - Praćenje aktivnih procesa, vizualizacija
- **Tasklist** - Lista taskova (alternativa našem custom UI-u)
- **Admin** - Upravljanje korisnicima i grupama

---

## Tehnički detalji

**Verzija:** Camunda BPM 7.20.0  
**Integracija:** Spring Boot Starter  
**Baza:** PostgreSQL (dijeli se s Trip Planner podacima)  
**Format:** BPMN 2.0 XML  

**Datoteke:**
- `trip-approval-process.bpmn` - BPMN dijagram
- `WorkflowController.java` - REST API
- `WorkflowManager.tsx` - React UI komponenta

---

## Zaključak

Sustav ispunjava sve zahtjeve:
- ✅ Persistence - PostgreSQL
- ✅ Reakcija na vanjske događaje - Message Event
- ✅ Reference umjesto entiteta - samo ID-evi
- ✅ Grananje - Exclusive Gateways
- ✅ Petlja - Vraćanje na prethodni korak
- ✅ Vizualno modeliranje - BPMN dijagram
- ✅ UI za demonstraciju - React komponenta
