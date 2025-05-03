package com.example.summitdiary

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.summitdiary.databinding.BaseBinding
import com.example.summitdiary.fragments.MapFragment
import com.example.summitdiary.fragments.GlobeFragment
import androidx.core.graphics.toColorInt
import com.example.summitdiary.fragments.HomeFragment
import androidx.work.*
import com.example.summitdiary.workers.SyncWorker
import java.util.concurrent.TimeUnit
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import android.content.Context
import com.example.summitdiary.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: BaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestIgnoreBatteryOptimizations(this)
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

        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "syncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        Log.d("MainActivity", "SyncWorker scheduled")
    }


    fun requestIgnoreBatteryOptimizations(context: Context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = context.packageName
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = android.net.Uri.parse("package:$packageName")
            context.startActivity(intent)
        }
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        binding.topBarTitle.text = fragment.toString()
        supportFragmentManager.commit {
            replace(R.id.mainContainer, fragment)
        }
    }
}