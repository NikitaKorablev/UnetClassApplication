package com.app.datastore.domain.repository

import android.graphics.Bitmap
import com.app.model.InferenceMetadata
import java.io.File

interface ImageRepository {
    fun saveImage(bitmap: Bitmap, directory: File, fileName: String): Boolean
    fun createResultsDirectory(): File
    fun saveMetadata(metadata: InferenceMetadata, directory: File): Boolean
    fun loadMetadata(directory: File): InferenceMetadata?
}