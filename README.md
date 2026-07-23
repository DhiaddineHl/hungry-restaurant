# Hungry Restaurant POS (Sunmi)

A native Android **POS terminal app for the restaurant side of a food-delivery
service**, built for **Sunmi** hardware (T2 / T3 / D3 / V2s and similar). Staff
triage incoming delivery-app orders, advance them through the kitchen, print
tickets on the built-in Sunmi thermal printer, and review daily metrics.

> Screens follow the *Modern Food Delivery Redesign* concept: Partner Login,
> Active Orders, Order Details, Order History, and Metrics Overview.

## Tech choices

| Area | Choice | Why |
|------|--------|-----|
| Language | **Kotlin** | Google's default for Android; matches Sunmi's modern SDK samples. |
| UI | **Jetpack Compose** + Material 3 | Declarative, fast to iterate, theme-aware (light/dark). |
| Architecture | MVVM + repository, manual DI | ViewModels expose `StateFlow`; screens are stateless. |
| Data | In-memory mock repository | Swap `MockOrderRepository`/`MockAuthRepository` for REST/Firebase later — the interfaces don't change. |
| Printing | **`com.sunmi:printerlibrary`** | Official Sunmi client for the built-in printer service; wraps the AIDL binding so no `.aidl` files are vendored in-app. |
| Min / Compile SDK | 24 / 35 | Covers older Sunmi firmware while building against a modern SDK. |

## Project layout

```
app/src/main/
└─ java/com/hungry/restaurant/pos/
   ├─ HungryPosApp.kt               # Application + DI container bootstrap; binds printer
   ├─ MainActivity.kt              # Compose host (edge-to-edge)
   ├─ di/AppContainer.kt           # App-scoped singletons
   ├─ data/
   │  ├─ model/                    # Order, OrderItem, DeliveryPlatform, DashboardMetrics, Money…
   │  └─ repository/               # OrderRepository / AuthRepository (+ mock impls)
   ├─ printer/
   │  ├─ SunmiPrinter.kt           # Service binding + coroutine-friendly print API
   │  └─ ReceiptFormatter.kt       # Lays out an Order as a thermal receipt
   └─ ui/
      ├─ theme/                    # Colors, typography, Material 3 theme
      ├─ components/               # OrderCard, StatusPill, PlatformChip
      ├─ navigation/               # Routes + NavHost + bottom bar
      └─ screens/                  # login / active / details / history / metrics
```

## Sunmi printer integration

`SunmiPrinter` binds to the inner-printer service via the official client library:

```kotlin
implementation("com.sunmi:printerlibrary:1.0.23") // resolved from maven.sunmi.com, see settings.gradle.kts

InnerPrinterManager.getInstance().bindService(context, innerPrinterCallback)
```

- On a real Sunmi device the service binds and `status` becomes `CONNECTED`.
- On an emulator / non-Sunmi device it can't bind, `status` becomes
  `UNAVAILABLE`, and print calls return a `Result.failure` instead of crashing —
  so the whole app is fully usable for development without hardware.

`ReceiptFormatter` builds a 58 mm ticket (header, items + modifiers, totals,
customer note, prep target) using the service's text/column/alignment commands,
wrapped in a printer buffer transaction and finished with `cutPaper` (ignored on
devices without a cutter).

## Build & run

Prerequisites: JDK 17, Android SDK with platform 35 + build-tools 35.

```bash
./gradlew assembleDebug          # build the debug APK
./gradlew installDebug           # install onto a connected Sunmi device / emulator
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

**Demo login:** any email + a password of 4+ characters.

## Swapping in a real backend

Implement `OrderRepository` / `AuthRepository` (e.g. Retrofit or Firebase) and
change the two lines in `di/AppContainer.kt`. No screen or ViewModel changes are
required.

## Design assets

The Stitch *Modern Food Delivery Redesign* export referenced only screen IDs (no
hosted asset URLs), so the current UI is a faithful interpretation of the five
screen names with a modern delivery aesthetic. Drop in the exported images/HTML
to fine-tune spacing, imagery, and exact colors.
