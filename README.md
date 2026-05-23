# Naeem Receipt Printer 🖨️

A professional, offline-first Android application built with **Kotlin** and **Jetpack Compose** that simplifies receipt generation and printing. It is specifically designed to connect seamlessly with Bluetooth thermal printers (supporting **58mm** and **80mm** ESC/POS standards) to print custom receipts for [naeemdocumentation.com](https://naeemdocumentation.com), documentation agencies, and freelance service providers.

The application adheres to modern **Material Design 3 (M3)** guidelines, offering a responsive, dual-theme, real-time print preview, alongside a highly persistent localized Room Database.

---

## 🎨 Visual Identity & Key Views

- **Receipt Generator (Main)**: Form-based configuration supporting customizable fields like *Amount (Rs.)*, *Service Item*, *Client Name*, and *Custom Notes*, paired with a pixel-perfect, interactive real-time print preview box that supports dynamic sizing for 58mm and 80mm rollers.
- **Branding Customizer**: Flexible Template Editor allowing you to modify and save multiple branding profiles comprising custom header texts, support phone lines, email support, websites, footer notes, and preferred paper widths.
- **Historic Logs**: View list and detail pages of previously printed or generated receipts saved securely on-device.
- **Direct Bluetooth Sync**: A direct status bar toggle linking to standard paired Bluetooth printers supporting low-latency printing.

---

## 🚀 Key Features

*   **ESC/POS Bluetooth Thermal Printing**: Zero-config automatic translation of receipt details to base thermal printing protocols.
*   **Fully-Formed Local Templates**: Save multiple business profiles (default initialized with **Naeem Documentation** details). Modify contact numbers, websites, or paper tolerances on-the-fly.
*   **Real-time Adaptive Preview**: The virtual layout box automatically scales, wraps text, and aligns elements as you type corresponding inputs.
*   **Offline-First Architecture**: Built around high-performance Android Room bindings, ensuring secure database records for every invoice without external server dependencies.
*   **Advanced Permission Management**: Integrates clean runtime Bluetooth and Location checks powered by Google's Accompanist Permissions layout library.

---

## 🛠️ Architecture & Tech Stack

This software uses Google’s recommended MVVM (Model-View-ViewModel) pattern and Clean Architecture practices:

| Layer | Technology / Library | Description |
| :--- | :--- | :--- |
| **UI & Theming** | Jetpack Compose & Material 3 | High-fidelity declarative layout engines, dynamic Dark/Light schemas, fluid animation transitions, and responsive density structures. |
| **Database** | Room Database (SQLite) | Embedded multi-table, relational data engine managed safely through typed DAOs. Supports automated migrations. |
| **Permissions** | Google Accompanist | Direct declarative runtime permission wrappers handling granular Android 12+ (API 31+) permissions gracefully (e.g. `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`). |
| **Testing** | Robolectric & Roborazzi | Isolated JVM testing of Compose layers, state flows, and visual regression layouts. |
| **Language** | Kotlin & Coroutines | Safe asynchronous threading model using `withContext(Dispatchers.IO)` and reactive Kotlin Flows. |

---

## 📦 Directory Structure

```text
/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt        # Application Entry Point & Navigation Layout
│   │   │   │   ├── PrinterService.kt      # Bluetooth Service & Socket Connection (ESC/POS)
│   │   │   │   ├── data/                  # Room Data Persistence Layer
│   │   │   │   │   ├── AppDatabase.kt     # Database setup
│   │   │   │   │   ├── ReceiptDao.kt      # DAO for Receipt entity operations
│   │   │   │   │   ├── ReceiptEntity.kt   # Database Entity model for receipts
│   │   │   │   │   ├── ReceiptRepository.kt # Central repository handling DAO queries
│   │   │   │   │   ├── TemplateDao.kt     # DAO for Brand Template entities
│   │   │   │   │   └── TemplateEntity.kt  # Database Entity model for business templates
│   │   │   │   └── ui/theme/              # Material 3 typography and palette definitions
│   │   │   └── AndroidManifest.xml        # System and Hardware permission registrations
│   └── build.gradle.kts                   # Module level dependencies and SDK ranges
├── gradle/
│   └── libs.versions.toml                 # Version Catalog containing dependency groupings
└── build.gradle.kts                       # Root level Gradle setups and settings
```

---

## 🔋 How to Print (Connection Protocol)

1. **Turn on Bluetooth**: Confirm Bluetooth is enabled on your Android device.
2. **Pair Thermal Printer**: Navigate to Android's Bluetooth system settings and pair with your external ticket/thermal printer (customary pairing PINs are usually `0000` or `1234`).
3. **App Selection**: Launch the Naeem Receipt Printer application.
4. **Connect**: Tap the **Connect** button in the top right header.
5. **Selection**: Select the mapped printer from the dynamic device dialog.
6. **Print**: Click the visual **Print Receipt** floating button to dispatch data to the thermal roller!

---

## 🧪 Testing and Quality Control

The project includes unit, integration, and UI scenario verification layouts using **Robolectric**:

*   To execute the comprehensive suite of local unit tests, run:
    ```bash
    gradle :app:testDebugUnitTest
    ```
*   To record snapshot visual references for any UI layouts built, use the integrated Roborazzi runner:
    ```bash
    gradle :app:recordRoborazziDebug
    ```

---

## 🔒 Security & Device Permissions

To ensure maximum hardware compatibility and system security, the app defines granular permission models inside `AndroidManifest.xml` depending on the host Android Operating System:

-   **Pre-Android 12 (API < 31)**: Requires location checks (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`) to browse and look up local Bluetooth peripherals.
-   **Android 12+ (API >= 31)**: Uses highly restricted modern keys (`BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`) to connect without querying location details. It additionally uses `neverForLocation` flags to maximize privacy.
