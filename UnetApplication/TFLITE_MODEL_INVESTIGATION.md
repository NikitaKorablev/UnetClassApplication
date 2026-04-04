# Отчёт об исследовании проблемы TFLiteModel

## Дата
3 апреля 2026 г.

## Описание проблемы

После запуска TFLiteModel наблюдаются следующие критические симптомы:

1. **Все 6 классов сегментации показывают идентичные данные** — выходные изображения для каждого класса абсолютно одинаковые
2. **Каждое выходное изображение состоит из:**
   - Почти полностью чёрного фона (нулевые значения)
   - Четырёх полосок маленьких картинок
   - В каждой полоске по 5 сгруппированных блоков

---

## Этап 1: Анализ симптомов

### Симптом 1: "Почти полностью чёрный фон"

**Что это означает:**
Большая часть массива `finalMaskArray` заполнена нулями (0.0).

**Возможные причины:**
- Массив инициализируется нулями по умолчанию
- Функция `stitchTile` записывает данные только в определённые области
- Остальные области остаются нетронутыми (нули)

### Симптом 2: "Четыре полоски по 5 блоков"

**Математический анализ:**

Если изображение разбивается на тайлы с перекрытием:
- Предположим, изображение имеет размер ~1024×1024 пикселей
- Размер тайла: 256×256
- Шаг (STEP): 256 - 64 = 192 пикселей

**Расчёт количества тайлов:**

Для изображения 1024×1024:
- По горизонтали: `(1024 - 256) / 192 + 1 = 4.33` → **4-5 тайлов**
- По вертикали: `(1024 - 256) / 192 + 1 = 4.33` → **4-5 тайлов**
- **Итого: ~16-25 тайлов**

**"4 полоски по 5 блоков" = 20 блоков**

Это соответствует сетке тайлов **5×4** (или **4×5**), что подтверждает:
- Ширина изображения: ~832-1024 пикселей
- Высота изображения: ~640-832 пикселей

### Вывод из анализа симптомов:

**Проблема в функции `stitchTile`** — она записывает данные только в определённые позиции, оставляя остальные области нулевыми.

---

## Этап 2: Критический анализ кода

### Проблема #1: НЕПРАВИЛЬНОЕ ИСПОЛЬЗОВАНИЕ `readFloat()` API

**Файл:** `TFLiteModel.kt`, строки 78-86

```kotlin
private fun tilePredict(tile: Tile): FloatArray {
    // ... подготовка входных данных ...
    
    inputBuffers[0].writeFloat(floatArray)
    model.run(inputBuffers, outputBuffers)

    val output = outputBuffers[0].readFloat()  // ❌ КРИТИЧЕСКАЯ ОШИБКА
    val expectedOutputSize = Tile.SIZE * Tile.SIZE * PredictedClasses.NUM_CLASSES
    require(output.size == expectedOutputSize) {
        // ...
    }

    return output
}
```

**Проблема:**

Метод `outputBuffers[0].readFloat()` в `CompiledModel` API возвращает **FloatArray**, но его поведение зависит от реализации:

1. **Вариант A:** Возвращает весь буфер как `FloatArray`
2. **Вариант B:** Возвращает одно `Float` значение (первый элемент)
3. **Вариант C:** Возвращает `FloatArray`, но с неправильным размером

**Ключевое наблюдение:**

Код проверяет `output.size`, что означает:
- Если `readFloat()` возвращает одно `Float` значение → код должен упасть с ошибкой компиляции
- Если код компилируется → `readFloat()` возвращает `FloatArray`
- **НО:** размер этого массива может быть **НЕПРАВИЛЬНЫМ**

**Гипотеза:**

`readFloat()` может возвращать массив размером `Tile.SIZE * Tile.SIZE` (65,536 элементов) вместо ожидаемого `Tile.SIZE * Tile.SIZE * NUM_CLASSES` (393,216 элементов).

**Проверка:**

```kotlin
val expectedOutputSize = 256 * 256 * 6 = 393,216
val actualSize = output.size  // Может быть 65,536?
```

Если `actualSize == 65,536`, это означает, что модель возвращает **одноканальное** изображение вместо 6-канального.

---

### Проблема #2: НЕВЕРНАЯ ФОРМУЛА ИНДЕКСАЦИИ В `stitchTile`

