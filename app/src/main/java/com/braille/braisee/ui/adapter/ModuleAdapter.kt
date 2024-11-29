package com.braille.braisee.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.braille.braisee.R
import com.braille.braisee.data.learn.Module
import com.braille.braisee.databinding.ListModuleBinding

class ModuleAdapter(private val listModule: ArrayList<Module>) :
    RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        // Inflate the layout using View Binding
        val binding = ListModuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ModuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = listModule[position]
        with(holder.binding) {
            // Set title and description
            tvJudulLearn.text = module.title
            tvDeskripsiLearn.text = module.description
        }

        // Handle item click to navigate with arguments
        holder.binding.root.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("module", module)
            }
            it.findNavController().navigate(R.id.action_navigation_learn_to_detailFragment, bundle)
        }
    }

    override fun getItemCount(): Int = listModule.size

    class ModuleViewHolder(val binding: ListModuleBinding) : RecyclerView.ViewHolder(binding.root)
}
