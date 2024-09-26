package com.denyskostetskyi.systemmonitor.client

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.denyskostetskyi.systemmonitor.client.databinding.ActivityMainBinding
import com.denyskostetskyi.systemmonitor.library.ISystemMonitorService
import com.denyskostetskyi.systemmonitor.library.SystemMonitorServiceHelper
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var systemMonitorService: ISystemMonitorService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            systemMonitorService = ISystemMonitorService.Stub.asInterface(service)
            Log.d(TAG, "Bound to ${className.className}")
            updateUiLoadingState(false)
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
        bindSystemMonitorService()
        initViews()
    }

    private fun bindSystemMonitorService() {
        updateUiLoadingState(true)
        val intent = SystemMonitorServiceHelper.newIntent()
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun updateUiLoadingState(isLoading: Boolean) {
        with(binding) {
            progressBar.isVisible = isLoading
            buttonServiceRunningTime.isEnabled = !isLoading
            buttonSystemInfo.isEnabled = !isLoading
            buttonRunningProcessIds.isEnabled = !isLoading
        }
    }

    private fun initViews() {
        setServiceRunningTimeButtonClickListener()
        setSystemInfoButtonClickListener()
        setRunningProcessIdsButtonClickListener()
    }

    private fun setServiceRunningTimeButtonClickListener() {
        binding.buttonServiceRunningTime.setOnClickListener {
            callServiceInBackground {
                val runningTime = systemMonitorService?.serviceRunningTime
                runOnUiThread {
                    val result =
                        getString(R.string.result_service_running_time, runningTime.toString())
                    binding.textViewServiceRunningTime.text = result
                }
            }
        }
    }

    private fun setSystemInfoButtonClickListener() {
        binding.buttonSystemInfo.setOnClickListener {
            callServiceInBackground {
                val systemInfo = systemMonitorService?.systemInfo
                runOnUiThread {
                    binding.textViewSystemInfo.text = systemInfo
                }
            }
        }
    }

    private fun setRunningProcessIdsButtonClickListener() {
        binding.buttonRunningProcessIds.setOnClickListener {
            callServiceInBackground {
                val processIds = systemMonitorService?.runningProcessIds
                val formattedIds = formatIntArray(processIds)
                runOnUiThread {
                    val result = getString(R.string.result_running_process_ids, formattedIds)
                    binding.textViewRunningProcessIds.text = result
                }
            }
        }
    }

    private fun callServiceInBackground(call: () -> Unit) {
        if (systemMonitorService == null) {
            bindSystemMonitorService()
            return
        }
        updateUiLoadingState(true)
        thread {
            try {
                call.invoke()
            } catch (e: RemoteException) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.could_not_connect_to_service),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            runOnUiThread { updateUiLoadingState(false) }
        }
    }

    private fun formatIntArray(array: IntArray?) = array?.joinToString(
        prefix = "[ ",
        postfix = " ]",
        separator = ", "
    ) ?: "[]"


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
