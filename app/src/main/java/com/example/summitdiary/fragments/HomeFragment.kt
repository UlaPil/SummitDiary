
package com.example.summitdiary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.summitdiary.databinding.FragmentHomeBinding
import com.example.summitdiary.viewmodel.HomeViewModel
import androidx.fragment.app.commit
import com.example.summitdiary.fragments.RecordFragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HikeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        adapter = HikeAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.hikes.observe(viewLifecycleOwner) { hikeList ->
            adapter.updateData(hikeList)
        }

        binding.fabAdd.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.mainContainer, RecordFragment())
                addToBackStack(null)
            }
        }
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
