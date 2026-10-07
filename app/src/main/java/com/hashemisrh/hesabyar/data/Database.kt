package com.hashemisrh.hesabyar.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDb(context: Context) : SQLiteOpenHelper(context, "hesabyar.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE banks(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,code TEXT NOT NULL UNIQUE)")
        db.execSQL("CREATE TABLE accounts(id INTEGER PRIMARY KEY AUTOINCREMENT,bank_id INTEGER NOT NULL,name TEXT NOT NULL,identifier TEXT NOT NULL,opening_balance INTEGER NOT NULL DEFAULT 0,corrected_balance INTEGER,active INTEGER NOT NULL DEFAULT 1,UNIQUE(bank_id,identifier))")
        db.execSQL("CREATE TABLE categories(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,kind TEXT NOT NULL,parent_id INTEGER)")
        db.execSQL("CREATE TABLE raw_sms(id INTEGER PRIMARY KEY AUTOINCREMENT,sender TEXT,body TEXT NOT NULL,received_at INTEGER NOT NULL,fingerprint TEXT NOT NULL UNIQUE,status TEXT NOT NULL)")
        db.execSQL("CREATE TABLE transactions(id INTEGER PRIMARY KEY AUTOINCREMENT,account_id INTEGER NOT NULL,direction TEXT NOT NULL,nature TEXT NOT NULL,amount INTEGER NOT NULL,date_text TEXT NOT NULL,sms_balance INTEGER,calculated_balance INTEGER,category TEXT,title TEXT,channel TEXT,description TEXT,tracking TEXT,raw_sms_id INTEGER,status TEXT NOT NULL,created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE patterns(id INTEGER PRIMARY KEY AUTOINCREMENT,bank_code TEXT NOT NULL,name TEXT NOT NULL,direction TEXT,enabled INTEGER NOT NULL DEFAULT 1,version INTEGER NOT NULL DEFAULT 1)")
        seedBanks(db); seedCategories(db); seedPatterns(db)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    private fun seedBanks(db: SQLiteDatabase) {
        listOf("TOURISM|بانک گردشگری","BLUE|بلو","PASARGAD|پاسارگاد","MELLI|بانک ملی","TEJARAT|بانک تجارت","MELLAT|بانک ملت","SADERAT|بانک صادرات").forEach { x -> val p=x.split("|"); val v=ContentValues(); v.put("code",p[0]); v.put("name",p[1]); db.insert("banks",null,v) }
    }
    private fun seedPatterns(db: SQLiteDatabase) {
        val items=listOf("TOURISM|گردشگری|واریز/برداشت با شماره حساب|1","BLUE|بلو|نشست/پرید + تاریخ و موجودی|1","PASARGAD|پاسارگاد|شناسه حساب + علامت +/−|1","MELLI|ملی|مبلغ/مانده/شماره حساب/زمان|1","TEJARAT|تجارت|حساب + برداشت/واریز + مانده|1","MELLAT|ملت|حساب + برداشت/واریز + مانده|1","SADERAT|صادرات|عملیات + علامت +/− + حساب|1")
        items.forEach { x -> val p=x.split("|"); val v=ContentValues();v.put("bank_code",p[0]);v.put("name",p[1]+" — "+p[2]);v.put("enabled",1);v.put("version",1);db.insert("patterns",null,v) }
    }
    private fun seedCategories(db: SQLiteDatabase) {
        val items=listOf("حقوق|INCOME","انتقال بین حساب‌های خودم|TRANSFER","فروش|INCOME","سود و سرمایه‌گذاری|INCOME","اقساط وام|EXPENSE","خرید|EXPENSE","قبض|EXPENSE","شارژ|EXPENSE","کارمزد|EXPENSE","سایر درآمد|INCOME","سایر هزینه|EXPENSE")
        items.forEach { x -> val p=x.split("|"); val v=ContentValues(); v.put("name",p[0]);v.put("kind",p[1]);db.insert("categories",null,v) }
    }

    fun banks(): List<Bank> = readableDatabase.rawQuery("SELECT id,name,code FROM banks ORDER BY name",null).use { c -> buildList { while(c.moveToNext()) add(Bank(c.getLong(0),c.getString(1),c.getString(2))) } }
    fun accounts(): List<Account> = readableDatabase.rawQuery("SELECT a.id,a.bank_id,a.name,a.identifier,a.opening_balance,a.corrected_balance,a.active,b.code FROM accounts a JOIN banks b ON b.id=a.bank_id WHERE a.active=1 ORDER BY a.name",null).use { c -> buildList { while(c.moveToNext()) add(Account(c.getLong(0),c.getLong(1),c.getString(2),c.getString(3),c.getLong(4),if(c.isNull(5)) null else c.getLong(5),c.getInt(6)==1,c.getString(7))) } }
    fun account(id:Long): Account? = accounts().firstOrNull{it.id==id}
    fun addAccount(bankId:Long,name:String,identifier:String,balance:Long):Long { val v=ContentValues();v.put("bank_id",bankId);v.put("name",name);v.put("identifier",identifier);v.put("opening_balance",balance);return writableDatabase.insertOrThrow("accounts",null,v) }
    fun addRaw(sender:String?,body:String,received:Long,fingerprint:String):Long { val v=ContentValues();v.put("sender",sender);v.put("body",body);v.put("received_at",received);v.put("fingerprint",fingerprint);v.put("status","RECEIVED");return writableDatabase.insertWithOnConflict("raw_sms",null,v,SQLiteDatabase.CONFLICT_IGNORE) }
    fun rawExists(fingerprint:String)=readableDatabase.rawQuery("SELECT 1 FROM raw_sms WHERE fingerprint=? LIMIT 1",arrayOf(fingerprint)).use{it.moveToFirst()}
    fun setRawStatus(id:Long,status:String){val v=ContentValues();v.put("status",status);writableDatabase.update("raw_sms",v,"id=?",arrayOf(id.toString()))}
    fun addTransaction(t:Transaction):Long { val v=ContentValues();v.put("account_id",t.accountId);v.put("direction",t.direction);v.put("nature",t.nature);v.put("amount",t.amount);v.put("date_text",t.dateText);if(t.smsBalance!=null)v.put("sms_balance",t.smsBalance);if(t.calculatedBalance!=null)v.put("calculated_balance",t.calculatedBalance);v.put("category",t.category);v.put("title",t.title);v.put("channel",t.channel);v.put("description",t.description);v.put("tracking",t.tracking);if(t.rawSmsId!=null)v.put("raw_sms_id",t.rawSmsId);v.put("status",t.status);v.put("created_at",t.createdAt);return writableDatabase.insertOrThrow("transactions",null,v) }
    fun recent(limit:Int=20):List<Transaction> = readableDatabase.rawQuery("SELECT id,account_id,direction,nature,amount,date_text,sms_balance,calculated_balance,category,title,channel,description,tracking,raw_sms_id,status,created_at FROM transactions ORDER BY id DESC LIMIT ?",arrayOf(limit.toString())).use{c->buildList{while(c.moveToNext())add(row(c))}}
    fun allTransactions():List<Transaction> = readableDatabase.rawQuery("SELECT id,account_id,direction,nature,amount,date_text,sms_balance,calculated_balance,category,title,channel,description,tracking,raw_sms_id,status,created_at FROM transactions ORDER BY id",null).use{c->buildList{while(c.moveToNext())add(row(c))}}
    private fun row(c:android.database.Cursor)=Transaction(c.getLong(0),c.getLong(1),c.getString(2),c.getString(3),c.getLong(4),c.getString(5),if(c.isNull(6))null else c.getLong(6),if(c.isNull(7))null else c.getLong(7),c.getString(8),c.getString(9),c.getString(10),c.getString(11),c.getString(12),if(c.isNull(13))null else c.getLong(13),c.getString(14),c.getLong(15))
    fun counts():Triple<Long,Long,Long>{val total=readableDatabase.rawQuery("SELECT COUNT(*) FROM transactions",null).use{it.moveToFirst();it.getInt(0).toLong()};val inc=readableDatabase.rawQuery("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE direction='DEPOSIT' AND nature NOT IN ('TRANSFER','ADJUSTMENT')",null).use{it.moveToFirst();it.getLong(0)};val exp=readableDatabase.rawQuery("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE direction='WITHDRAWAL' AND nature NOT IN ('TRANSFER','ADJUSTMENT')",null).use{it.moveToFirst();it.getLong(0)};return Triple(total,inc,exp)}
    fun currentBalance(accountId:Long):Long {
        val a=account(accountId) ?: return 0
        val q=readableDatabase.rawQuery("SELECT COALESCE(SUM(CASE WHEN direction='DEPOSIT' THEN amount ELSE -amount END),0) FROM transactions WHERE account_id=?",arrayOf(accountId.toString()))
        q.use{it.moveToFirst();return a.openingBalance+it.getLong(0)}
    }
    fun pendingRaw():List<RawSms> = readableDatabase.rawQuery("SELECT id,sender,body,received_at,fingerprint,status FROM raw_sms WHERE status='REVIEW' ORDER BY id DESC",null).use{c->buildList{while(c.moveToNext())add(RawSms(c.getLong(0),c.getString(1),c.getString(2),c.getLong(3),c.getString(4),c.getString(5)))}}
    fun categories():List<Category> = readableDatabase.rawQuery("SELECT id,name,kind,parent_id FROM categories ORDER BY kind,name",null).use{c->buildList{while(c.moveToNext())add(Category(c.getLong(0),c.getString(1),c.getString(2),if(c.isNull(3))null else c.getLong(3)))}}
    fun insertManualTransaction(accountId:Long,direction:String,nature:String,amount:Long,dateText:String,category:String?,note:String?):Long = addTransaction(Transaction(0,accountId,direction,nature,amount,dateText,null,null,category,note,null,null,null,null,"POSTED",System.currentTimeMillis()))

    fun allRaw():List<RawSms> = readableDatabase.rawQuery("SELECT id,sender,body,received_at,fingerprint,status FROM raw_sms ORDER BY id",null).use{c->buildList{while(c.moveToNext())add(RawSms(c.getLong(0),c.getString(1),c.getString(2),c.getLong(3),c.getString(4),c.getString(5)))}}
    fun transactionExists(accountId:Long,direction:String,amount:Long,dateText:String):Boolean = readableDatabase.rawQuery("SELECT 1 FROM transactions WHERE account_id=? AND direction=? AND amount=? AND date_text=? LIMIT 1",arrayOf(accountId.toString(),direction,amount.toString(),dateText)).use{it.moveToFirst()}

    fun setTransactionCategory(id:Long,category:String){val v=ContentValues();v.put("category",category);writableDatabase.update("transactions",v,"id=?",arrayOf(id.toString()))}
    fun addAdjustment(accountId:Long,amountDelta:Long,dateText:String,note:String){val direction=if(amountDelta>=0)"DEPOSIT" else "WITHDRAWAL";addTransaction(Transaction(0,accountId,direction,"ADJUSTMENT",kotlin.math.abs(amountDelta),dateText,null,null,"اصلاح موجودی",note,null,null,null,null,"POSTED",System.currentTimeMillis()))}

    fun addCategory(name:String,kind:String):Long{val v=ContentValues();v.put("name",name);v.put("kind",kind);return writableDatabase.insert("categories",null,v)}
    fun updateCategory(id:Long,name:String){val old=readableDatabase.rawQuery("SELECT name FROM categories WHERE id=?",arrayOf(id.toString()));var oldName="";old.use{if(it.moveToFirst())oldName=it.getString(0)};val v=ContentValues();v.put("name",name);writableDatabase.update("categories",v,"id=?",arrayOf(id.toString()));if(oldName.isNotBlank()){val tv=ContentValues();tv.put("category",name);writableDatabase.update("transactions",tv,"category=?",arrayOf(oldName))}}
    fun deleteCategory(id:Long){writableDatabase.delete("categories","id=?",arrayOf(id.toString()))}

}
