package com.braille.braisee.ui.learn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.braille.braisee.data.learn.Module
import com.braille.braisee.databinding.FragmentLearnBinding
import com.braille.braisee.ui.adapter.ModuleAdapter

class LearnFragment : Fragment() {

    private lateinit var moduleList: ArrayList<Module>
    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using View Binding
        _binding = FragmentLearnBinding.inflate(inflater, container, false)

        // Initialize RecyclerView
        with(binding.rvLearn) {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        // Populate module list and set adapter
        moduleList = ArrayList()
        populateModulesFromResources()
        binding.rvLearn.adapter = ModuleAdapter(moduleList)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to avoid memory leaks
    }

    private fun populateModulesFromResources() {
        if (moduleList.isNotEmpty()) return // Avoid repopulating data

        val titles = resources.getStringArray(com.braille.braisee.R.array.list_module)
        val descriptions = resources.getStringArray(com.braille.braisee.R.array.list_deskripsi)
        val ytLinks = resources.getStringArray(com.braille.braisee.R.array.list_ytlink)

        for (i in titles.indices) {
            moduleList.add(Module(titles[i], descriptions[i], ytLinks[i]))
        }
    }
}
