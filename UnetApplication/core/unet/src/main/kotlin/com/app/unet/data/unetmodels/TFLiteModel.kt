package com.app.unet.data.unetmodels

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.app.model.ImageData
import com.app.model.PredictedClasses
import com.app.model.ResultState
import com.app.model.Tile
import com.app.unet.data.LabelFactory
import com.app.unet.domain.UnetModel
import com.app.unet.models.LabeledData
import com.app.unet.models.classes.Axon
import com.app.unet.models.classes.Boundaries
import com.app.unet.models.classes.Mitochondria
import com.app.unet.models.classes.MitochondriaBoundaries
import com.app.unet.models.classes.PSD
import com.app.unet.models.classes.Vesicles
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp
import javax.inject.Inject

class TFLiteModel @Inject constructor(
    val context: Context
): UnetModel {
    private val model: CompiledModel
    private val inputBuffers: List<TensorBuffer>
    private val outputBuffers: List<TensorBuffer>

    init {
        val result = try {
            val m = CompiledModel.create(
                context.assets,
                MODEL_PATH,
                CompiledModel.Options(Accelerator.GPU)
            )
            val i = m.createInputBuffers()
            val o = m.createOutputBuffers()
            Log.i(TAG, "LiteRT initialized successfully with GPU acceleration.")
            Triple(m, i, o)
        } catch (e: Exception) {
            Log.w(TAG, "GPU acceleration failed. Falling back to CPU.", e)
            val m = CompiledModel.create(
                context.assets,
                MODEL_PATH,
                CompiledModel.Options(Accelerator.CPU)
            )
            val i = m.createInputBuffers()
            val o = m.createOutputBuffers()
            Log.i(TAG, "LiteRT initialized with CPU.")
            Triple(m, i, o)
        }
        model = result.first
        inputBuffers = result.second
        outputBuffers = result.third
    }

    private val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(0f, 255f))
        .add(TransformToGrayscaleOp())
        .build()

    override fun predict(inputImageData: ImageData)
    : ResultState<LabeledData, String> {
        val finalMaskArray = Array(PredictedClasses.NUM_CLASSES) {
            Array(inputImageData.height) {
                FloatArray(inputImageData.width)
            }
        }

        inputImageData.tiles.forEach { tile ->
            val outputFloatArray = try { tilePredict(tile) }
            catch (err: Exception) {
                err.printStackTrace()
                return ResultState.Error("Ошибка обработки тайла: " +
                        err.message.toString())
            }

            stitchTile(outputFloatArray, tile, finalMaskArray)
        }

        val result = LabeledData(
            mitochondria = LabelFactory.getLabel(PredictedClasses.MITOCHONDRIA.className, finalMaskArray[0]) as Mitochondria,
            PSD = LabelFactory.getLabel(PredictedClasses.PSD.className, finalMaskArray[1]) as PSD,
            vesicles = LabelFactory.getLabel(PredictedClasses.VESICLES.className, finalMaskArray[2]) as Vesicles,
            axon = LabelFactory.getLabel(PredictedClasses.AXON.className, finalMaskArray[3]) as Axon,
            boundaries = LabelFactory.getLabel(PredictedClasses.BOUNDARIES.className, finalMaskArray[4]) as Boundaries,
            mitochondriaBoundaries = LabelFactory.getLabel(PredictedClasses.MITOCHONDRIAL_BOUNDARIES.className, finalMaskArray[5]) as MitochondriaBoundaries,
        )

        return ResultState.Success(result)
    }

    override fun close() {
        model.close()
        Log.d(TAG, "TFLite model closed.")
    }

    private fun tilePredict(tile: Tile): FloatArray {
//        val grayBitmap = tile.bitmap.copy(Bitmap.Config.ARGB_8888, true).apply {
//            val canvas = android.graphics.Canvas(this)
//            val paint = android.graphics.Paint()
//            val colorMatrix = android.graphics.ColorMatrix().apply {
//                setSaturation(0f)
//            }
//            paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
//            canvas.drawBitmap(this, 0f, 0f, paint)
//        }
//
//        var tensorImage = TensorImage(DataType.FLOAT32)
//        tensorImage.load(grayBitmap)
//        tensorImage = imageProcessor.process(tensorImage)
//
//        val inputTensorBuffer = TensorBuffer.createFixedSize(
//            intArrayOf(1, 1, Tile.SIZE, Tile.SIZE),
//            DataType.FLOAT32
//        )
//        inputTensorBuffer.loadBuffer(tensorImage.buffer)
//        inputBuffers[0].writeFloat(inputTensorBuffer.floatArray)

// -----------------------------------------------------------------
//
//        val byteBuffer = tensorImage.buffer
//        val floatArray = FloatArray(Tile.SIZE * Tile.SIZE)
//
//        byteBuffer.rewind()
//        byteBuffer.asFloatBuffer().get(floatArray)


//        val floatArray = FloatArray(1 * 1 * Tile.SIZE * Tile.SIZE)
//        val pixels = IntArray(Tile.SIZE * Tile.SIZE)
//        tile.bitmap.getPixels(pixels, 0, Tile.SIZE, 0, 0, Tile.SIZE, Tile.SIZE)

//        inputTensorBuffer.loadBuffer(ByteBuffer.allocateDirect(floatArray.size * 4).apply {
//            asFloatBuffer().put(floatArray)
//            rewind()
//        })

// -----------------------------------------------------------------

        inputBuffers[0].writeFloat(toGrayFloat(tile.bitmap))
        model.run(inputBuffers, outputBuffers)

        val output = outputBuffers[0].readFloat()
        val expectedOutputSize = Tile.SIZE * Tile.SIZE * PredictedClasses.NUM_CLASSES

//        Log.d(TAG, "=== Tile Predict Debug ===")
//        Log.d(TAG, "Tile position: (${tile.startX}, ${tile.startY})")
//        Log.d(TAG, "Expected output size: $expectedOutputSize")
//        Log.d(TAG, "Actual output size: ${output.size}")

        require(output.size == expectedOutputSize) {
            "Output size mismatch: expected $expectedOutputSize, got ${output.size}"
        }

        return output
    }

    private fun toGrayFloat(bitmap: Bitmap): FloatArray {
        val width = bitmap.width
        val height = bitmap.height

        val floatArray = FloatArray(1 * 1 * height * width)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for ((i, pixel) in pixels.withIndex()) {
            // Берем значение одного из каналов (так как в градациях серого R=G=B)
            val grayValue =
                (pixel shr 16) and 0xFF // Берем R канал, но в градации серого все каналы одинаковы
            floatArray[i] = grayValue / 255.0f // Нормализация к [0, 1]
        }

        return floatArray
    }

    companion object {
        const val TAG = "TFLiteModel"
        private const val MODEL_PATH = "tiny_unet_v3.tflite"
    }
}