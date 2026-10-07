package com.hashemisrh.hesabyar.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

class SecurityStore(context:Context){
    private val p:SharedPreferences=context.getSharedPreferences("security",Context.MODE_PRIVATE)
    fun hasPin()=p.contains("hash")
    fun biometricEnabled()=p.getBoolean("bio",false)
    fun setBiometric(enabled:Boolean){p.edit().putBoolean("bio",enabled).apply()}
    fun setPin(pin:String){val salt=ByteArray(16);SecureRandom().nextBytes(salt);p.edit().putString("salt",b64(salt)).putString("hash",hash(pin,salt)).apply()}
    fun verify(pin:String):Boolean{val salt=p.getString("salt",null)?.let{Base64.getDecoder().decode(it)}?:return false;return hash(pin,salt)==p.getString("hash","")}
    private fun hash(pin:String,salt:ByteArray):String{var x=salt+pin.toByteArray();repeat(12000){x=MessageDigest.getInstance("SHA-256").digest(x)};return b64(x)}
    private fun b64(b:ByteArray)=Base64.getEncoder().encodeToString(b)
}
