package com.example.abe.services

import android.content.Intent

import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.abe.data.local.PreferenceDataStoreConstants
import com.example.abe.data.local.PreferenceDataStoreHelper
import com.example.abe.data.network.CheckAuthResultCallback
import com.example.abe.data.network.Retrofit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AuthService : LifecycleService(), CheckAuthResultCallback {
    var isRunning: Boolean = false
    private lateinit var preferenceDataStoreHelper: PreferenceDataStoreHelper
    override fun onCreate() {
        super.onCreate()
        preferenceDataStoreHelper = PreferenceDataStoreHelper(applicationContext)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        isRunning = true
        lifecycleScope.launch {
            while (isRunning) {
                val retrofit = Retrofit()
                val token = preferenceDataStoreHelper.getFirstPreference(
                    PreferenceDataStoreConstants.TOKEN,
                    ""
                )
                retrofit.checkAuth(token, this@AuthService)
                delay(30000)
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onFailure() {
        Intent().also { intent ->
            intent.setAction("EXPIRED_TOKEN")
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
}