package com.example.summitdiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.summitdiary.HikeAdapter
import com.example.summitdiary.R
import androidx.fragment.app.viewModels
import com.example.summitdiary.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HikeAdapter
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        adapter = HikeAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.hikes.observe(viewLifecycleOwner) { hikeList ->
            adapter.updateData(hikeList)
        }

        viewModel.loadHikes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun toString(): String {
        return "Activities"
    }
}


//package com.example.summitdiary.fragments
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.summitdiary.Hike
//import com.example.summitdiary.HikeAdapter
//import com.example.summitdiary.R
//import com.example.summitdiary.databinding.FragmentHomeBinding
//
//class HomeFragment : Fragment(R.layout.fragment_home) {
//
//    private var _binding: FragmentHomeBinding? = null
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentHomeBinding.inflate(inflater, container, false)
//        val adapter = HikeAdapter(createHikes())
//        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        binding.recyclerView.adapter = adapter
//        return binding.root
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    override fun toString(): String {
//        return "Activities"
//    }
//
//    private fun createHikes(): List<Hike> = buildList {
//        for (i in 1..50) {
//            val dist = (i*137)%30
//            val newHike = Hike("February 24, 2025 • Brzegi", "Rysy", 23, "6h 22m")
//            add(newHike)
//        }
//    }
//}
