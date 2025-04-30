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
import com.example.summitdiary.databinding.HikeRowBinding
import com.example.summitdiary.fragments.PhotoPreviewDialogFragment
import java.io.File

class HikeAdapter(
    private var hikes: List<HikeWithPhotos>,
    private val onDeleteClick: (HikeWithPhotos) -> Unit,
    private val onTitleChange: (newTitle: String, hike: HikeWithPhotos) -> Unit
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
            distance.text = "${hike.distance} km"
            time.text = hike.time
            dateAndPlace.text = "${hike.date} • ${hike.place_id}"
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
                        val dialog = PhotoPreviewDialogFragment.newInstance(photo.path)
                        dialog.show(fragmentManager, "photo_preview")
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