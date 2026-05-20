package ru.unet_app.inference_details.presentation.detail

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.unet_app.model.ClassNames
import org.json.JSONObject
import ru.unet_app.inference_details.R
import ru.unet_app.inference_details.presentation.fullscreen.FullscreenActivity
import java.io.File

class DetailActivity : AppCompatActivity() {

    private lateinit var imagesRecyclerView: RecyclerView
    private lateinit var backButton: ImageButton
    private lateinit var titleTextView: androidx.appcompat.widget.AppCompatTextView
    private lateinit var dimensionsTextView: TextView
    private lateinit var executionTimeTextView: TextView
    private lateinit var memoryUsageTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        initViews()
        setupRecyclerView()
        setupBackButton()

        val predictionPath = intent.getStringExtra("prediction_path")
        if (predictionPath != null) {
            loadAndDisplayMetadata(predictionPath)
        }
    }

    private fun initViews() {
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView)
        backButton = findViewById(R.id.backButton)
        titleTextView = findViewById(R.id.titleTextView)
        dimensionsTextView = findViewById(R.id.dimensionsTextView)
        executionTimeTextView = findViewById(R.id.executionTimeTextView)
        memoryUsageTextView = findViewById(R.id.memoryUsageTextView)
    }

    private fun loadAndDisplayMetadata(predictionPath: String) {
        val metadataFile = File(predictionPath, "metadata.json")
        android.util.Log.d("DetailActivity", "Loading metadata from: ${metadataFile.absolutePath}")
        
        if (metadataFile.exists()) {
            try {
                val jsonText = metadataFile.readText()
                android.util.Log.d("DetailActivity", "Metadata content: $jsonText")
                
                val json = JSONObject(jsonText)
                val width = json.getInt("width")
                val height = json.getInt("height")
                val time = json.getLong("executionTimeMs")
                val memory = json.getLong("memoryUsageBytes") / (1024 * 1024)

                dimensionsTextView.text = "Dimensions: ${width} x ${height}"
                executionTimeTextView.text = "Execution Time: ${time} ms"
                memoryUsageTextView.text = "Memory Usage: ${memory} MB"
            } catch (e: Exception) {
                android.util.Log.e("DetailActivity", "Error parsing metadata", e)
            }
        } else {
            android.util.Log.w("DetailActivity", "Metadata file NOT FOUND")
        }
    }

    private fun setupRecyclerView() {
        val predictionPath = intent.getStringExtra("prediction_path")
        if (predictionPath != null) {
            val imageFiles = getImageFilesFromDirectory(predictionPath)
            val detailItems = imageFiles.map { file ->
                val className = extractClassNameFromFile(file.name)
                DetailImageItem(
                    imagePath = file.absolutePath,
                    imageName = file.name,
                    className = className
                )
            }

            val adapter = DetailAdapter(detailItems) { detailImageItem ->
                // Открытие полноэкранного просмотра при клике на изображение
                val intent = Intent(this, FullscreenActivity::class.java).apply {
                    putExtra("image_path", detailImageItem.imagePath)
                    putExtra("image_name", detailImageItem.imageName)
                }
                startActivity(intent)
            }

            imagesRecyclerView.apply {
                this.adapter = adapter
                layoutManager = GridLayoutManager(this@DetailActivity, 2) // 2 колонки
            }
        }
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun getImageFilesFromDirectory(directoryPath: String): List<File> {
        val directory = File(directoryPath)
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }

        return directory.listFiles { file ->
            file.isFile && isImageFile(file)
        }?.sortedBy { it.name } ?: emptyList()
    }

    private fun extractClassNameFromFile(fileName: String): String? {
        // Ищем, соответствует ли имя файла шаблону className_prediction.png
        for (className in ClassNames.NAMES) {
            if (fileName.startsWith(className + "_prediction.")) {
                return className
            }
        }
        return null  // для других файлов, таких как united_mask.png
    }

    private fun isImageFile(file: File): Boolean {
        val name = file.name.lowercase()
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
    }
}
