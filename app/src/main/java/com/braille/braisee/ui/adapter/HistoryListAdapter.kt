package com.braille.braisee.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.braille.braisee.R
import com.braille.braisee.data.AnalyzeHistory
import com.braille.braisee.databinding.ListHistoryBinding
import com.bumptech.glide.Glide

class HistoryListAdapter(private var onBookmarkClick: (AnalyzeHistory) -> Unit) :
    RecyclerView.Adapter<HistoryListAdapter.ListViewHolder>() {

    private val history = mutableListOf<AnalyzeHistory>()

    inner class ListViewHolder(private val binding: ListHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(history: AnalyzeHistory) {
            binding.tvDescription.text = history.result
            Glide.with(itemView.context)
                .load(history.imageUri)
                .into(binding.ivImageLogo)
            binding.ivBookmark.setImageResource(
                if (history.favorite) R.drawable.baseline_favorite_24
                else R.drawable.heart
            )

            binding.ivBookmark.setOnClickListener {
                onBookmarkClick(history)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ListHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(history[position])
    }

    override fun getItemCount(): Int = history.size

    fun setData(newData: List<AnalyzeHistory>) {
        history.clear()
        history.addAll(newData)
        notifyDataSetChanged()
    }


}