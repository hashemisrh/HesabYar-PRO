package com.hashemisrh.hesabyar.sms

import com.hashemisrh.hesabyar.data.*
import java.util.Locale
import java.util.regex.Pattern

object SmsParser {
    private fun norm(s:String):String = s.replace('۰','0').replace('۱','1').replace('۲','2').replace('۳','3').replace('۴','4').replace('۵','5').replace('۶','6').replace('۷','7').replace('۸','8').replace('۹','9').replace('٠','0').replace('١','1').replace('٢','2').replace('٣','3').replace('٤','4').replace('٥','5').replace('٦','6').replace('٧','7').replace('٨','8').replace('٩','9').replace('٬',',').replace('،',',').replace('ي','ی').replace('ى','ی').replace('ك','ک').trim()
    private fun num(s:String?):Long? = s?.replace(",","")?.replace(" ","")?.filter{it.isDigit()}?.toLongOrNull()
    private fun first(pattern:String,text:String,flags:Int=Pattern.MULTILINE):String? = Pattern.compile(pattern,flags).matcher(text).let{if(it.find())it.group(1) else null}
    private fun date(text:String):String? {
        val t=norm(text)
        val tm=Pattern.compile("(\\d{1,2}):(\\d{2})[^0-9]+(14\\d{2})[./-](\\d{1,2})[./-](\\d{1,2})").matcher(t);if(tm.find())return "%04d/%02d/%02d %02d:%02d".format(Locale.US,tm.group(3).toInt(),tm.group(4).toInt(),tm.group(5).toInt(),tm.group(1).toInt(),tm.group(2).toInt())

        val f=first("(14\\d{2})[./-](\\d{1,2})[./-](\\d{1,2})[^0-9]+(\\d{1,2}):(\\d{2})",t)
        if(f!=null){val m=Pattern.compile("(14\\d{2})[./-](\\d{1,2})[./-](\\d{1,2})[^0-9]+(\\d{1,2}):(\\d{2})").matcher(t);if(m.find())return "%04d/%02d/%02d %02d:%02d".format(Locale.US,m.group(1).toInt(),m.group(2).toInt(),m.group(3).toInt(),m.group(4).toInt(),m.group(5).toInt())}
        val patterns=listOf("(\\d{2})/(\\d{2})/(\\d{2})[_-](\\d{1,2}):(\\d{2})","(\\d{2})\\.(\\d{2})\\.(\\d{2})[^0-9]+(\\d{1,2}):(\\d{2})","(\\d{2})/(\\d{2})[_-](\\d{1,2}):(\\d{2})","(\\d{2})(\\d{2})\\s*-?\\s*(\\d{1,2}):(\\d{2})")
        for(p in patterns){val m=Pattern.compile(p).matcher(t);if(m.find()){val y=if(m.groupCount()==5)m.group(1).toInt() else 0; val mo=if(m.groupCount()==5)m.group(2).toInt() else m.group(1).toInt(); val d=if(m.groupCount()==5)m.group(3).toInt() else m.group(2).toInt(); val h=if(m.groupCount()==5)m.group(4).toInt() else m.group(3).toInt(); val mi=if(m.groupCount()==5)m.group(5).toInt() else m.group(4).toInt(); val year=if(y>0)if(y<100)1400+y else y else Jalali.nowYear(); return "%04d/%02d/%02d %02d:%02d".format(Locale.US,year,mo,d,h,mi)}}
        val dm=Pattern.compile("(14\\d{2})[./-](\\d{1,2})[./-](\\d{1,2})").matcher(t);val hm=Pattern.compile("(?m)^\\s*(\\d{1,2}):(\\d{2})\\s*$").matcher(t);if(dm.find()&&hm.find())return "%04d/%02d/%02d %02d:%02d".format(Locale.US,dm.group(1).toInt(),dm.group(2).toInt(),dm.group(3).toInt(),hm.group(1).toInt(),hm.group(2).toInt())
        return null
    }
    private fun amount(text:String):Long? = num(first("(?:مبلغ|برداشت|واریز|انتقال)\\s*:?\\s*([0-9,]+)",text)) ?: num(first("(?m)^\\s*[+-]\\s*([0-9,]+)",text)) ?: num(first("([0-9,]+)\\s*[+-]",text)) ?: num(first("([0-9,]+)\\s*ریال",text))
    private fun balance(text:String):Long? = num(first("(?:موجودی|مانده)\\s*:?\\s*([0-9,]+)",text))
    private fun account(text:String):String? = first("(?:شماره حساب|حساب|حساب:|واریز به:|برداشت از:)\\s*#?([0-9.]+)",text) ?: first("(?m)^(?!\\s*14[0-9]{2}\\.[0-9]{1,2}\\.[0-9]{1,2}\\s*$)\\s*([0-9]{3,}(?:\\.[0-9]+)*)\\s*$",text)
    private fun title(text:String):String? = when { text.contains("برداشت_قسط") -> "برداشت قسط"; text.contains("پرداخت قبض") -> "پرداخت قبض"; text.contains("شارژ شدی") -> "شارژ"; text.contains("انتقال پل") -> "انتقال پل"; else -> null }

