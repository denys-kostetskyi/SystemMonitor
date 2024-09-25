package com.denyskostetskyi.systemmonitor.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.denyskostetskyi.systemmonitor.client.databinding.ActivityMainBinding
import com.denyskostetskyi.systemmonitor.library.ISystemMonitorService
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var systemMonitorService: ISystemMonitorService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            systemMonitorService = ISystemMonitorService.Stub.asInterface(service)
            updateUiLoadingState(false)
            testSystemMonitorService()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            systemMonitorService = null
            Toast.makeText(
                this@MainActivity,
                getString(R.string.service_unexpectedly_disconnected),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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
    }

    private fun initViews() {
        with(binding) {
            buttonTest.setOnClickListener {
                updateUiLoadingState(true)
                bindSystemMonitorService()
            }
        }
    }

    private fun updateUiLoadingState(isLoading: Boolean) {
        with(binding) {
            buttonTest.isEnabled = !isLoading
            progressBar.isVisible = isLoading
        }
    }

    private fun bindSystemMonitorService() {
        val intent = Intent() //TODO use valid intent
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun testSystemMonitorService() {
        systemMonitorService?.let {
            thread {
                Log.d(TAG, "System information: ${it.systemInfo}")
                Log.d(TAG, "SystemMonitorService running time: ${it.serviceRunningTime}")
                Log.d(TAG, "Running process ids: ${it.runningProcessIds}")
            }
        }
    }

    private fun unbindSystemMonitorService() {
        unbindService(serviceConnection)
        systemMonitorService = null
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindSystemMonitorService()
    }

    companion object {
        private const val TAG = "ClientMain"
    }
}
