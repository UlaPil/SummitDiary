package com.example.summitdiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.summitdiary.R
import com.example.summitdiary.databinding.FragmentGlobeBinding
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.summitdiary.workers.SyncWorker

class GlobeFragment : Fragment(R.layout.fragment_globe) {
    private var _binding: FragmentGlobeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGlobeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSyncNow.setOnClickListener {
            val workRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
            WorkManager.getInstance(requireContext()).enqueue(workRequest)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun toString(): String {
        return "Global Activities"
    }
}
