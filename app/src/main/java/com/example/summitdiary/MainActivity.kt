package com.example.summitdiary

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.summitdiary.databinding.BaseBinding
import com.example.summitdiary.fragments.MapFragment
import com.example.summitdiary.fragments.GlobeFragment
import androidx.core.graphics.toColorInt
import com.example.summitdiary.fragments.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: BaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = "#467272".toColorInt()
        }
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace<HomeFragment>(R.id.mainContainer)
            }
        }
        binding.buttonHome.setOnClickListener {
            replaceFragment(HomeFragment())
        }
        binding.buttonMap.setOnClickListener {
            replaceFragment(MapFragment())
        }
        binding.buttonGlobe.setOnClickListener {
            replaceFragment(GlobeFragment())
        }
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        binding.topBarTitle.text = fragment.toString()
        supportFragmentManager.commit {
            replace(R.id.mainContainer, fragment)
        }
    }
}