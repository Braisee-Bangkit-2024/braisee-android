package com.braille.braisee.ui.adapter

import android.os.Bundle
import androidx.navigation.findNavController
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.braille.braisee.R
import com.braille.braisee.data.learn.Module

class ModuleAdapter(private val listModule: ArrayList<Module>) :
    RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_module, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = listModule[position]
        holder.title.text = module.title
        holder.description.text = module.description

        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("module", module)
            }
            it.findNavController().navigate(R.id.action_navigation_learn_to_detailFragment, bundle)
        }
    }

    override fun getItemCount(): Int = listModule.size

    class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_judul_learn)
        val description: TextView = itemView.findViewById(R.id.tv_deskripsi_learn)
    }
}
