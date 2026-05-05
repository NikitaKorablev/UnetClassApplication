# UnetClass Project Index (AI-Optimized)

## 📂 Root: `D:/Repozitories/UnetClassApplication/UnetApplication`

## 🛠 Modules & Path Mapping
| Module Path | Kotlin Source Root | Package Base | Description |
|:---|:---|:---|:---|
| `:app` | `app/src/main/kotlin/` | `com.app.unetclass` | UI Entry point, MainActivity, App-level DI. |
| `:core:unet` | `core/unet/src/main/kotlin/` | `com.app.unet` | U-Net LiteRT logic, TFLite assets, processing logic. |
| `:core:model` | `core/model/src/main/kotlin/` | `com.app.model` | Shared domain models & data classes. |
| `:core:datastore` | `core/datastore/src/main/kotlin/` | `com.app.datastore` | Local persistence (Jetpack DataStore). |
| `:core:domain` | `core/domain/src/main/kotlin/` | `com.app.domain` | Common business logic/utils. |
| `:features:inference_details` | `features/inference_details/src/main/kotlin/` | `ru.unet_app.inference_details` | Inference details feature. |
| `:features:transparency_settings` | `features/transparency_settings/src/main/kotlin/` | `ru.unet_app.transparency_settings` | Transparency settings feature. |

## 🏗 Layer Structure (Internal Module Organization)
Most modules follow a Clean Architecture pattern:
- `data/`: Repositories, data sources, API/DB implementations.
- `domain/`: UseCases, business logic, domain models (if specific).
- `presentation/`: ViewModels, UI components (if applicable).
- `di/`: Koin/Dagger/Hilt module definitions.

## 🗝 Key Entry Points & Global Config
- **Application Class:** `app/src/main/kotlin/com/app/unetclass/UnetClassApplication.kt`
- **MainActivity:** `app/src/main/kotlin/com/app/unetclass/presentation/MainActivity.kt`
- **Dependencies:** `gradle/libs.versions.toml`
- **Module Config:** `settings.gradle.kts`
- **Root Build:** `build.gradle.kts`

## 🧠 AI/Neural Network Resources (`:core:unet`)
- **TFLite Models:** `core/unet/src/main/assets/`
    - `tiny_unet_v3.tflite`
    - `traced_model.pt` (PyTorch Trace)

## 💉 Dependency Injection (DI) Registry
- `:app`: `com.app.unetclass.di`
- `:core:unet`: `com.app.unet.di`
- `:core:datastore`: `com.app.datastore.di`
- `:features:inference_details`: `ru.unet_app.inference_details.di`
- `:features:transparency_settings`: `ru.unet_app.transparency_settings.di`

## 🎨 UI Resources
- **App Layouts:** `app/src/main/res/layout/`
- **Values/Strings:** `app/src/main/res/values/`
