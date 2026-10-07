package com.hashemisrh.hesabyar.data

data class Bank(val id: Long, val name: String, val code: String)
data class Account(val id: Long, val bankId: Long, val name: String, val identifier: String, val openingBalance: Long, val correctedBalance: Long?, val active: Boolean, val bankCode: String = "")
data class Category(val id: Long, val name: String, val kind: String, val parentId: Long?)
data class Transaction(
    val id: Long, val accountId: Long, val direction: String, val nature: String, val amount: Long,
    val dateText: String, val smsBalance: Long?, val calculatedBalance: Long?, val category: String?,
    val title: String?, val channel: String?, val description: String?, val tracking: String?,
    val rawSmsId: Long?, val status: String, val createdAt: Long
)
data class RawSms(val id: Long, val sender: String?, val body: String, val receivedAt: Long, val fingerprint: String, val status: String)
data class ParseResult(
    val matched: Boolean, val accountId: Long?, val direction: String?, val nature: String = "NORMAL",
    val amount: Long? = null, val dateText: String? = null, val smsBalance: Long? = null,
    val title: String? = null, val channel: String? = null, val description: String? = null,
    val tracking: String? = null, val reason: String? = null
)
