package com.example.summitdiary.fragments

import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import com.example.summitdiary.HikeAdapter
import com.example.summitdiary.R
import com.example.summitdiary.database.HikeWithPhotos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import com.example.summitdiary.database.HikeDao
import com.example.summitdiary.database.AppDatabase
import com.example.summitdiary.database.HikePhotoDao
import com.example.summitdiary.database.PhotoDao

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var hikeDao: HikeDao
    private lateinit var photoDao: PhotoDao
    private lateinit var hikePhotoDao: HikePhotoDao
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
        val db = AppDatabase.getDatabase(requireContext())
        hikeDao = db.hikeDao()
        photoDao = db.photoDao()
        hikePhotoDao = db.hikePhotoDao()
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        adapter = HikeAdapter(
            emptyList(),
            onDeleteClick = { hikeWithPhotos -> deleteHikeWithPhotos(hikeWithPhotos) },
            onTitleChange = { newTitle, hikeWithPhotos -> updateHikeTitle(newTitle, hikeWithPhotos) },
            onMapClick = { hike ->
                val intent = Intent(requireContext(), MapTrackActivity::class.java).apply {
                    putExtra("gpxPath", hike.hike.gpx_path)
                    putExtra("hikeId", hike.hike.hike_id)
                }
                startActivity(intent)
            },
            AppDatabase.getDatabase(requireContext()).placeDao()
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.hikesWithPhotos.observe(viewLifecycleOwner) { hikeList ->
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

    private fun deleteHikeWithPhotos(hikeWithPhotos: HikeWithPhotos) {
        lifecycleScope.launch(Dispatchers.IO) {
            val hikeId = hikeWithPhotos.hike.hike_id
            hikePhotoDao.deleteByHikeId(hikeId)
            for (photo in hikeWithPhotos.photos) {
                try {
                    val file = File(photo.path)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                photoDao.delete(photo)
            }
            hikeDao.delete(hikeWithPhotos.hike)
        }
    }

    private fun updateHikeTitle(newTitle: String, hikeWithPhotos: HikeWithPhotos) {
        lifecycleScope.launch(Dispatchers.IO) {
            val updated = hikeWithPhotos.hike.apply { title = newTitle }
            hikeDao.update(updated)
        }
    }
}