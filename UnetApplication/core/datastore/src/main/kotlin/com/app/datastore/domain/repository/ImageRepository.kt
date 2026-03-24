package com.app.datastore.domain.repository

import android.graphics.Bitmap
import java.io.File

interface ImageRepository {
    fun saveImage(bitmap: Bitmap, directory: File, fileName: String): Boolean
    fun createResultsDirectory(): File
}