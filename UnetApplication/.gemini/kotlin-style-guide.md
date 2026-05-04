# Kotlin & Android Style Guide

Standard practices for Kotlin development and Android-specific components in this project.

## Kotlin Standards

- **Language:** Use Kotlin for all new code.
- **Version:** Targeting Kotlin 2.1.0 (check `libs.versions.toml`).
- **JVM Target:** 17.
- **Null Safety:** Leverage Kotlin's null safety. Avoid `!!` unless absolutely necessary (e.g., in tests or legacy interoperability).
- **Coroutines:** Use Coroutines for asynchronous work. Prefer `viewModelScope` in ViewModels.
- **Formatting:** Follow standard Kotlin coding conventions.

## Dependency Injection (Hilt)

- **Library:** Use Dagger Hilt for Dependency Injection.
- **Scopes:** Use `@Singleton` for app-wide components and `@ViewModelScoped` for ViewModel dependencies.
- **Modules:** Organize Hilt modules within a `di` package in each module.
- **Constructor Injection:** Prefer constructor injection over field injection.

## Android UI (View-based)

- **UI Framework:** This project uses traditional Android Views (XML layouts) and Fragments.
- **ViewBinding:** Always use ViewBinding instead of `findViewById`.
- **Fragments:** Use `fragment-ktx` for fragment transactions and navigation.
- **Resources:** Follow standard naming conventions for resources (e.g., `fragment_main.xml`, `strings.xml`).

## Lifecycle & Architecture

- **ViewModel:** Keep business logic in ViewModels. Use `LiveData` or `StateFlow` to expose state to the UI.
- **Repository Pattern:** Abstract data sources behind repositories.
- **Separation of Concerns:** Keep Activities and Fragments thin; they should only handle UI and user input.