    fun parse(raw:String, accounts:List<Account>):ParseResult {
        val t=norm(raw)
        val tourism=t.contains("بانك گردشگری")||t.contains("بانک گردشگری")
        val blue=t.startsWith("بلو")
        val tejarat=t.contains("بانک تجارت")
        val melli=t.contains("بانک ملّی")||t.contains("بانک ملی")||t.contains("بانك ملي")
        val acc=account(t)
        val detectedCode=when { tourism->"TOURISM"; blue->"BLUE"; tejarat->"TEJARAT"; melli->"MELLI"; else->"" }
        val candidates=if(acc!=null)accounts.filter{it.identifier.replace(" ","").removePrefix("#")==acc.replace(" ","").removePrefix("#")} else if(detectedCode.isNotBlank()) accounts.filter{it.bankCode==detectedCode} else accounts
        if(candidates.size>1 && !blue && !tourism && !tejarat && !melli)return ParseResult(false,null,null,reason="چند حساب ممکن است")
        val chosen=candidates.singleOrNull()
        val direction=when{
            t.contains("تایید برداشت از حساب") -> "WITHDRAWAL"
            tourism && t.contains("واريز") -> "DEPOSIT"
            tourism && t.contains("برداشت") -> "WITHDRAWAL"
            blue && (t.contains("نشست")||t.contains("واریز پول")) -> "DEPOSIT"
            blue && t.contains("پرید") -> "WITHDRAWAL"
            t.contains("برداشت") -> "WITHDRAWAL"
            t.contains("واریز") -> "DEPOSIT"
            Pattern.compile("[0-9,]+\\s*\\+").matcher(t).find() -> "DEPOSIT"
            Pattern.compile("[0-9,]+\\s*-").matcher(t).find() -> "WITHDRAWAL"
            else -> null
        }
        val amt=amount(t); val bal=balance(t); val dt=date(t)
        if(direction==null||amt==null||dt==null)return ParseResult(false,chosen?.id,direction,amount=amt,dateText=dt,smsBalance=bal,reason="ساختار SMS کامل تشخیص داده نشد")
        val nature=if(t.contains("سند برگشت عملیات"))"REVERSAL" else if(t.contains("انتقال پل")||t.contains("انتقال بین حساب"))"TRANSFER" else "NORMAL"
        val channel=first("از طريق\\s*:\\s*([^\\n]+)",t) ?: first("از طریق\\s*:\\s*([^\\n]+)",t)
        val tracking=first("(?:شماره پیگیری|پیگیری)\\s*:?\\s*([^\\n]+)",t)
        val desc=first("توضیحات\\s*:\\s*([^\\n]+)",t)
        val reason=title(t)
        return ParseResult(true,chosen?.id,direction,nature,amt,dt,bal,reason=reason,title=reason,channel=channel,description=desc,tracking=tracking)
    }
}

object Jalali { fun nowYear():Int { val y=java.util.Calendar.getInstance().get(java.util.Calendar.YEAR); return y-621 } }
