package com.braille.braisee.ui.favorite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.braille.braisee.R
import com.braille.braisee.databinding.FragmentFavoriteBinding
import com.braille.braisee.factory.ViewModelFactory
import com.braille.braisee.ui.adapter.HistoryListAdapter

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FavoriteViewModel
    private lateinit var adapter: HistoryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi ViewModel
        val factory = ViewModelFactory.getInstance(requireContext())
        viewModel = ViewModelProvider(this, factory)[FavoriteViewModel::class.java]

        setupRecyclerView()

        // Observasi LiveData untuk daftar favorite
        viewModel.getFavoriteHistory().observe(viewLifecycleOwner) { favoriteList ->
            adapter.setData(favoriteList)
        }
    }

    private fun setupRecyclerView() {
        adapter = HistoryListAdapter(
            onBookmarkClick = { historyItem ->
                viewModel.toggleBookmark(historyItem)  // Menghapus dari favorite jika di-tap
            }, onItemClick = { historyItem ->
                val action = FavoriteFragmentDirections.actionFavoriteToAnalyze(
                    historyId = historyItem.id,
                    result = historyItem.result,
                    imageUri = historyItem.imageUri
                )
                findNavController().navigate(action)

            })

        binding.rvFavorite.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavoriteFragment.adapter
        }
    }


}