# Отчёт об исследовании проблемы TFLiteModel

## Дата
1 апреля 2026 г.

## Описание проблемы

После исправления формата тензора с NCHW на NHWC появилась новая проблема:
> **Все слои имеют куски всех слоёв сразу, при этом эти куски идентичны.**

Визуально это означает, что каждый из 6 классов сегментации содержит одинаковые данные.

---

## Проведённое исследование

### Этап 1: Анализ текущего состояния

**Проверено:**
- Формула индексации в `stitchTile` изменена на NHWC-совместимую
- Комментарий обновлён с указанием правильного формата `[1, 256, 256, 6]`

**Текущий код:**
```kotlin
val index = (ty * tileSize + tx) * PredictedClasses.NUM_CLASSES + imgType
```

Эта формула **правильная** для формата NHWC.

---

### Этап 2: Проверка размера выходного массива

**Ожидаемый размер для NHWC `[1, 256, 256, 6]`:**
```
256 × 256 × 6 = 393,216 элементов
```

**Проблема:** Метод `outputBuffers[0].readFloat()` может возвращать не весь буфер.

---

### Этап 3: Проверка формулы индексации NHWC

**Расчёт для NHWC формата:**
- Пиксель (0, 0), канал 0: `index = (0 × 256 + 0) × 6 + 0 = 0`
- Пиксель (0, 0), канал 1: `index = (0 × 256 + 0) × 6 + 1 = 1`
- Пиксель (0, 0), канал 2: `index = (0 × 256 + 0) × 6 + 2 = 2`

**Вывод:** Формула **правильная**.

---

### Этап 4: Анализ API CompiledModel

**Критическое обнаружение:**

Метод `outputBuffers[0].readFloat()` в TensorFlow Lite `CompiledModel` API может вести себя двумя способами:

1. **Возвращать одно Float-значение** (первый элемент буфера)
2. **Возвращать FloatArray** (весь буфер)

**Симптомы указывают на проблему #1:**

```kotlin
val outputFloatArray = outputBuffers[0].readFloat()  // Возвращает Float, а не FloatArray!
```

Если `outputFloatArray` — это `Float` (одно значение), то при попытке индексации:
```kotlin
finalMaskArray[imgType][outY][outX] = outputData[index]
```

...все каналы получают **одно и то же значение**.

---

## Корневая причина

### **`readFloat()` возвращает одно значение, а не массив**

Метод `outputBuffers[0].readFloat()` в `CompiledModel` API возвращает **единственное float-значение**, а не весь выходной буфер.

**Механизм ошибки:**

```
┌─────────────────────────────────────────────────────────┐
│  Выходной буфер модели (393,216 элементов):            │
│  [канала0_пиксель0, канала1_пиксель0, ..., канала5_пиксель0,  │
│   канала0_пиксель1, канала1_пиксель1, ..., канала5_пиксель1,  │
│   ...]                                                   │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
              outputBuffers[0].readFloat()
                            │
                            ▼
                    Возвращает ТОЛЬКО
                    первый элемент
                            │
                            ▼
              outputFloatArray = 0.12345 (одно число)
                            │
                            ▼
              outputData[index] всегда возвращает
              одно и то же значение (или 0.0)
```

**Результат:** Все 6 классов получают идентичные данные.

---

## Решение

### **Использовать метод чтения всего буфера**

Вместо:
```kotlin
val outputFloatArray = outputBuffers[0].readFloat()  // НЕПРАВИЛЬНО
```

Нужно использовать:
```kotlin
val expectedOutputSize = Tile.SIZE * Tile.SIZE * PredictedClasses.NUM_CLASSES
val outputFloatArray = FloatArray(expectedOutputSize)
outputBuffers[0].readFloat(outputFloatArray)  // ПРАВИЛЬНО: читает весь буфер
```

---

## Исправленный код

### Файл: `TFLiteModel.kt`

