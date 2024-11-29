package com.braille.braisee.ui.learn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.braille.braisee.R
import com.braille.braisee.data.learn.Module
import com.braille.braisee.ui.adapter.ModuleAdapter

class LearnFragment : Fragment() {

    private lateinit var moduleList: ArrayList<Module>
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_learn, container, false)

        recyclerView = view.findViewById(R.id.rv_learn)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        moduleList = ArrayList()
        populateModulesFromResources()
        recyclerView.adapter = ModuleAdapter(moduleList)

        return view
    }

    private fun populateModulesFromResources() {
        if (moduleList.isNotEmpty()) return  // Hindari pengisian ulang data

        val titles = resources.getStringArray(R.array.list_module)
        val descriptions = resources.getStringArray(R.array.list_deskripsi)
        val ytLinks = resources.getStringArray(R.array.list_ytlink)

        for (i in titles.indices) {
            moduleList.add(Module(titles[i], descriptions[i], ytLinks[i]))
        }
    }
}
