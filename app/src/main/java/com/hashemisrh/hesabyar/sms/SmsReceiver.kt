package com.hashemisrh.hesabyar.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.hashemisrh.hesabyar.data.AppDb
import com.hashemisrh.hesabyar.data.Transaction
import java.security.MessageDigest

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val db=AppDb(context.applicationContext)
        val messages=Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val body=messages.joinToString("") { it.messageBody ?: "" }
        if(body.isBlank()) return
        val sender=messages.firstOrNull()?.originatingAddress
        val received=messages.minOfOrNull{it.timestampMillis} ?: System.currentTimeMillis()
        val fp=sha256((sender?:(""))+"|"+body+"|"+received)
        if(db.rawExists(fp))return
        val rawId=db.addRaw(sender,body,received,fp)
        val parsed=SmsParser.parse(body,db.accounts())
        if(parsed.matched && parsed.accountId!=null && parsed.amount!=null && parsed.dateText!=null){
            db.addTransaction(Transaction(0,parsed.accountId,parsed.direction!!,parsed.nature,parsed.amount,parsed.dateText,parsed.smsBalance,null,null,parsed.title,parsed.channel,parsed.description,parsed.tracking,rawId,"POSTED",System.currentTimeMillis()))
            db.setRawStatus(rawId,"POSTED")
        } else db.setRawStatus(rawId,"REVIEW")
    }
    private fun sha256(s:String):String=MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString(""){ "%02x".format(it) }
}
