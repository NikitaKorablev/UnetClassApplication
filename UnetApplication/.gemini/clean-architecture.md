# Clean Architecture & Modularization Guide

This project follows a modular Clean Architecture pattern to ensure separation of concerns, scalability, and testability.

## Module Structure

The project is divided into layers:

### 1. App Layer (`:app`)
- **Responsibility:** Entry point, Application class, MainActivity, and high-level DI configuration.
- **Dependencies:** Depends on all feature modules and core modules.

### 2. Feature Layer (`:features:*`)
- **Responsibility:** Self-contained UI features (e.g., `:features:transparency_settings`).
- **Organization:** Usually contains `presentation` (ViewModels, UI) and `di` packages.
- **Dependencies:** Depends on `:core:domain`, `:core:model`, and `:core:datastore`.

### 3. Core Layer (`:core:*`)
- **`:core:domain`**: Common business logic and utilities.
- **`:core:model`**: Shared domain models and data classes.
- **`:core:unet`**: U-Net/TFLite processing logic and ML assets.
- **`:core:datastore`**: Data persistence and repository implementations.

## Layer Principles

- **Domain First:** Business logic should be independent of UI.
- **Data Flow:** UI -> ViewModel -> UseCase (optional) -> Repository -> Data Source.
- **Module Isolation:** Avoid circular dependencies between modules. Use `:core:model` for shared data structures.

## Package Naming

Follow the base package `com.app.*` corresponding to the module name:
- `:app` -> `com.app.unetclass`
- `:core:unet` -> `com.app.unet`
- `:core:datastore` -> `com.app.datastore`
- `:features:transparency_settings` -> `com.app.transparency_settings`
