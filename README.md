# FairSplit - Expense Tracker App

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com/)

A modern Android expense tracking application built with Kotlin, Jetpack Compose, and Firebase. Track personal expenses, view spending history, and analyze your financial data with beautiful charts.

## 📱 Features

### ✅ Implemented
- **User Authentication** 
  - Email/Password authentication
  - Google Sign-In integration
  - Firebase Authentication
  
- **Expense Management**
  - Add personal expenses with categories
  - Modern, intuitive UI with custom numeric keypad
  - Category selection with color-coded icons
  - Date picker for expense tracking
  - Real-time data sync with Firebase

- **Dashboard & History**
  - Monthly expense overview
  - Financial history with month-end archiving
  - Beautiful charts using Vico library
  - Income and expense tracking

- **Data Persistence**
  - Room Database (primary storage)
  - Firestore sync (optional, works offline)
  - Automatic month-end data archiving

### 🚧 Planned Features
- Group expense splitting
- Receipt camera capture
- Export data (PDF/Excel)
- Budget tracking and alerts
- Multi-currency support

## 🏗️ Architecture

This project follows **Clean Architecture** principles with **MVVM** pattern:

```
app/
├── data/
│   ├── local/          # Room database entities & DAOs
│   ├── remote/         # Firebase/Firestore integration
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Domain models
│   └── repository/     # Repository interfaces
├── ui/
│   ├── screens/        # Composable screens
│   ├── components/     # Reusable UI components
│   ├── theme/          # Material3 theming
│   └── navigation/     # Navigation setup
└── di/                 # Hilt dependency injection
```

## 🛠️ Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM + Clean Architecture
- **Dependency Injection:** Hilt
- **Local Database:** Room
- **Cloud Storage:** Firebase Firestore
- **Authentication:** Firebase Auth
- **Charts:** Vico
- **Async:** Kotlin Coroutines & Flow
- **Testing:** JUnit, MockK, Espresso

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- JDK 17 or later
- Android SDK (API 24+)
- Firebase project (for authentication and cloud features)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/abhishek-atole/fairsplit-expense-tracker.git
   cd fairsplit-expense-tracker
   ```

2. **Firebase Configuration**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Download `google-services.json`
   - Place it in `app/` directory
   - Enable Authentication (Email/Password and Google Sign-In)
   - Enable Firestore Database

3. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run tests**
   ```bash
   ./gradlew test                    # Unit tests
   ./gradlew connectedAndroidTest    # Instrumented tests
   ```

## 📦 Build Variants

- **Debug:** Development build with debugging enabled
- **Release:** Production build with ProGuard/R8 optimization

## 🧪 Testing

The project includes:
- **Unit Tests:** ViewModels, Repositories, Use Cases (33+ tests)
- **UI Tests:** Espresso tests for critical user flows
- **Code Coverage:** Minimum 80% coverage target

Run all tests:
```bash
./gradlew test connectedAndroidTest
```

## 📝 Code Style

This project follows:
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Material Design 3 guidelines

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'feat: Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Commit Message Convention
Follow [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `refactor:` Code refactoring
- `test:` Test updates
- `chore:` Build/config changes

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Abhishek Atole**
- GitHub: [@abhishek-atole](https://github.com/abhishek-atole)

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Firebase](https://firebase.google.com/)
- [Vico Charts](https://github.com/patrykandpatrick/vico)
- [Hilt](https://dagger.dev/hilt/)

## 📞 Support

For support, email abhishek.atole@example.com or open an issue in this repository.

---

Made with ❤️ using Kotlin & Jetpack Compose