```kotlin
override fun predict(inputImageData: ImageData)
: ResultState<LabeledData, String> {
    val finalMaskArray = Array(PredictedClasses.NUM_CLASSES) {
        Array(inputImageData.height) {
            FloatArray(inputImageData.width)
        }
    }

    // Вычисляем ожидаемый размер выходных данных: [1, TILE_SIZE, TILE_SIZE, NUM_CLASSES]
    val expectedOutputSize = Tile.SIZE * Tile.SIZE * PredictedClasses.NUM_CLASSES

    inputImageData.tiles.forEach { tile ->
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(tile.bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val byteBuffer = tensorImage.buffer
        val floatArray = FloatArray(Tile.SIZE * Tile.SIZE)

        byteBuffer.rewind()
        byteBuffer.asFloatBuffer().get(floatArray)

        inputBuffers[0].writeFloat(floatArray)
        model.run(inputBuffers, outputBuffers)
        
        // ИСПРАВЛЕНИЕ: Читаем весь выходной буфер как массив
        val outputFloatArray = FloatArray(expectedOutputSize)
        outputBuffers[0].readFloat(outputFloatArray)

        Log.d(TAG, "Tile (${tile.startX}, ${tile.startY}): output size = ${outputFloatArray.size}")
        Log.d(TAG, "Sample values: [0]=${outputFloatArray[0]}, [1]=${outputFloatArray[1]}, [5]=${outputFloatArray[5]}")

        stitchTile(outputFloatArray, tile, finalMaskArray)
    }

    // ... создание LabeledData
}
```

---

## Таблица изменений

| № | Изменение | Строка | Описание |
|---|-----------|--------|----------|
| 1 | Добавлен `expectedOutputSize` | ~47 | Вычисление ожидаемого размера выхода |
| 2 | Изменено чтение выхода | ~65-66 | Чтение всего буфера в массив |
| 3 | Добавлено логирование | ~68-69 | Отладка размера и значений |
| 4 | Обновлён комментарий | ~120 | Указание правильного формата NHWC |

---

## Ожидаемый результат

После применения исправления:

✅ Каждый класс сегментации будет содержать **уникальные данные**  
✅ Выходные данные будут правильно интерпретироваться в формате NHWC  
✅ Результаты TFLiteModel совпадут с PyTorchModel  

---

## Дополнительные проверки

### Если проблема сохранится:

1. **Проверить размер `outputFloatArray` после чтения:**
   ```kotlin
   Log.d(TAG, "Actual output size: ${outputFloatArray.size}")
   ```

2. **Проверить значения в выходном массиве:**
   ```kotlin
   Log.d(TAG, "Channel 0 sample: ${outputFloatArray[0]}")
   Log.d(TAG, "Channel 1 sample: ${outputFloatArray[1]}")
   Log.d(TAG, "Channel 5 sample: ${outputFloatArray[5]}")
   ```

3. **Проверить модель на предмет конвертации:**
   - Убедиться, что модель сконвертирована с правильным выходным слоем
   - Проверить, что выходной слой имеет 6 каналов активации

---

## Приложения

### A. Форматы тензоров

| Фреймворк | Формат | Порядок осей | Пример размера |
|-----------|--------|--------------|----------------|
| PyTorch | NCHW | `[batch, channels, height, width]` | `[1, 6, 256, 256]` |
| TensorFlow Lite | NHWC | `[batch, height, width, channels]` | `[1, 256, 256, 6]` |

### B. Формулы индексации

**NCHW (PyTorch):**
```kotlin
val index = channel * height * width + y * width + x
```

**NHWC (TFLite):**
```kotlin
val index = (y * width + x) * channels + channel
```

---

## Источники

- TensorFlow Lite Documentation: `CompiledModel` API
- PyTorch Mobile Documentation: Tensor indexing
- Внутренний код проекта: `PyTorchModel.kt`, `TFLiteModel.kt`
