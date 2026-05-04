# Android Testing Guide

Guidelines for testing the UnetClass application.

## Testing Stack

- **Unit Testing:** JUnit 4.
- **UI Testing:** Espresso.
- **Mocking:** Use MockK or Mockito (check current project usage).
- **Hilt Testing:** Use `HiltAndroidTest` for integration tests that require DI.

## Best Practices

- **Location:**
    - Unit tests: `src/test/`
    - Instrumentation/UI tests: `src/androidTest/`
- **Naming:** Test classes should end with `Test` (e.g., `SegmentationUseCaseTest`).
- **Isolation:** Tests should be independent and not rely on previous test states.
- **ViewModel Testing:** Use `InstantTaskExecutorRule` for LiveData testing.
- **Repository Testing:** Mock data sources or use in-memory databases/datastores where possible.

## Execution

- Run unit tests: `./gradlew test`
- Run instrumentation tests: `./gradlew connectedAndroidTest`
