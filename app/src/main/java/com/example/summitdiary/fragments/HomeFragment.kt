package com.example.summitdiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.summitdiary.Hike
import com.example.summitdiary.HikeAdapter
import com.example.summitdiary.R
import com.example.summitdiary.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    // Zmienna do przechowywania instancji ViewBinding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!! // Używaj go tylko po sprawdzeniu, że _binding != null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inicjalizacja ViewBinding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val adapter = HikeAdapter(createHikes())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        return binding.root // Zwróć root widoku z bindingu
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Zapewnij, że binding jest anulowany, aby uniknąć wycieków pamięci
        _binding = null
    }

    override fun toString(): String {
        return "Activities"
    }

    private fun createHikes(): List<Hike> = buildList {
        for (i in 1..50) {
            val dist = (i*137)%30
            val newHike = Hike("February 24, 2025 • Brzegi", "Rysy", 23, "6h 22m")
            add(newHike)
        }
    }
}
