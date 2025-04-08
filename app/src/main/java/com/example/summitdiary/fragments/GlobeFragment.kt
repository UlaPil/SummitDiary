package com.example.summitdiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.summitdiary.R
import com.example.summitdiary.databinding.FragmentGlobeBinding

class GlobeFragment : Fragment(R.layout.fragment_globe) {

    // Zmienna do przechowywania instancji ViewBinding
    private var _binding: FragmentGlobeBinding? = null
    private val binding get() = _binding!! // Używaj go tylko po sprawdzeniu, że _binding != null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inicjalizacja ViewBinding
        _binding = FragmentGlobeBinding.inflate(inflater, container, false)

        // Teraz możesz uzyskać dostęp do widoków za pomocą bindinga, np.:
        // binding.textView.text = "Globe Fragment"

        return binding.root // Zwróć root widoku z bindingu
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Zapewnij, że binding jest anulowany, aby uniknąć wycieków pamięci
        _binding = null
    }

    override fun toString(): String {
        return "Global Activities"
    }
}
