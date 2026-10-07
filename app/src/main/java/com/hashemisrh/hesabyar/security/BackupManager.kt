package com.hashemisrh.hesabyar.security

import android.content.Context
import com.hashemisrh.hesabyar.data.AppDb
import com.hashemisrh.hesabyar.data.Transaction
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupManager {
    private const val VERSION=1
    fun export(context:Context,pin:String):ByteArray{
        val db=AppDb(context); val root=JSONObject(); root.put("version",VERSION); root.put("createdAt",System.currentTimeMillis())
        val a=JSONArray();db.accounts().forEach{val o=JSONObject();o.put("id",it.id);o.put("bankId",it.bankId);o.put("name",it.name);o.put("identifier",it.identifier);o.put("openingBalance",it.openingBalance);a.put(o)};root.put("accounts",a)
        val t=JSONArray();db.allTransactions().forEach{val o=JSONObject();o.put("accountId",it.accountId);o.put("direction",it.direction);o.put("nature",it.nature);o.put("amount",it.amount);o.put("dateText",it.dateText);o.put("smsBalance",it.smsBalance);o.put("category",it.category);o.put("title",it.title);o.put("channel",it.channel);o.put("description",it.description);o.put("tracking",it.tracking);t.put(o)};root.put("transactions",t)
        val r=JSONArray();db.allRaw().forEach{val o=JSONObject();o.put("sender",it.sender);o.put("body",it.body);o.put("receivedAt",it.receivedAt);o.put("fingerprint",it.fingerprint);o.put("status",it.status);r.put(o)};root.put("reviewSms",r)
        return encrypt(root.toString().toByteArray(Charsets.UTF_8),pin)
    }
    fun decrypt(data:ByteArray,pin:String):String{val bb=ByteBuffer.wrap(data);val salt=ByteArray(16);bb.get(salt);val iv=ByteArray(12);bb.get(iv);val n=ByteArray(bb.remaining());bb.get(n);val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(pin,salt),GCMParameterSpec(128,iv));return String(c.doFinal(n),Charsets.UTF_8)}
    private fun encrypt(data:ByteArray,pin:String):ByteArray{val salt=ByteArray(16);val iv=ByteArray(12);SecureRandom().nextBytes(salt);SecureRandom().nextBytes(iv);val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key(pin,salt),GCMParameterSpec(128,iv));val out=c.doFinal(data);return ByteBuffer.allocate(28+out.size).put(salt).put(iv).put(out).array()}
    private fun key(pin:String,salt:ByteArray):SecretKeySpec{val spec=PBEKeySpec(pin.toCharArray(),salt,150000,256);val b=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded;return SecretKeySpec(b,"AES")}
    fun restore(context:Context,json:String){
        val root=JSONObject(json);if(root.optInt("version",0)>VERSION)throw IllegalArgumentException("نسخه پشتیبان جدیدتر است")
        val db=AppDb(context);val map=HashMap<Long,Long>()
        val a=root.optJSONArray("accounts")?:JSONArray();for(i in 0 until a.length()){val o=a.getJSONObject(i);val bankId=o.getLong("bankId");val existing=db.accounts().firstOrNull{it.bankId==bankId&&it.identifier==o.optString("identifier")};val id=existing?.id?:db.addAccount(bankId,o.optString("name"),o.optString("identifier"),o.optLong("openingBalance"));map[o.getLong("id")]=id}
        val r=root.optJSONArray("reviewSms")?:JSONArray();for(i in 0 until r.length()){val o=r.getJSONObject(i);if(!db.rawExists(o.optString("fingerprint")))db.addRaw(o.optString("sender").takeIf{it!="null"},o.optString("body"),o.optLong("receivedAt"),o.optString("fingerprint"))}
        val t=root.optJSONArray("transactions")?:JSONArray();for(i in 0 until t.length()){val o=t.getJSONObject(i);val aid=map[o.getLong("accountId")]?:continue;val dir=o.getString("direction");val amount=o.getLong("amount");val date=o.getString("dateText");if(!db.transactionExists(aid,dir,amount,date))db.addTransaction(Transaction(0,aid,dir,o.optString("nature","NORMAL"),amount,date,if(o.isNull("smsBalance"))null else o.optLong("smsBalance"),null,o.optString("category").takeIf{it!="null"},o.optString("title").takeIf{it!="null"},o.optString("channel").takeIf{it!="null"},o.optString("description").takeIf{it!="null"},o.optString("tracking").takeIf{it!="null"},null,"POSTED",System.currentTimeMillis()))}
    }

}
