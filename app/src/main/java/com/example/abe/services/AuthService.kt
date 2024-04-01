package com.example.abe.services

import android.app.Service
import android.content.Context
import android.content.Intent

import android.os.IBinder
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.abe.R
import com.example.abe.data.network.CheckAuthResponse
import com.example.abe.data.network.CheckAuthResultCallback
import com.example.abe.data.network.Retrofit


class AuthService : Service(), CheckAuthResultCallback {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            while (true) {
                Log.v("Servicecekut", "Service is running...")
                val retrofit = Retrofit()
                val sharedPref = getSharedPreferences(
                    getString(R.string.preference_file_key),
                    Context.MODE_PRIVATE
                )
                val token = sharedPref.getString("login_token", "").toString()
                Log.v("Servicecekut", token)
                retrofit.checkAuth(token, this)
                try {
                    Thread.sleep(30000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onFailure() {
        Intent().also { intent ->
            intent.setAction("EXPIRED_TOKEN")
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }
}