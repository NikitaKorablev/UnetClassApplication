package com.app.unet.data.unetmodels

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import com.app.model.ResultState
import com.app.unet.domain.UnetModel
import com.app.unet.domain.models.SegmentationResult
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject

class TFLiteModel @Inject constructor(
    val context: Context
): UnetModel {
//    private val interpreter: Interpreter by lazy {
//        val assetManager = context.assets
//        val model = loadModelFile(assetManager, MODEL_PATH)
//        Interpreter(
//            model,
//            Interpreter.Options()
//        )
//    }

    private val model = CompiledModel.create(
        context.assets,
        MODEL_PATH,
        CompiledModel.Options(Accelerator.GPU),
    )
    private val inputBuffers = model.createInputBuffers()
    private val outputBuffers = model.createOutputBuffers()

    override fun startSegmentation(bitmap: Bitmap): ResultState<SegmentationResult, String> {
        inputBuffers.

        TODO("Not yet implemented")
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
    }

    companion object {
        private const val MODEL_PATH = "tiny_unet_v3.tflite"
    }
}