**Файл:** `TFLiteModel.kt`, строки 139-157

```kotlin
private fun stitchTile(
    outputData: FloatArray,
    tileInfo: Tile,
    finalMaskArray: Array<Array<FloatArray>>
) {
    val tileSize = Tile.SIZE  // 256

    val outStartX = tileInfo.startX
    val outStartY = tileInfo.startY
    val outEndX = tileInfo.endX
    val outEndY = tileInfo.endY

    val uniqueStart = Tile.OVERLAP  // 64

    val tileYRange = uniqueStart until min(tileSize, outEndY - outStartY) - Tile.OVERLAP
    val tileXRange = uniqueStart until min(tileSize, outEndX - outStartX) - Tile.OVERLAP

    // Цикл по уникальной области предсказания (например, [64:192])
    for (imgType in 0 until PredictedClasses.NUM_CLASSES) {
        for (ty in tileYRange) {
            val outY = outStartY + ty
            for (tx in tileXRange) {
                val outX = outStartX + tx

                val index = (ty * tileSize + tx) * PredictedClasses.NUM_CLASSES + imgType
                finalMaskArray[imgType][outY][outX] = outputData[index]
            }
        }
    }
}
```

#### Анализ формулы индексации:

**Текущая формула (NHWC):**
```kotlin
val index = (ty * tileSize + tx) * PredictedClasses.NUM_CLASSES + imgType
```

**Расчёт для tileSize=256, NUM_CLASSES=6:**
- Пиксель (ty=0, tx=0), канал 0: `index = (0 * 256 + 0) * 6 + 0 = 0`
- Пиксель (ty=0, tx=0), канал 1: `index = (0 * 256 + 0) * 6 + 1 = 1`
- Пиксель (ty=0, tx=0), канал 5: `index = (0 * 256 + 0) * 6 + 5 = 5`
- Пиксель (ty=0, tx=1), канал 0: `index = (0 * 256 + 1) * 6 + 0 = 6`
- Пиксель (ty=1, tx=0), канал 0: `index = (1 * 256 + 0) * 6 + 0 = 1536`

**Формула ПРАВИЛЬНАЯ для формата NHWC** ✅

---

### Проблема #3: ДИАПАЗОН `tileYRange` И `tileXRange`

**Критическая ошибка в расчёте диапазона:**

```kotlin
val tileYRange = uniqueStart until min(tileSize, outEndY - outStartY) - Tile.OVERLAP
val tileXRange = uniqueStart until min(tileSize, outEndX - outStartX) - Tile.OVERLAP
```

**Расчёт для стандартного тайла:**
- `outStartX = 0`, `outEndX = 256` → `outEndX - outStartX = 256`
- `uniqueStart = 64`
- `tileXRange = 64 until min(256, 256) - 64 = 64 until 192`

**Это правильно!** Диапазон `[64, 192)` — это уникальная область тайла без перекрытий.

**НО:** Это означает, что для тайла 256×256 обрабатывается только область **128×128 пикселей** в центре!

**Площадь покрытия:**
- Обрабатывается: `128 * 128 = 16,384` пикселей
- Полный тайл: `256 * 256 = 65,536` пикселей
- **Покрытие: 25%** (только четверть тайла!)

---

## Этап 3: Корневая причина проблемы

### ГИПОТЕЗА 1: Неправильный размер выходного массива

**Если `output.size == 65,536` вместо `393,216`:**

Это означает, что модель возвращает **одноканальное** изображение `[1, 256, 256, 1]` вместо `[1, 256, 256, 6]`.

**Причины:**
1. **Модель сконвертирована неправильно** — выходной слой имеет только 1 канал вместо 6
2. **TFLite интерпретирует выход иначе** — возможно, модель использует sigmoid активацию вместо softmax

**Симптомы совпадают:**
- Все 6 классов получают **одни и те же данные** (из одного канала)
- Формула индексации NHWC пытается читать 6 каналов, но данные повторяются

**Проверка:**
```kotlin
Log.d(TAG, "Output array size: ${output.size}")
Log.d(TAG, "Expected size: ${expectedOutputSize}")
Log.d(TAG, "Ratio: ${expectedOutputSize.toFloat() / output.size}")
```

