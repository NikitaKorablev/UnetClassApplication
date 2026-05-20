package ru.unet_app.unetclass.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.unet_app.datastore.data.PredictionHistoryItem
import ru.unet_app.unetclass.R

class HistoryAdapter(
    private val items: MutableList<PredictionHistoryItem> = mutableListOf(),
    private val onItemClick: (PredictionHistoryItem) -> Unit
): RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    fun addItem(item: PredictionHistoryItem) {
        items.add(0, item) // Добавляем в начало списка (новые сверху)
        notifyItemInserted(0) // Уведомляем адаптер о вставке нового элемента
    }

    fun updateItems(newItems: List<PredictionHistoryItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    // Получить копию списка элементов
    fun getItems(): List<PredictionHistoryItem> {
        return items.toList() // Создаем копию списка, чтобы избежать изменений извне
    }

    // Метод для обновления выбранной позиции
    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position

        // Обновляем предыдущую и новую позиции
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition)
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition)
        }
    }

    // Реализация стандартных методов RecyclerView.Adapter
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position == selectedPosition)
    }

    override fun getItemCount(): Int = items.size

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestamp_text)
        private val timeTextView: TextView = itemView.findViewById(R.id.time_text)
        private val dimensionsTextView: TextView = itemView.findViewById(R.id.dimensions_text)

        fun bind(item: PredictionHistoryItem, isSelected: Boolean) {
            timestampTextView.text = item.timestamp
            timeTextView.text = "${item.executionTime} ms"
            dimensionsTextView.text = "${item.imageWidth}x${item.imageHeight}"

            // Устанавливаем фон в зависимости от состояня выделения
            if (isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
            }

            itemView.setOnClickListener {
                // Обновляем выбранную позицию
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    setSelectedPosition(currentPosition)
                    onItemClick(item)
                }
            }
        }
    }
}
