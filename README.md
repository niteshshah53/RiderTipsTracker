# Rider Tips Tracker 🚴‍♂️💰

A comprehensive Android application designed for delivery riders to track their shifts, tips, and earnings across multiple platforms. Built with modern Android development practices and Jetpack Compose.

## 📱 About the App

**Rider Tips Tracker** is an offline-first mobile application that helps delivery riders efficiently manage their work schedules, track earnings, analyze performance, and make data-driven decisions. Whether you work for Uber Eats, Lieferando, Flink, or any other delivery platform, this app provides all the tools you need to stay on top of your finances and optimize your work schedule.

### Key Features

- **📊 Shift Management**: Easily add, edit, and track your delivery shifts with automatic calculations
- **📈 Performance Analytics**: View detailed reports with charts showing tips over time, weekday comparisons, and cash vs online breakdowns
- **🎯 Goal Setting**: Set daily, weekly, or monthly earning goals and track your progress
- **📅 Shift Scheduler**: Create recurring shift templates to plan your work schedule
- **🔮 Tips Prediction**: AI-powered prediction engine to forecast next week's earnings
- **💾 Data Management**: Import/export data via CSV, backup/restore functionality
- **🌙 Dark Mode**: Beautiful dark theme support for comfortable viewing
- **📱 Offline-First**: All data stored locally - works without internet connection

## 🎯 How It's Useful

### For Delivery Riders

1. **Financial Tracking**: Keep accurate records of all your earnings, tips, and hours worked
2. **Performance Analysis**: Identify your best performing days, shifts, and times to maximize earnings
3. **Goal Achievement**: Set realistic earning targets and track progress toward financial goals
4. **Schedule Optimization**: Use historical data and predictions to plan the most profitable shifts
5. **Tax Preparation**: Export all your data for easy tax filing and record keeping
6. **Multi-Platform Support**: Track earnings from multiple delivery platforms in one place

### Business Value

- **Data-Driven Decisions**: Make informed choices about when and where to work
- **Time Management**: Optimize your schedule based on historical performance
- **Financial Planning**: Set and achieve earning goals
- **Record Keeping**: Maintain professional records of all work activities

## 🛠️ Technologies & Skills Demonstrated

This project showcases proficiency in modern Android development and various technical skills:

### **Core Android Development**

- **Kotlin**: Primary programming language for Android development
- **Jetpack Compose**: Modern declarative UI framework for building native Android interfaces
- **Material Design 3**: Implementation of Google's latest design system
- **Android Architecture Components**: Following MVVM (Model-View-ViewModel) architecture pattern

### **Architecture & Design Patterns**

- **MVVM Architecture**: Clean separation of concerns with ViewModels managing UI logic
- **Repository Pattern**: Abstraction layer for data access
- **Dependency Injection**: Using Hilt for managing dependencies
- **Reactive Programming**: Kotlin Flow and StateFlow for reactive data streams

### **Data Management**

- **Room Database**: Local SQLite database with type-safe queries
- **DataStore**: Modern data storage solution for preferences
- **Type Converters**: Custom converters for LocalDate and LocalTime
- **Database Migrations**: Handling schema changes and version upgrades

### **UI/UX Development**

- **Compose UI**: Building complex, responsive user interfaces
- **Material 3 Components**: Cards, Buttons, TextFields, Navigation Drawer, etc.
- **State Management**: Managing UI state with Compose State and ViewModels
- **Navigation**: Jetpack Navigation Compose for screen navigation
- **Charts & Visualization**: Data visualization for reports and analytics

### **Asynchronous Programming**

- **Kotlin Coroutines**: Managing background operations and async tasks
- **Flow API**: Reactive streams for data observation
- **StateFlow**: State management with reactive updates
- **Suspend Functions**: Proper handling of async operations

### **File Operations**

- **CSV Import/Export**: Reading and writing CSV files
- **File Picker**: Using ActivityResultContracts for file selection
- **JSON Backup/Restore**: Data serialization and deserialization
- **Content Providers**: Accessing files through Android's content system

