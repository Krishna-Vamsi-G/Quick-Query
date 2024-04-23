package com.example.quickquery.view.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.quickquery.databinding.FragmentScreenshotViewBinding

class ScreenshotView(private val imagePath: String) : Fragment() {
    private var _binding: FragmentScreenshotViewBinding? = null

    // Safe access to the binding property to avoid memory leaks.
    private val binding get() = _binding!!

    /**
     * Inflates the layout for this fragment and initializes binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScreenshotViewBinding.inflate(inflater, container, false)
        displayImage()
        displayImageName()

        return binding.root


    }

    private fun displayImageName() {
        binding.textView.text = imagePath
    }

    private fun displayImage() {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        binding.imageView.setImageBitmap(bitmap)
    }

    /**
     * Called after the fragment's view has been created.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * Ensures the binding is nullified when the view is destroyed to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
