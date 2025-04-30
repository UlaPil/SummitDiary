package com.example.summitdiary


import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.summitdiary.database.HikeWithPhotos
import com.example.summitdiary.database.PlaceDao
import com.example.summitdiary.databinding.HikeRowBinding
import com.example.summitdiary.fragments.PhotoGalleryDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class HikeAdapter(
    private var hikes: List<HikeWithPhotos>,
    private val onDeleteClick: (HikeWithPhotos) -> Unit,
    private val onTitleChange: (newTitle: String, hike: HikeWithPhotos) -> Unit,
    private val onMapClick: (HikeWithPhotos) -> Unit,
    private val placeDao: PlaceDao
) : RecyclerView.Adapter<HikeAdapter.HikeViewHolder>() {

    inner class HikeViewHolder(val binding: HikeRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HikeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val hikeRowBinding = HikeRowBinding.inflate(inflater, parent, false)
        return HikeViewHolder(hikeRowBinding)
    }

    override fun onBindViewHolder(holder: HikeViewHolder, position: Int) {
        val hikeWithPhotos = hikes[position]
        val hike = hikeWithPhotos.hike
        holder.binding.apply {
            title.text = hike.title
            distance.text = String.format(Locale.getDefault(), "%.2f km", hike.distance)
            time.text = hike.time
            dateAndPlace.text = "${hike.date} • ${hike.place_id}"
            CoroutineScope(Dispatchers.IO).launch {
                val placeName = placeDao.getById(hike.place_id)
                withContext(Dispatchers.Main) {
                    dateAndPlace.text = "${hike.date} • ${placeName ?: "nieznane"}"
                }
            }
            imageGallery.removeAllViews()

            for (photo in hikeWithPhotos.photos) {
                val imageView = ImageView(holder.itemView.context).apply {
                    layoutParams = LinearLayout.LayoutParams(370, 300).apply {
                        rightMargin = 8.dpToPx(context)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }

                Glide.with(holder.itemView.context)
                    .load(File(photo.path))
                    .centerCrop()
                    .into(imageView)

                imageGallery.addView(imageView)

                imageView.setOnClickListener {
                    val context = it.context
                    val activity = context as? AppCompatActivity
                    activity?.supportFragmentManager?.let { fragmentManager ->
                        val allPhotoPaths = hikeWithPhotos.photos.map { p -> p.path }
                        val clickedIndex = hikeWithPhotos.photos.indexOf(photo)
                        val dialog = PhotoGalleryDialogFragment.newInstance(allPhotoPaths, clickedIndex)
                        dialog.show(fragmentManager, "photo_gallery")
                    }
                }
            }
        }
        holder.binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Usuń wędrówkę")
                .setMessage("Czy na pewno chcesz usunąć \"${hike.title}\"?")
                .setPositiveButton("Usuń") { _, _ ->
                    onDeleteClick(hikeWithPhotos)
                }
                .setNegativeButton("Anuluj", null)
                .show()
        }
        holder.binding.mapButton.setOnClickListener {
            onMapClick(hikeWithPhotos)
        }
        holder.binding.title.setOnClickListener {
            holder.binding.title.setOnClickListener {
                val context = holder.itemView.context
                val editText = EditText(context).apply {
                    setText(hike.title)
                    setPadding(32, 32, 32, 32)
                }

                AlertDialog.Builder(context)
                    .setTitle("Zmień tytuł wędrówki")
                    .setView(editText)
                    .setPositiveButton("Zapisz") { _, _ ->
                        val newTitle = editText.text.toString().trim()
                        if (newTitle.isNotEmpty()) {
                            onTitleChange(newTitle, hikeWithPhotos)
                        }
                    }
                    .setNegativeButton("Anuluj", null)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int = hikes.size

    fun updateData(newHikes: List<HikeWithPhotos>) {
        hikes = newHikes
        notifyDataSetChanged()
    }

    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}