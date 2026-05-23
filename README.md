# ND-QuickReceipt 🖨️

A production-ready Android application built with **Kotlin** and **Jetpack Compose** for generating and printing thermal receipts via Bluetooth. Designed for [Naeem Documentation](https://naeemdocumentation.com) and adaptable to any documentation agency or freelance service provider.

Supports **58mm** and **80mm** ESC/POS thermal printers, follows **Material Design 3** guidelines, and stores all receipt and template data fully on-device using Room.

---

## Screenshots & Key Views

| Screen | Description |
| :--- | :--- |
| **New Print** | Form with Amount, Service, Client Name, and Notes fields. Live print preview updates as you type, scaled to selected paper width. |
| **Customize** | Template editor for managing multiple branding profiles — store name, phone, email, website, footer text, and paper size. |
| **History** | Running log of all printed receipts with totals analytics. Stores up to 50 recent entries shown in-app. |

---

## Features

- **ESC/POS Bluetooth Printing** — Raw byte protocol over RFCOMM socket for reliable thermal printer communication.
- **Multiple Branding Templates** — Create, save, and switch between business profiles. Default template is pre-seeded with Naeem Documentation details.
- **Live Print Preview** — The preview pane reflects your input in real time and dynamically resizes for 58mm vs 80mm paper.
- **Offline-First** — All data (receipts and templates) stored locally via Room. No network dependency whatsoever.
- **Dark / Light Mode** — Manual theme toggle in the app bar, defaulting to system preference.
- **Crash Diagnostics** — On app launch, if a previous crash occurred, the stack trace is surfaced in a dismissible dialog for debugging.
- **Permission Handling** — Bluetooth permissions handled correctly across API levels: location-based discovery on API < 31, `BLUETOOTH_CONNECT` / `BLUETOOTH_SCAN` (with `neverForLocation`) on API 31+.

---

## Architecture & Tech Stack

Follows the **MVVM** pattern with a Repository layer over Room DAOs.

| Layer | Library | Notes |
| :--- | :--- | :--- |
| **UI** | Jetpack Compose + Material 3 | Declarative layout, dark/light theming, animated content size transitions |
| **Database** | Room (SQLite) | Two entities: `receipts` and `templates`. `fallbackToDestructiveMigration` enabled — migrations are destructive for now |
| **Async** | Kotlin Coroutines + Flow | IO dispatched off main thread; UI state collected via `collectAsState` |
| **Permissions** | Accompanist Permissions | Declarative runtime permission wrappers |
| **Printing** | Custom `PrinterService` | RFCOMM socket over SPP UUID; raw ESC/POS byte commands for alignment, content, and paper cut |
| **Testing** | Robolectric + Roborazzi | JVM-based Compose tests and snapshot infrastructure |
| **Language** | Kotlin 2.2 | KSP used for Room and Moshi codegen |

---

## Project Structure

```
/
├── app/
│   └── src/
│       └── main/
│           └── java/com/example/
│               ├── MainActivity.kt          # App entry point, Compose host, nav
│               ├── PrinterService.kt        # Bluetooth connection + ESC/POS output
│               └── data/
│                   ├── AppDatabase.kt       # Room database definition (v2)
│                   ├── ReceiptDao.kt        # Receipt CRUD
│                   ├── ReceiptEntity.kt     # Receipt schema
│                   ├── ReceiptRepository.kt # Repository combining both DAOs
│                   ├── TemplateDao.kt       # Template CRUD + selection
│                   └── TemplateEntity.kt    # Branding template schema
└── gradle/
    └── libs.versions.toml                   # Version catalog
```

---

## How to Print

1. Enable Bluetooth on your Android device.
2. Pair your thermal printer via Android Bluetooth settings (default PINs are usually `0000` or `1234`).
3. Open the app and tap **Connect** in the top bar.
4. Select your printer from the paired device list.
5. Fill in receipt details on the **New Print** tab.
6. Tap **Print Receipt** — data is sent over the Bluetooth socket and the receipt is saved to local history.

> If the printer is already paired but the connection fails, try toggling Bluetooth off and on. Some thermal printers drop idle RFCOMM connections.

---

## Bluetooth Permissions

The app declares and handles permissions across both legacy and modern Bluetooth APIs:

| API Level | Permissions Required |
| :--- | :--- |
| **API < 31** (Android 11 and below) | `BLUETOOTH`, `BLUETOOTH_ADMIN`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` |
| **API 31+** (Android 12+) | `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN` (with `neverForLocation` — location is not accessed) |

---

## Running Tests

Unit and snapshot tests run on the JVM via Robolectric.

```bash
# Run all local unit tests
./gradlew :app:testDebugUnitTest

# Record Roborazzi screenshot baselines
./gradlew :app:recordRoborazziDebug

# Verify screenshots against recorded baselines
./gradlew :app:verifyRoborazziDebug
```

---

## Known Limitations

- **No delete in History** — `deleteReceiptById` is implemented in the DAO but not exposed in the UI yet.
- **Destructive migrations** — The database uses `fallbackToDestructiveMigration`. A schema change will wipe all stored receipts and templates.
- **Single connection** — Only one printer can be connected at a time. Switching printers requires disconnecting first.
- **No invoice numbering** — Receipts are stored with a timestamp but no sequential invoice number is generated.

---

## 📄 License

This project is open-source and available for educational and commercial use under the MIT License.

---

**Made with ❤️ by [Abdul Hayy Khan](https://www.linkedin.com/in/abdulhayykhan/)**