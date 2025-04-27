package com.example.summitdiary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.summitdiary.database.Hike
import com.example.summitdiary.databinding.HikeRowBinding

class HikeAdapter(private var hikes: List<Hike>) :
    RecyclerView.Adapter<HikeAdapter.HikeViewHolder>() {

    inner class HikeViewHolder(val binding: HikeRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HikeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val hikeRowBinding = HikeRowBinding.inflate(inflater, parent, false)
        return HikeViewHolder(hikeRowBinding)
    }

    override fun onBindViewHolder(holder: HikeViewHolder, position: Int) {
        val hike = hikes[position]
        holder.binding.apply {
            title.text = hike.title
            distance.text = "${hike.distance} km"
            time.text = hike.time
            dateAndPlace.text = "${hike.date} | miejsce ID: ${hike.place_id}"
            // TODO: Obsługa imageGallery jeśli będą zdjęcia
        }
    }

    override fun getItemCount(): Int = hikes.size

    fun updateData(newHikes: List<Hike>) {
        hikes = newHikes
        notifyDataSetChanged()
    }
}


//package com.example.summitdiary
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import androidx.recyclerview.widget.RecyclerView.ViewHolder
//import com.example.summitdiary.databinding.HikeRowBinding
//
//class HikeAdapter(private var hikes: List<Hike>) :
//    RecyclerView.Adapter<HikeAdapter.HikeViewHolder>() {
//
//    inner class HikeViewHolder(binding: HikeRowBinding) : ViewHolder(binding.root) {
//        val dateAndPlace: TextView = binding.dateAndPlace
//        val title: TextView = binding.title
//        val distance: TextView = binding.distance
//        val time: TextView = binding.time
////        TODO: imageGallery
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HikeViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        val hikeRowBinding = HikeRowBinding.inflate(inflater, parent,false)
//        return HikeViewHolder(hikeRowBinding)
//    }
//
//    override fun onBindViewHolder(holder: HikeViewHolder, position: Int) {
//        val hike = hikes[position]
//        holder.dateAndPlace.text = "${hike.date} | miejsce ID: ${hike.place_id}"
//        holder.title.text = hike.title
//        holder.distance.text = buildString {
//            append(hike.distance)
//            append(" km")
//        }
//        holder.time.text = hike.time
////      TODO: imageGallery
//
////        Glide.with(holder.itemView.context)
////            .load(hike.imagePath)
////            .into(holder.image)
//    }
//
//    override fun getItemCount() = hikes.size
//
//    fun updateData(newHikes: List<Hike>) {
//        hikes = newHikes
//        notifyDataSetChanged()
//    }
//
//}

//package com.example.summitdiary
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import androidx.recyclerview.widget.RecyclerView.ViewHolder
//import com.example.summitdiary.databinding.HikeRowBinding
//
//class HikeAdapter(private val hikes: List<Hike>) :
//    RecyclerView.Adapter<HikeAdapter.HikeViewHolder>() {
//
//    inner class HikeViewHolder(binding: HikeRowBinding) : ViewHolder(binding.root) {
//        val dateAndPlace: TextView = binding.dateAndPlace
//        val title: TextView = binding.title
//        val distance: TextView = binding.distance
//        val time: TextView = binding.time
////        TODO: imageGallery
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HikeViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        val hikeRowBinding = HikeRowBinding.inflate(inflater, parent,false)
//        return HikeViewHolder(hikeRowBinding)
//    }
//
//    override fun onBindViewHolder(holder: HikeViewHolder, position: Int) {
//        val hike = hikes[position]
//        holder.dateAndPlace.text = hike.dateAndPlace
//        holder.title.text = hike.title
//        holder.distance.text = buildString {
//            append(hike.distance)
//            append(" km")
//        }
//        holder.time.text = hike.time
////      TODO: imageGallery
//    }
//
//    override fun getItemCount() = hikes.size
//}