package com.example.summitdiary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.summitdiary.databinding.HikeRowGlobalBinding
import com.example.summitdiary.network.HikeDto
import java.util.*

class GlobeHikeAdapter(
    private val onApplyClick: (HikeDto) -> Unit
) : RecyclerView.Adapter<GlobeHikeAdapter.HikeViewHolder>() {

    private var hikes: List<HikeDto> = emptyList()

    inner class HikeViewHolder(val binding: HikeRowGlobalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HikeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = HikeRowGlobalBinding.inflate(inflater, parent, false)
        return HikeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HikeViewHolder, position: Int) {
        val hike = hikes[position]
        holder.binding.apply {
            title.text = hike.title
            distance.text = String.format(Locale.getDefault(), "%.2f km", hike.distance)
            time.text = hike.time
            dateAndPlace.text = "${hike.date} • miejsce • user ${hike.userId}"
            imageGallery.removeAllViews() // brak zdjęć

            applyButton.setOnClickListener {
                onApplyClick(hike)
            }
        }
    }

    override fun getItemCount(): Int = hikes.size

    fun updateData(newHikes: List<HikeDto>) {
        hikes = newHikes
        notifyDataSetChanged()
    }
}
