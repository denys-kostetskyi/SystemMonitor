package com.denyskostetskyi.systemmonitor.server

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.denyskostetskyi.systemmonitor.server.databinding.ActivityServerBinding


class ServerActivity : AppCompatActivity() {
    private val binding by lazy { ActivityServerBinding.inflate(layoutInflater) }
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initViews()
        checkPermissions()
    }

    private fun initViews() {
        updateUiStateServiceRunning(SystemMonitorService.isServiceRunning)
        binding.buttonStartService.setOnClickListener {
            startSystemMonitorService()
        }
    }

    private fun updateUiStateServiceRunning(isRunning: Boolean) {
        with(binding) {
            textViewServiceRunning.isVisible = isRunning
            buttonStartService.isVisible = !isRunning
        }
    }

    private fun startSystemMonitorService() {
        val intent = SystemMonitorService.newIntent(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        updateUiStateServiceRunning(true)
    }

    private fun checkPermissions() {
        checkNotificationPermission()
        checkIgnoreBatteryOptimizations()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result == PackageManager.PERMISSION_DENIED) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun checkIgnoreBatteryOptimizations() {
        val powerManager = getSystemService(PowerManager::class.java)
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent().apply {
                action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            }
            startActivity(intent)
        }
    }
}