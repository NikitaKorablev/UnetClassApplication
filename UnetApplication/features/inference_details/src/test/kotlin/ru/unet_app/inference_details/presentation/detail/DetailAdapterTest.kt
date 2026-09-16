package ru.unet_app.inference_details.presentation.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.unet_app.inference_details.R
import java.util.ArrayList

@RunWith(RobolectricTestRunner::class)
class DetailAdapterTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val items = ArrayList<DetailImageItem>().apply {
        add(DetailImageItem("/path1.png", "Image 1", "mitochondria"))
        add(DetailImageItem("/path2.png", "Image 2", "PSD"))
        add(DetailImageItem("/path3.png", "Image 3", "vesicles"))
    }
    private var clickItem: DetailImageItem? = null
    private val adapter = DetailAdapter(items) { item ->
        clickItem = item
    }

    @Test
    fun testGetItemCount() {
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun testOnCreateViewHolder() {
        val parent = ViewGroup(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(viewHolder)
        assertTrue(viewHolder.itemView is View)
        assertTrue(viewHolder.itemView.findViewById<ImageView>(R.id.imageView) != null)
        assertTrue(viewHolder.itemView.findViewById<TextView>(R.id.nameTextView) != null)
    }

    @Test
    fun testOnBindViewHolder() {
        val parent = ViewGroup(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        val nameTextView = viewHolder.itemView.findViewById<TextView>(R.id.nameTextView)
        assertEquals("Image 1", nameTextView.text.toString())
    }

    @Test
    fun testOnBindViewHolderMultipleItems() {
        val parent = ViewGroup(context)

        for (i in 0 until items.size) {
            val viewHolder = adapter.onCreateViewHolder(parent, 0)
            adapter.onBindViewHolder(viewHolder, i)

            val nameTextView = viewHolder.itemView.findViewById<TextView>(R.id.nameTextView)
            assertEquals(items[i].imageName, nameTextView.text.toString())
        }
    }

    @Test
    fun testClickListener() {
        val parent = ViewGroup(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(viewHolder, 1)

        viewHolder.itemView.performClick()

        assertNotNull(clickItem)
        assertEquals(items[1], clickItem)
    }

    @Test
    fun testEmptyList() {
        val emptyItems = ArrayList<DetailImageItem>()
        val emptyAdapter = DetailAdapter(emptyItems) { }
        
        assertEquals(0, emptyAdapter.itemCount)
    }
}