# Отчет о добавлении отображения сохраненных предиктов при запуске приложения (Обновленный)

## Описание задачи
Требуется, чтобы при запуске приложения отображались сохраненные ранее предикты из директории `Pictures/UnetClass/`, а также реализовать проверку корректности этих предиктов.

## Анализ текущей реализации
1. **MainActivity.kt** - управляет основной логикой приложения, включая историю предиктов
2. **HistoryAdapter.kt** - адаптер списка истории, уже имеет метод `updateItems` для загрузки множества элементов
3. **ImageSaver.kt** - отвечает за сохранение результатов в директорию `Pictures/UnetClass/` с временной меткой
4. **PredictionHistoryItem.kt** - модель данных для элемента истории

## Предлагаемые изменения с учетом принципов SOLID

### 1. Создать интерфейс для поиска предиктов

#### IPredictionHistoryRepository.kt
```kotlin
package com.app.unetclass.domain.repository

import com.app.unetclass.models.PredictionHistoryItem
import android.content.Context

interface IPredictionHistoryRepository {
    suspend fun getAllSavedPredictions(context: Context): List<PredictionHistoryItem>
}
```

### 2. Реализация репозитория для файловой системы

#### FilePredictionHistoryRepository.kt
```kotlin
package com.app.unetclass.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Environment
import com.app.unetclass.domain.repository.IPredictionHistoryRepository
import com.app.unetclass.models.PredictionHistoryItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FilePredictionHistoryRepository : IPredictionHistoryRepository {
    private companion object {
        const val NUM_CLASSES = 6
    }
    
    override suspend fun getAllSavedPredictions(context: Context): List<PredictionHistoryItem> {
        val directories = getAllPredictionDirectories(context)
        val historyItems = mutableListOf<PredictionHistoryItem>()
        
        for (directory in directories) {
            val historyItem = createHistoryItemFromDirectory(directory)
            if (historyItem != null) {
                historyItems.add(historyItem)
            }
        }
        
        // Сортируем по убыванию времени (новые сверху)
        return historyItems.sortedByDescending { it.timestamp }
    }
    
    private fun getAllPredictionDirectories(context: Context): List<File> {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val unetClassDir = File(picturesDir, "UnetClass")
        if (!unetClassDir.exists()) return emptyList()
        
        return unetClassDir.listFiles { file -> file.isDirectory }?.toList() ?: emptyList()
    }
    
    private fun isPredictionDirectoryValid(directory: File): Boolean {
        // Проверяем наличие всех необходимых файлов
        val requiredFiles = listOf("united_mask.png") + 
            (0 until NUM_CLASSES).map { "class_$it.png" }
        
        for (fileName in requiredFiles) {
            val file = File(directory, fileName)
            if (!file.exists()) {
                return false
            }
        }
        
        // Проверяем, что все изображения имеют одинаковые размеры
        val dimensionsList = mutableListOf<Pair<Int, Int>>()
        for (fileName in requiredFiles) {
            val filePath = File(directory, fileName).absolutePath
            val dimensions = getImageDimensions(filePath)
            if (dimensions == null) {
                return false
            }
            dimensionsList.add(dimensions)
        }
        
        // Проверяем, что все измерения одинаковы
        if (dimensionsList.distinct().size > 1) {
            return false
        }
        
        return true
    }
    
    private fun getImageDimensions(filePath: String): Pair<Int, Int>? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        return if (options.outWidth > 0 && options.outHeight > 0) {
            Pair(options.outWidth, options.outHeight)
        } else {
            null
        }
    }
    
    private fun createHistoryItemFromDirectory(directory: File): PredictionHistoryItem? {
        if (!isPredictionDirectoryValid(directory)) {
            return null
        }
        
        // Извлекаем временную метку из имени папки
        val timestamp = directory.name
        val outputPath = directory.absolutePath
        
        return PredictionHistoryItem(
            timestamp = timestamp,
            executionTime = 0, // Точное время выполнения недоступно из файловой системы
            outputPath = outputPath
        )
    }
}
```

### 3. Создать UseCase для получения сохраненных предиктов

#### GetSavedPredictionsUseCase.kt
```kotlin
package com.app.unetclass.domain.usecases

import com.app.unetclass.domain.repository.IPredictionHistoryRepository
import com.app.unetclass.models.PredictionHistoryItem
import android.content.Context

class GetSavedPredictionsUseCase(
    private val repository: IPredictionHistoryRepository
) {
    suspend operator fun invoke(context: Context): List<PredictionHistoryItem> {
        return repository.getAllSavedPredictions(context)
    }
}
```

### 4. Изменить MainActivity.kt чтобы использовать UseCase

