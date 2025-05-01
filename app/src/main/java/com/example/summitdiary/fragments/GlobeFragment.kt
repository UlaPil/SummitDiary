package com.example.summitdiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.summitdiary.GlobeHikeAdapter
import com.example.summitdiary.databinding.FragmentGlobeBinding
import com.example.summitdiary.network.ApiClient
import com.example.summitdiary.network.HikeDto
import com.example.summitdiary.workers.SyncWorker
import kotlinx.coroutines.launch

class GlobeFragment : Fragment() {

    private var _binding: FragmentGlobeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GlobeHikeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGlobeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = GlobeHikeAdapter { hike ->
            applyHike(hike)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.buttonSyncNow.setOnClickListener {
            val workRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
            WorkManager.getInstance(requireContext()).enqueue(workRequest)
        }

        fetchHikes()
    }

    private fun fetchHikes() {
        lifecycleScope.launch {
            try {
                val hikes = ApiClient.apiService.getHikes()
                adapter.updateData(hikes)
                binding.errorText.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
                binding.errorText.text = "Brak połączenia z serwerem"
                binding.errorText.visibility = View.VISIBLE
            }
        }
    }

    private fun applyHike(hike: HikeDto) {
        // Tu możesz dodać logikę do „przepisania” globalnego hiku do lokalnej bazy
        // Na początek wrzucamy tylko log do konsoli
        println("Kliknięto Apply dla hiku: ${hike.title}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun toString(): String {
        return "Global Activities"
    }
}


//package com.example.summitdiary.fragments
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import com.example.summitdiary.R
//import com.example.summitdiary.databinding.FragmentGlobeBinding
//import androidx.work.OneTimeWorkRequestBuilder
//import androidx.work.WorkManager
//import com.example.summitdiary.workers.SyncWorker
//
//class GlobeFragment : Fragment(R.layout.fragment_globe) {
//    private var _binding: FragmentGlobeBinding? = null
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentGlobeBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.buttonSyncNow.setOnClickListener {
//            val workRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
//            WorkManager.getInstance(requireContext()).enqueue(workRequest)
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    override fun toString(): String {
//        return "Global Activities"
//    }
//}
