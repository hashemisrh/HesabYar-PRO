package com.hashemisrh.hesabyar.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d("HesabYarBoot", "Receiver ready after boot/update")
                // Persistent SMS processing requires no open Activity.
                // Background notification/health check will be connected later.
            }
        }
    }
}