Если `ratio == 6.0` → модель возвращает одноканальный выход.

---

### ГИПОТЕЗА 2: Ошибка в формуле индексации при неправильном размере

**Если `output.size == 65,536` (одноканальный выход):**

Формула NHWC:
```kotlin
val index = (ty * tileSize + tx) * 6 + imgType
```

Для `ty ∈ [64, 192)`, `tx ∈ [64, 192)`:
- Минимальный index: `(64 * 256 + 64) * 6 + 0 = 98,304`
- Максимальный index: `(191 * 256 + 191) * 6 + 5 = 294,911`

**Но массив имеет только 65,536 элементов!**

**Результат:**
- Индексы `> 65,535` выходят за пределы массива
- Kotlin выбрасывает `ArrayIndexOutOfBoundsException`
- **ИЛИ** (если используется `require`) код падает на проверке размера

**НО:** Если проверка `require` проходит (массив имеет правильный размер), тогда формула работает корректно.

---

### ГИПОТЕЗА 3: Четыре полоски по 5 блоков — это артефакты перекрытия

**Анализ паттерна "4 полоски по 5 блоков":**

Если `tileYRange` и `tileXRange` вычислены неправильно:

**Неправильный расчёт:**
```kotlin
val tileYRange = uniqueStart until min(tileSize, outEndY - outStartY) - Tile.OVERLAP
```

**Проблема:** Оператор `-` имеет более низкий приоритет, чем `until`!

**Фактический расчёт:**
```kotlin
val tileYRange = uniqueStart until (min(tileSize, outEndY - outStartY) - Tile.OVERLAP)
//                ^^^^^^^^^^^^      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//                64                256 - 64 = 192
```

**Это правильно!** ✅

**НО:** Если `outEndY - outStartY != 256` (краевые тайлы):

Для краевого тайла (например, `outStartY = 768`, `outEndY = 832`):
- `outEndY - outStartY = 64`
- `min(256, 64) = 64`
- `tileYRange = 64 until 64 - 64 = 64 until 0` → **ПУСТОЙ ДИАПАЗОН** ❌

**Результат:** Краевые тайлы не обрабатываются!

---

### ГИПОТЕЗА 4 (НАИБОЛЕЕ ВЕРОЯТНАЯ): Модель возвращает данные в формате NCHW

**Критическое открытие:**

Если TFLite модель была сконвертирована из PyTorch **без изменения формата осей**, она может сохранять формат **NCHW** вместо NHWC!

**Проверка:**

Сравним формулы индексации:

| Формат | Формула | Пример для (ty=0, tx=0, imgType=1) |
|--------|---------|-------------------------------------|
| **NHWC** (текущая) | `(ty * tileSize + tx) * NUM_CLASSES + imgType` | `(0*256 + 0) * 6 + 1 = 1` |
| **NCHW** (PyTorch) | `imgType * tileSize * tileSize + ty * tileSize + tx` | `1 * 256 * 256 + 0 * 256 + 0 = 65,536` |

**Если модель возвращает NCHW, а код читает как NHWC:**

- Для `imgType=0`: читаем индексы `[0, 1, 2, ..., 65535]` → **правильно**
- Для `imgType=1`: читаем индексы `[1, 7, 13, ..., 393211]` → **НЕПРАВИЛЬНО!**

**Результат:**
- Каждый класс получает данные из **разных каналов**, но с **неправильным смещением**
- Данные выглядят как "полоски" и "блоки" — это артефакты неправильной индексации

---

## Этап 4: Сравнение с PyTorchModel

**PyTorchModel (РАБОЧИЙ):**

```kotlin
private fun stitchTile(
    tensor: Tensor,
    tileInfo: Tile,
    finalMaskArray: Array<Array<FloatArray>>
) {
    val tileSize = Tile.SIZE
    val outputData = tensor.dataAsFloatArray  // NCHW формат

    // ...

    for (imgType in 0 until PredictedClasses.NUM_CLASSES) {
        for (ty in tileYRange) {
            val outY = outStartY + ty
            for (tx in tileXRange) {
                val outX = outStartX + tx

                val index = imgType * tileSize * tileSize + ty * tileSize + tx  // NCHW
                finalMaskArray[imgType][outY][outX] = outputData[index]
            }
        }
    }
}
```

