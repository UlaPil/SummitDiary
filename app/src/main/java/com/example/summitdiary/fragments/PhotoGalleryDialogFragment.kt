package com.example.summitdiary.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import java.io.File
import com.github.chrisbanes.photoview.PhotoView

class PhotoGalleryDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_PATHS = "photo_paths"
        private const val ARG_INDEX = "photo_index"

        fun newInstance(paths: List<String>, index: Int): PhotoGalleryDialogFragment {
            val fragment = PhotoGalleryDialogFragment()
            val args = Bundle()
            args.putStringArrayList(ARG_PATHS, ArrayList(paths))
            args.putInt(ARG_INDEX, index)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewPager = ViewPager2(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val photoPaths = arguments?.getStringArrayList(ARG_PATHS).orEmpty()
        val startIndex = arguments?.getInt(ARG_INDEX) ?: 0

        viewPager.adapter = object : RecyclerView.Adapter<PhotoViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
                val photoView = PhotoView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(0xFF000000.toInt())
                }
                return PhotoViewHolder(photoView)
            }

            override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
                Glide.with(holder.photoView.context)
                    .load(File(photoPaths[position]))
                    .into(holder.photoView)
            }

            override fun getItemCount(): Int = photoPaths.size
        }

        viewPager.setCurrentItem(startIndex, false)

        return Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(viewPager)
        }
    }

    class PhotoViewHolder(val photoView: PhotoView) : RecyclerView.ViewHolder(photoView)
}