```kotlin
// Добавить импорты
import com.app.unetclass.domain.repository.IPredictionHistoryRepository
import com.app.unetclass.data.repository.FilePredictionHistoryRepository
import com.app.unetclass.domain.usecases.GetSavedPredictionsUseCase

// Добавить в поля класса MainActivity
private lateinit var getSavedPredictionsUseCase: GetSavedPredictionsUseCase

// В методе onCreate() после инициализации других компонентов:
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.predictBtn.isEnabled = false // Кнопка "Predict" отключена по умолчанию
    binding.moreInfoButton.isEnabled = false
    model = UnetModel(applicationContext)

    // Инициализация новых компонентов
    timeMeasurementService = TimeMeasurementService()
    presenter = SegmentationPresenter(model, timeMeasurementService)
    
    // Инициализация репозитория и UseCase
    val predictionHistoryRepository: IPredictionHistoryRepository = FilePredictionHistoryRepository()
    getSavedPredictionsUseCase = GetSavedPredictionsUseCase(predictionHistoryRepository)

    // Инициализация RecyclerView и адаптера
    initRecyclerView()
    
    // Загрузка сохраненных предиктов из файловой системы
    loadSavedPredictions()

    binding.selectImageBtn.setOnClickListener {
        pickImage.launch("image/*")
    }

    binding.predictBtn.setOnClickListener {
        bitmap?.let {
            runSegmentation(it)
        }
    }

    binding.moreInfoButton.setOnClickListener {
        selectedHistoryItem?.let { historyItem ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("prediction_path", historyItem.outputPath)
            }
            startActivity(intent)
        } ?: run {
            Toast.makeText(this, "Please select a prediction first", Toast.LENGTH_SHORT).show()
        }
    }
}

// Добавить новый метод:
private fun loadSavedPredictions() {
    lifecycleScope.launch(Dispatchers.IO) {
        try {
            val savedPredictions = getSavedPredictionsUseCase(this@MainActivity)
            withContext(Dispatchers.Main) {
                historyAdapter.updateItems(savedPredictions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity, 
                    "Error loading saved predictions: ${e.message}", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
```

## Обоснование изменений с учетом принципов SOLID

### 1. Single Responsibility Principle (SRP)
- `FilePredictionHistoryRepository` отвечает только за работу с файловой системой и получение списка предиктов
- `GetSavedPredictionsUseCase` отвечает только за выполнение конкретной бизнес-логики получения сохраненных предиктов
- `MainActivity` остается сфокусированной на задачах представления (UI)

### 2. Open/Closed Principle (OCP)
- Через интерфейс `IPredictionHistoryRepository` можно легко добавить другие источники данных (например, базу данных)

### 3. Liskov Substitution Principle (LSP)
- Любая реализация `IPredictionHistoryRepository` может использоваться вместо текущей

### 4. Interface Segregation Principle (ISP)
- Интерфейс репозитория содержит только один метод, соответствующий конкретной ответственности

### 5. Dependency Inversion Principle (DIP)
- `MainActivity` зависит от абстракции (UseCase и интерфейса репозитория), а не от конкретных реализаций

## Технические детали реализации

1. **FilePredictionHistoryRepository** - класс, реализующий логику поиска и проверки сохраненных предиктов в файловой системе:
   - `getAllPredictionDirectories()` - находит все подкаталоги в `Pictures/UnetClass/`
   - `isPredictionDirectoryValid()` - проверяет, что папка содержит все необходимые файлы и они имеют одинаковые размеры
   - `getImageDimensions()` - получает размеры изображения из файла
   - `createHistoryItemFromDirectory()` - создает объект PredictionHistoryItem из директории

2. **GetSavedPredictionsUseCase** - класс, реализующий бизнес-логику получения сохраненных предиктов

3. **Проверка корректности**:
   - Наличие всех необходимых файлов: `united_mask.png` и `class_0.png` до `class_5.png`
   - Одинаковые размеры всех изображений в папке
   - Файлы действительно существуют и доступны для чтения

4. **Загрузка при запуске**:
   - В методе `onCreate()` MainActivity создается UseCase и вызывается `loadSavedPredictions()`
   - Загрузка происходит в фоновом потоке, чтобы не блокировать UI
   - Результаты загружаются в RecyclerView через `historyAdapter.updateItems()`

## Маршрут реализации

1. Создать интерфейс `IPredictionHistoryRepository.kt` в пакете `domain.repository`
2. Создать класс `FilePredictionHistoryRepository.kt` в пакете `data.repository`
3. Создать класс `GetSavedPredictionsUseCase.kt` в пакете `domain.usecases`
4. Изменить MainActivity для использования UseCase при загрузке сохраненных предиктов
5. Протестировать функциональность

## Потерянная информация

Обратите внимание, что точное время выполнения предикта не сохраняется в файловой системе, поэтому в `executionTime` будет установлено значение 0 для загруженных из файловой системы элементов. Если требуется сохранять и отображать точное время выполнения, потребуется изменить механизм сохранения для хранения этой информации в каком-либо формате (например, в JSON-файле).

## Блокировка поворота экрана

Для всех активностей приложения рекомендуется заблокировать возможность поворота экрана. Это можно сделать следующим образом:

1. В файле `AndroidManifest.xml` для каждой активности добавить атрибут `android:screenOrientation="portrait"`:

```xml
<activity
    android:name=".MainActivity"
    android:screenOrientation="portrait"
    android:exported="true">
    ...
</activity>

<activity
    android:name=".features.detail.DetailActivity"
    android:screenOrientation="portrait"
    android:exported="false">
    ...
</activity>

<activity
    android:name=".features.fullscreen.FullscreenActivity"
    android:screenOrientation="portrait"
    android:exported="false">
    ...
</activity>
```

2. Альтернативно, можно установить ориентацию по умолчанию для всего приложения, добавив в тег `<application>`:

```xml
<application
    android:screenOrientation="portrait"
    ... >
    ...
</application>
```

Блокировка ориентации экрана поможет избежать проблем с сохранением состояния активностей и обеспечит лучший пользовательский опыт для приложения, ориентированного на конкретный тип задач.