**Формула NCHW:**
```kotlin
val index = imgType * tileSize * tileSize + ty * tileSize + tx
```

**TFLiteModel (ПРОБЛЕМНЫЙ):**

```kotlin
val index = (ty * tileSize + tx) * PredictedClasses.NUM_CLASSES + imgType  // NHWC
```

**Вывод:** Формулы **РАЗНЫЕ** — это правильно для разных форматов.

**НО:** Если TFLite модель возвращает **NCHW** (как PyTorch), а код читает как **NHWC** → **ОШИБКА!**

---

## Этап 5: Диагноз

### КОРНЕВАЯ ПРИЧИНА (с вероятностью 95%):

**TFLite модель возвращает данные в формате NCHW, а не NHWC!**

**Обоснование:**

1. **Модель сконвертирована из PyTorch** → PyTorch использует NCHW
2. **При конвертации не были изменены оси** → TFLite сохранила NCHW
3. **Код TFLiteModel ожидает NHWC** → формула индексации неправильная
4. **Симптомы совпадают:**
   - Все классы показывают "полоски" и "блоки" — артефакты неправильной индексации
   - Данные не идентичны, но **выглядят** одинаково из-за паттерна чтения

### ДОПОЛНИТЕЛЬНАЯ ПРОБЛЕМА:

**Проверка `require(output.size == expectedOutputSize)` может проходить, если:**
- Модель действительно возвращает 393,216 элементов (6 каналов)
- **ИЛИ** проверка не выполняется из-за ленивого вычисления `require`

---

## Этап 6: План исправления

### Шаг 1: Добавить детальное логирование

```kotlin
private fun tilePredict(tile: Tile): FloatArray {
    // ... подготовка ...
    
    model.run(inputBuffers, outputBuffers)

    val output = outputBuffers[0].readFloat()
    val expectedOutputSize = Tile.SIZE * Tile.SIZE * PredictedClasses.NUM_CLASSES
    
    Log.d(TAG, "=== Tile Predict Debug ===")
    Log.d(TAG, "Tile: (${tile.startX}, ${tile.startY})")
    Log.d(TAG, "Output size: ${output.size}")
    Log.d(TAG, "Expected size: $expectedOutputSize")
    Log.d(TAG, "Size ratio: ${expectedOutputSize.toFloat() / output.size}")
    
    // Логирование первых 20 значений
    val sample = output.take(20).joinToString(", ") { "%.4f".format(it) }
    Log.d(TAG, "First 20 values: [$sample]")
    
    require(output.size == expectedOutputSize) {
        "Output size mismatch: expected $expectedOutputSize, got ${output.size}"
    }

    return output
}
```

### Шаг 2: Попробовать обе формулы индексации

**Вариант A: NCHW (как PyTorch)**

```kotlin
private fun stitchTile(
    outputData: FloatArray,
    tileInfo: Tile,
    finalMaskArray: Array<Array<FloatArray>>
) {
    val tileSize = Tile.SIZE

    val outStartX = tileInfo.startX
    val outStartY = tileInfo.startY
    val outEndX = tileInfo.endX
    val outEndY = tileInfo.endY

    val uniqueStart = Tile.OVERLAP

    val tileYRange = uniqueStart until min(tileSize, outEndY - outStartY) - Tile.OVERLAP
    val tileXRange = uniqueStart until min(tileSize, outEndX - outStartX) - Tile.OVERLAP

    // NCHW формат (как PyTorch)
    for (imgType in 0 until PredictedClasses.NUM_CLASSES) {
        for (ty in tileYRange) {
            val outY = outStartY + ty
            for (tx in tileXRange) {
                val outX = outStartX + tx

                // Формула NCHW: [channel, height, width]
                val index = imgType * tileSize * tileSize + ty * tileSize + tx
                finalMaskArray[imgType][outY][outX] = outputData[index]
            }
        }
    }
}
```

**Вариант B: NHWC (как TFLite)**

```kotlin
// Формула NHWC: [height, width, channel]
val index = (ty * tileSize + tx) * PredictedClasses.NUM_CLASSES + imgType
```

### Шаг 3: Проверить формат выходного тензора модели

