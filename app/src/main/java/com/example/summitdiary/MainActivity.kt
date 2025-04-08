package com.example.summitdiary

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.summitdiary.databinding.BaseBinding
import com.example.summitdiary.fragments.HomeFragment
import com.example.summitdiary.fragments.MapFragment
import com.example.summitdiary.fragments.GlobeFragment

class MainActivity : AppCompatActivity() {

    // Inicjalizacja ViewBinding
    private lateinit var binding: BaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Używamy ViewBinding do ustawienia widoku
        binding = BaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.parseColor("#467272") // Ustaw kolor, np. #467272
        }
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//            window.navigationBarColor = Color.parseColor("#467272") // Ustaw kolor paska nawigacyjnego
//        }


        // Inicjalizacja fragmentu Home na początek
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace<HomeFragment>(binding.mainContainer.id)
            }
        }

        // Obsługa przycisków na dolnym pasku
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

    // Funkcja pomocnicza do zamiany fragmentu
    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        binding.topBarTitle.text = fragment.toString()
        supportFragmentManager.commit {
            replace(R.id.mainContainer, fragment)
        }
    }
}



//class MainActivity : AppCompatActivity() {
//    private lateinit var binding : ActivityMainBinding
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val adapter = HikeAdapter(createHikes())
//        binding.recyclerView.layoutManager = LinearLayoutManager(applicationContext)
//        binding.recyclerView.adapter = adapter
//    }
//
//
//
//    private fun createHikes(): List<Hike> = buildList {
//        for (i in 1..50) {
//            val dist = (i*137)%30
//            val newHike = Hike("New activity $i", "Miejsce", dist, null)
//            add(newHike)
//        }
//    }
//}