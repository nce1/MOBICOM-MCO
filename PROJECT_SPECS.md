# Project: Bayanihan Spots

**Course Output:** MOBICOM (Mobile Computing)  
**Sections:** BSCS-ST1, BSCS-ST2, BSCS-ST3

### Contributors

- Almoradie, Nicole
- Gaffud, Jean Luc
- Labarrete, Lance Desmond

---

## 1. Application Overview

**Bayanihan Spots** maps and documents small but valuable public micro-spaces across Manila. These include barangay covered courts, pocket plazas, churchyards, rooftop community gardens, corner study cafés, water refill stations, and weekend pop-ups.

The app emphasizes community-sourced information and local relevance for Manila’s dense, multi-use public spaces.

### Core Value Proposition

- **Discover:** Find places by use-case (study, rest, play, market).
- **Navigate:** Filter and route to nearby micro-spots.
- **Inspect:** Check simple crowd/safety indicators.
- **Contribute:** Upload photos, write reviews, and propose improvements.
- **Engage:** Join local volunteer events.
- **Offline First:** Robust offline support for reading data and drafting contributions.

---

## 2. Tech Stack & Architecture

This project utilizes Android native technologies with a focus on location services and offline capabilities.

| Component            | Technology                | Implementation Notes                                                                 |
| :------------------- | :------------------------ | :----------------------------------------------------------------------------------- |
| **Maps & Geocoding** | Google Maps SDK           | **Security:** API Keys must be stored in `local.properties`, never hardcoded.        |
| **Camera**           | CameraX                   | **Optimization:** Images must be compressed (max 500KB) before upload.               |
| **Image Storage**    | Firebase Storage          | Files are cached locally first, then uploaded via background job.                    |
| **Local Database**   | Room / SQLite             | **Single Source of Truth:** UI observes Room; Room syncs with Firestore.             |
| **Backend**          | Firebase Firestore + Auth | Handles user profiles, master spot list, and event data.                             |
| **Background Jobs**  | WorkManager               | Handles deferred uploads of photos/reviews when constraints (Internet/WiFi) are met. |

---

## 3. Functional Requirements

### User Management

- [ ] **Register/Login:** - Standard Email/Password or Google Sign-In.
  - **Guest Mode:** Allows browsing the map without an account, but restricts contributing.

### Discovery & Mapping

- [ ] **View Spot List/Discover Map:**
  - View map pins or list of micro-spots.
  - **Seeding:** The database will be pre-populated with ~20 curated spots (Manila/DLSU area) for initial demo purposes.
- [ ] **Filter Spots:**
  - Filter by Category (Study, Rest, Play, Market) or Facilities (Shade, WiFi, Outlets).
- [ ] **View Spot Details:**
  - Photo carousel, description, accessibility tags, crowd indicator.
  - **Actions:** Check-in, Favorite, Propose Improvement, Report.

### Engagement & Contribution

- [ ] **Smart Check-in:**
  - User logs a visit.
  - **Logic:** Uses **Geofencing Radius (~50m-100m)** to validate location. Does _not_ require exact GPS coordinate match to account for drift.
- [ ] **Take Photo & Upload:**
  - Captures spot conditions.
  - **Constraint:** Photos taken offline are saved locally and queued for upload via WorkManager.
- [ ] **Propose Improvement:**
  - Users submit text/photo suggestions. These are saved as "Drafts" locally if offline.

### Volunteer & Events

- [ ] **View Program List:**
  - Read-only list of Admin-curated events (Cleanup drives, Pop-ups).
- [ ] **RSVP/Join:**
  - Authenticated users can click "Join."
  - **Notification:** Local notifications scheduled for 3 days and 1 day before the event.
- [ ] **Event Management (Scope-Limited):**
  - _Priority:_ Admin-created events only.
  - _Stretch Goal:_ User-created events (only if time permits).

---

## 4. UI & Design System

**Theme:** "Urban Oasis"
_A calm, nature-inspired palette emphasizing safety, rest, and clarity._

| UI Element    | Color Name | Hex Code  | Usage                                                |
| :------------ | :--------- | :-------- | :--------------------------------------------------- |
| **Primary**   | Teal Green | `#00695C` | App Bar, Headers, Brand Identity                     |
| **Secondary** | Sage/Mint  | `#B2DFDB` | Selected states, chips, subtle backgrounds           |
| **Accent**    | Coral Red  | `#FF5252` | FAB (Floating Action Button), Call-to-Action, Alerts |
| **Surface**   | Off-White  | `#FAFAFA` | Card backgrounds, list items                         |
| **Text**      | Charcoal   | `#212121` | Primary text (readable on off-white)                 |

### Map Pin Coding

_Use specific colors to denote category types on the map:_

- **Study / Quiet:** Blue (`#1E88E5`)
- **Play / Active:** Orange (`#FB8C00`)
- **Nature / Garden:** Green (`#43A047`)
- **Food / Market:** Red (`#E53935`)

---

## 5. Development Guidelines & Constraints

_Use these rules when generating code._

### A. Offline Sync Strategy (Simplicity First)

To avoid complex conflict resolution:

1.  **Reading:** The Map always reads from the local Room database. Firestore updates the Room database in the background.
2.  **Editing:** Users cannot edit _existing_ public spots offline.
3.  **Creating:** New Spots, Reviews, or Photos created offline are stored as **"Pending Uploads"** in a separate local table. WorkManager uploads them when online.

### B. Image Handling

- Do not upload full-resolution CameraX bitmaps.
- Implement a `BitmapUtils` helper to resize/compress images to JPEG < 500KB before passing them to Firebase Storage.

### C. Map Security

- Use the **Secrets Gradle Plugin** or `local.properties` to inject the Google Maps API Key.
- Do not commit keys to version control.

### D. Data Seeding

- Include a `RoomCallback` or a JSON parser to seed the local database with dummy data on the first app launch so the map is never empty during testing.
