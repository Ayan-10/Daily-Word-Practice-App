package com.example.dailywordpractice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dailywordpractice.Data.WordItem
import com.example.dailywordpractice.WordItemListAdapter.WordViewHolder

public class WordItemListAdapter(private val listener: WordItemAdapter) : ListAdapter<WordItem, WordViewHolder>(WORDS_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        return WordViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, listener)
    }

    class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val wordView: TextView = itemView.findViewById(R.id.txt_word)
        private val definationView: TextView = itemView.findViewById(R.id.txt_definition)
        private  val deleteView: ImageView = itemView.findViewById(R.id.delete)

        fun bind(wordItem: WordItem, listener: WordItemAdapter) {
            wordView.text = wordItem.word
            definationView.text = wordItem.definition
            deleteView.setOnClickListener {
                listener.onDeleteClicked(wordItem)
            }
        }

        companion object {
            fun create(parent: ViewGroup): WordViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return WordViewHolder(view)
            }
        }
    }

    companion object {
        private val WORDS_COMPARATOR = object : DiffUtil.ItemCallback<WordItem>() {
            override fun areItemsTheSame(oldItem: WordItem, newItem: WordItem): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: WordItem, newItem: WordItem): Boolean {
                return oldItem.word == newItem.word
            }
        }
    }
}

interface WordItemAdapter {
    fun onDeleteClicked(wordItem: WordItem)
}