### **Business Logic**

- **Prediction Engine**: Implementing time-series analysis and regression algorithms
- **Statistical Calculations**: Weighted moving averages, standard deviation, confidence intervals
- **Data Aggregation**: Grouping and summarizing data by various criteria
- **Date/Time Handling**: Complex date calculations and formatting

### **Testing & Quality**

- **Type Safety**: Leveraging Kotlin's type system for compile-time safety
- **Error Handling**: Proper exception handling and user feedback
- **Code Organization**: Clean code structure with separation of concerns

### **Android System Integration**

- **Notifications**: Creating and managing system notifications
- **Permissions**: Handling runtime permissions (notifications, storage)
- **Activity Results**: Modern approach to handling activity results
- **Lifecycle Awareness**: Proper handling of Android component lifecycles

### **Development Tools**

- **Gradle**: Build system configuration and dependency management
- **Hilt**: Dependency injection framework
- **KAPT**: Kotlin annotation processing
- **Git**: Version control and collaboration

## 📋 Project Structure

```
app/src/main/java/com/example/ridertipstracker/
├── data/
│   ├── local/
│   │   ├── dao/          # Data Access Objects for Room
│   │   ├── db/           # Database configuration
│   │   ├── entity/       # Room entities
│   │   └── datastore/    # Preferences storage
│   └── model/            # Data models
├── repository/            # Repository layer
├── ui/
│   ├── addshift/         # Add/Edit shift screen
│   ├── dashboard/        # Main dashboard
│   ├── goals/            # Goals management
│   ├── reports/          # Reports and charts
│   ├── prediction/       # Tips prediction
│   ├── scheduler/        # Shift scheduler
│   ├── settings/         # Settings screen
│   ├── backup/           # Backup/restore
│   ├── importexport/     # CSV import/export
│   └── theme/            # App theming
├── utils/                # Utility classes
├── di/                   # Dependency injection modules
└── MainActivity.kt       # Main activity
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17 or higher
- Android SDK (API level 24+)
- Kotlin 1.9+

### Installation

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run on an emulator or physical device

### Building

```bash
./gradlew assembleDebug
```

## 📊 Key Features Explained

### Dashboard
- Real-time summary of today's earnings
- Weekly and monthly summaries with trend indicators
- Last 3 days tips breakdown
- Weekly daily breakdown
- Upcoming shifts display

### Reports
- Customizable date range filtering
- Tips over time chart
- Weekday comparison chart
- Cash vs Online tips breakdown
- Weekly and monthly summaries

### Goals
- Set daily, weekly, or monthly earning goals
- Visual progress tracking with progress bars
- Real-time progress calculation

### Prediction Engine
- Forecasts next week's earnings
- Provides confidence levels
- Explains prediction factors
- Uses historical data analysis

### Shift Scheduler
- Create recurring shift templates
- Plan weekly schedules
- Toggle shifts active/inactive

### Data Management
- CSV import/export
- JSON backup/restore
- Data validation and error handling

## 🎓 Learning Outcomes

By studying this project, developers can learn:

1. **Modern Android Development**: Latest practices and libraries
2. **Jetpack Compose**: Building UIs declaratively
3. **Architecture Patterns**: MVVM and Repository patterns
4. **Database Design**: Room database schema design
5. **Reactive Programming**: Using Flow and StateFlow
6. **Business Logic**: Implementing complex calculations
7. **File Handling**: CSV and JSON operations
8. **UI/UX Design**: Creating intuitive user interfaces
9. **State Management**: Managing complex app state
10. **Testing Strategies**: Writing testable code

## 👨‍💻 Developer

**Nitesh Kumar Shah**
- Email: kshahnitesh@gmail.com
- Website: In progress

## 📄 License

This project is developed for educational and personal use.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

## 📝 Version

**Current Version**: 1.0.0

---

**Built with ❤️ using Kotlin and Jetpack Compose**

