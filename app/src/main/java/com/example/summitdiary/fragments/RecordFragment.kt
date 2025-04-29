package com.example.summitdiary.fragments


import com.example.summitdiary.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.summitdiary.databinding.FragmentRecordBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.example.summitdiary.database.AppDatabase
import com.example.summitdiary.database.Hike
import com.example.summitdiary.database.HikeDao
import com.example.summitdiary.database.HikePhoto
import com.example.summitdiary.database.HikePhotoDao
import com.example.summitdiary.database.PhotoDao
import com.example.summitdiary.database.Photo
import androidx.lifecycle.lifecycleScope
import com.example.summitdiary.viewmodel.RecordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordFragment : Fragment() {
    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var photoDao: PhotoDao
    private lateinit var hikePhotoDao: HikePhotoDao
    private lateinit var hikeDao: HikeDao

    private val viewModel: RecordViewModel by viewModels()
    private var startTimeInMillis: Long? = 0L
    private var elapsedSeconds = 0L
    private var isRecording = false
    private var photoUri: Uri? = null
    private var timerJob: Job? = null

    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private var currentPhotoPath: String? = null
    private val CAMERA_PERMISSION_CODE = 100

    companion object {
        private const val KEY_IS_RECORDING = "key_is_recording"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isRecording = savedInstanceState?.getBoolean(KEY_IS_RECORDING) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startTimeInMillis = savedInstanceState?.getLong("startTimeInMillis")
        elapsedSeconds = savedInstanceState?.getLong("elapsedSeconds") ?: 0L
        if (startTimeInMillis != null && isRecording) {
            startTimer()
        }
        val db = AppDatabase.getDatabase(requireContext())
        hikeDao = db.hikeDao()
        photoDao = db.photoDao()
        hikePhotoDao = db.hikePhotoDao()
        updateStartStopIcon()

        binding.startStopButton.setOnClickListener {
            isRecording = !isRecording
            updateStartStopIcon()

            if (isRecording) {
                startTimeInMillis = System.currentTimeMillis()
                elapsedSeconds = 0L
                startTimer()

                lifecycleScope.launch(Dispatchers.IO) {
                    val hike = Hike(
                        title = "Nowa wędrówka",
                        date = SimpleDateFormat("dd.MM.yyyy (HH:mm)", Locale.getDefault()).format(Date()),
                        distance = 0,
                        time = "0",
                        place_id = 1,
                        gpx_path = ""
                    )
                    viewModel.currentHikeId = hikeDao.insert(hike)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Rozpoczęto wędrówkę! ID: ${viewModel.currentHikeId}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                stopTimer()
                binding.timeText.text = "00:00:00"
                showNameHikeDialog()
                Toast.makeText(requireContext(), "Zakończono wędrówkę", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cameraButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.locationButton.setOnClickListener {
            // TODO: Show current location
        }

        requestCameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Brak pozwolenia na aparat", Toast.LENGTH_SHORT).show()
            }
        }

        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                currentPhotoPath?.let { path ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.currentHikeId?.let { hikeId ->
                            val photoId = photoDao.insert(Photo(location = null, path = path))
                            if (photoId != null) {
                                hikePhotoDao.insert(HikePhoto(
                                    hike_id = hikeId,
                                    photo_id = photoId
                                ))
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "Zdjęcie zapisane do Hike $hikeId", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "Błąd: Nie udało się zapisać zdjęcia", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } ?: run {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Błąd: brak aktywnej wędrówki!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Anulowano robienie zdjęcia.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStartStopIcon() {
        if (isRecording) {
            binding.startStopButton.setImageResource(R.drawable.baseline_stop_24)
        } else {
            binding.startStopButton.setImageResource(R.drawable.baseline_play_arrow_24)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_RECORDING, isRecording)
        startTimeInMillis.let { outState.putLong("startTimeInMillis", startTimeInMillis ?: 0L)
        }
        outState.putLong("elapsedSeconds", elapsedSeconds)
    }

    private fun dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider", // <- bardzo ważne!
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePictureLauncher.launch(intent)
        }
    }

    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun showNameHikeDialog() {
        val editText = EditText(requireContext())
        editText.hint = "Podaj nazwę wędrówki"
        editText.setPadding(32, 32, 32, 32)

        val durationFormatted = formatDuration(elapsedSeconds)

        AlertDialog.Builder(requireContext())
            .setTitle("Zakończono wędrówkę")
            .setMessage("Czas trwania: $durationFormatted\nPodaj nazwę wędrówki:")
            .setView(editText)
            .setPositiveButton("Zapisz") { _, _ ->
                val newTitle = editText.text.toString().ifBlank { "Wędrówka $durationFormatted" }
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.currentHikeId?.let { hikeId ->
                        val hike = hikeDao.getHikeById(hikeId)
                        hike?.let {
                            it.title = newTitle
                            it.time = durationFormatted
                            hikeDao.update(it)
                        }
                    }
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                // Tutaj masz Uri do zapisanego zdjęcia!
                // Możesz np. wyświetlić je w ImageView, zapisać do bazy itd.
            }
        }
    }

    private fun startTimer() {
        timerJob = lifecycleScope.launch {
            while (isRecording) {
                delay(1000)
                elapsedSeconds++
                updateTimerUI()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun updateTimerUI() {
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        binding.timeText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}