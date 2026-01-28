# TrackSpend

**TrackSpend** is an Android app that helps you **track your packages and monitor your spending** in one place.  
It combines package tracking with personal expense insights, so you know **where your orders are** and **how much you’re spending** every month.

---

## Features

### Package Tracking
- Add packages manually or via **Smart Import**
- View all your packages in a clean, modern dashboard
- Edit, delete, or pin packages for quick access
- Track shipments using a live tracking API

### Smart Import (Email Parsing)
- Paste order confirmation emails directly into the app
- Automatically extracts:
  - Tracking number (if present)
  - Carrier
  - Store name
  - Item name
  - Price
  - Order date
- Implemented using **custom regex-based parsing**
- No AI / ML involved — fully local and deterministic
- ~70% accuracy depending on email format

> Smart Import is intentionally lightweight and privacy-friendly — no external processing or AI services.

---

### Spending Analytics
- Monthly spending overview
- Visualized using:
  - Line graph
  - Bar chart
  - Radial (donut) chart
- Helps you understand ordering habits and trends over time

---

##  App Screenshots

| Home | Add Package | Package Details | Tracking | Stats |
|-----|------------|----------------|---------|------|
| ![Home](images/home.png) | ![Add](images/add.png) | ![Details](images/details.png) | ![EmailContent](images/email-content.png) | ![Tracking](images/tracking.png) | ![Stats](images/stats.png) | ![RadialCircle](images/radial-circle.png) |


---

## Download APK

You can install the app directly on an Android device:

👉 **[Download Latest APK](https://github.com/USERNAME/REPO/releases/latest)**

> ⚠️ Android will ask you to allow installs from unknown sources.

---

##  Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM
- **Local Storage:** Room Database
- **Networking:** REST API (17Track)
- **State Management:** Kotlin Flow / State
- **Charts:** Compose-based custom visualizations

---

