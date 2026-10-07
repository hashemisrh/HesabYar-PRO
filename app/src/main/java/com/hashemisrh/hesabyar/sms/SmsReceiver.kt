package com.hashemisrh.hesabyar.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val rawBody = messages.joinToString(separator = "") { it.messageBody ?: "" }
        val sender = messages.firstOrNull()?.originatingAddress.orEmpty()
        val receivedAt = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()

        // IMPORTANT:
        // 1) Raw SMS is captured unchanged.
        // 2) Sender/short-code is diagnostic only, never the bank/account identity.
        // 3) Transaction date/time will later be extracted from the SMS body.
        // 4) The parser will return REVIEW instead of guessing on ambiguity.
        Log.d("HesabYarSms", "SMS captured: sender=$sender, receivedAt=$receivedAt, length=${rawBody.length}")

        // Parser/DB pipeline will be connected here:
        // Raw SMS -> normalize -> pattern -> account -> extract -> validate -> transaction/review.
    }
}
