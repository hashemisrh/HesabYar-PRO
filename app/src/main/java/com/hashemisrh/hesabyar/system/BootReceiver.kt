package com.hashemisrh.hesabyar.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent){if(intent.action==Intent.ACTION_BOOT_COMPLETED||intent.action==Intent.ACTION_MY_PACKAGE_REPLACED){/* SMS_RECEIVED receiver is manifest based and needs no service restart. */}}}