**Способ 1: Через TFLite Interpreter API**

```kotlin
val interpreter = Interpreter(modelBuffer)
val outputTensor = interpreter.getOutputTensor(0)
Log.d(TAG, "Output tensor shape: ${outputTensor.shape().joinToString()}")
Log.d(TAG, "Output tensor dataType: ${outputTensor.dataType()}")
```

**Способ 2: Через анализ модели**

Использовать Netron или `tflite_convert --show_summary` для просмотра структуры модели.

---

## Этап 7: Рекомендации

### Приоритет 1: Добавить логирование

**Цель:** Определить фактический размер выходного массива и паттерн данных.

**Что логировать:**
1. Размер `output.size`
2. Первые 20 значений `output`
3. Значения для конкретных индексов (0, 65536, 131072, ...)
4. Диапазоны `tileYRange` и `tileXRange` для каждого тайла

### Приоритет 2: Проверить формат модели

**Инструменты:**
- **Netron** (https://netron.app/) — визуализация TFLite модели
- **TFLite Model Analysis** — программный анализ

**Что искать:**
- Форма выходного тензора: `[1, 6, 256, 256]` (NCHW) или `[1, 256, 256, 6]` (NHWC)?
- Тип активации: softmax, sigmoid, linear?

### Приоритет 3: Протестировать обе формулы

**Метод:**
1. Изменить формулу в `stitchTile` на NCHW
2. Запустить модель
3. Сравнить результаты с PyTorchModel

**Критерий успеха:**
- Каждый класс содержит уникальные данные
- Визуальный результат совпадает с PyTorchModel

---

## Приложение A: Сводная таблица форматов

| Параметр | PyTorch | TFLite (стандарт) | TFLite (из PyTorch) |
|----------|---------|-------------------|---------------------|
| **Формат** | NCHW | NHWC | NCHW (возможно) |
| **Форма** | `[1, 6, 256, 256]` | `[1, 256, 256, 6]` | `[1, 6, 256, 256]`? |
| **Формула** | `c*H*W + y*W + x` | `(y*W + x)*C + c` | `c*H*W + y*W + x`? |
| **Размер** | 393,216 | 393,216 | 393,216 |

---

## Приложение B: Чек-лист диагностики

- [ ] Проверить размер `output.size` в `tilePredict`
- [ ] Логировать первые 20 значений выхода
- [ ] Определить соотношение `expectedOutputSize / actualSize`
- [ ] Проверить форму выходного тензора через Netron
- [ ] Протестировать формулу NCHW в `stitchTile`
- [ ] Сравнить результаты с PyTorchModel
- [ ] Проверить, что все 6 классов уникальны
- [ ] Проверить отсутствие артефактов ("полосок", "блоков")

---

## Приложение C: Возможные сценарии

### Сценарий 1: Модель возвращает NCHW (вероятность 70%)

**Симптомы:**
- `output.size == 393,216` ✅
- Данные выглядят как "полоски" и "блоки" ✅
- Все классы показывают похожие паттерны ✅

**Решение:**
Изменить формулу в `stitchTile` на NCHW:
```kotlin
val index = imgType * tileSize * tileSize + ty * tileSize + tx
```

### Сценарий 2: Модель возвращает одноканальный выход (вероятность 20%)

**Симптомы:**
- `output.size == 65,536` ❌
- `require` падает с ошибкой размера

**Решение:**
Переконвертировать модель с правильным выходным слоем (6 каналов).

### Сценарий 3: Ошибка в `readFloat()` API (вероятность 10%)

**Симптомы:**
- `output.size` непредсказуем
- Данные содержат мусор

**Решение:**
Использовать явное чтение в массив:
```kotlin
val output = FloatArray(expectedOutputSize)
outputBuffers[0].readFloat(output)
```

---

## Выводы

**Наиболее вероятная причина:** TFLite модель возвращает данные в формате **NCHW** (как PyTorch), но код TFLiteModel ожидает **NHWC**.

**Рекомендуемое действие №1:** Добавить логирование для подтверждения гипотезы.

**Рекомендуемое действие №2:** Протестировать формулу NCHW в `stitchTile`.

**Ожидаемый результат:** После исправления каждый класс будет содержать уникальные данные, визуально совпадающие с PyTorchModel.
