package com.example.summitdiary.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import java.io.File

class PhotoPreviewDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_PHOTO_PATH = "photo_path"

        fun newInstance(path: String): PhotoPreviewDialogFragment {
            val fragment = PhotoPreviewDialogFragment()
            val args = Bundle()
            args.putString(ARG_PHOTO_PATH, path)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val imageView = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(0xFF000000.toInt())
        }

        val path = arguments?.getString(ARG_PHOTO_PATH)
        if (!path.isNullOrBlank()) {
            Glide.with(this)
                .load(File(path))
                .into(imageView)
        }

        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(imageView)
        return dialog
    }
}