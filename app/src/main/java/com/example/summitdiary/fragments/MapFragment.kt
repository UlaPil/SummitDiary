package com.example.summitdiary.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.summitdiary.R
import com.example.summitdiary.database.Photo
import com.example.summitdiary.databinding.FragmentMapBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class MapFragment : Fragment(R.layout.fragment_map) {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
//    private val binding get() = _binding!!
//
//    private lateinit var photoUri: Uri
//    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
//        if (isSuccess) {
//            savePhoto(photoUri)
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentMapBinding.inflate(inflater, container, false)
//        binding.buttonA.setOnClickListener {
//            takePhoto()
//        }
//        return binding.root
//    }
//
//    private fun takePhoto() {
//        val photoFile = File(requireContext().filesDir, "photo_${System.currentTimeMillis()}.jpg")
//        photoUri = FileProvider.getUriForFile(
//            requireContext(),
//            "${requireContext().packageName}.provider",
//            photoFile
//        )
//
//        takePictureLauncher.launch(photoUri)
//    }
//
//    private fun savePhoto(photoUri: Uri) {
//        val photo = Photo(path = photoUri.toString(), location = "a")
//        // Tutaj dodajesz do Room
//        GlobalScope.launch {
////            PhotoDao.insert(photo)
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }

    override fun toString(): String {
        return "Map"
    }
}
