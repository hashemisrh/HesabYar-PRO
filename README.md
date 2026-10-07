# حساب‌یار (HesabYar)

Personal offline-first Persian finance manager driven by bank SMS.

## Initial build stack
- Android Studio: Quail 4 / 2026.1.4 stable
- Android Gradle Plugin: 9.4.0
- Gradle: 9.6.0
- JDK: 17
- Kotlin: 2.4.20
- Compile/Target SDK: 37
- Room: 2.8.5
- Compose: 1.12.1 / Material 3

## Design
Deep teal + ivory + champagne gold, RTL, with a premium dashboard.

## SMS safety rules
- Never identify a bank by sender/short code.
- Preserve raw SMS unchanged.
- Prefer account identifier from SMS.
- Extract transaction date/time from SMS itself.
- Never guess when account, amount, date, or pattern is ambiguous.
- Ambiguous messages go to review.

## Build
Use Android Studio with JDK 17, or GitHub Actions. The workflow pins Gradle 9.6.0 and JDK 17.
