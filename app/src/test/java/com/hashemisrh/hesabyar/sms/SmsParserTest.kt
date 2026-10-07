package com.hashemisrh.hesabyar.sms

import com.hashemisrh.hesabyar.data.Account
import org.junit.Assert.*
import org.junit.Test

class SmsParserTest {
    private val accounts=listOf(
        Account(1,1,"گردشگری","132.8501.3202.1",0,null,true,"TOURISM"),
        Account(2,2,"بلو","",0,null,true,"BLUE"),
        Account(3,3,"پاسارگاد","256.800.3585.1",0,null,true,"PASARGAD"),
        Account(4,4,"ملی","0236728207004",0,null,true,"MELLI"),
        Account(5,5,"تجارت","0279025419489",0,null,true,"TEJARAT"),
        Account(6,6,"ملت","4230512174",0,null,true,"MELLAT"),
        Account(7,7,"صادرات","13008",0,null,true,"SADERAT")
    )
    @Test fun tourismDeposit(){val r=SmsParser.parse("*بانك گردشگری*\nواريز به: 132.8501.3202.1\nمبلغ: 64,298,056 ريال\n05/07/08_14:46\nموجودي: 10,164,419,396 ريال",accounts);assertTrue(r.matched);assertEquals(1,r.accountId);assertEquals("DEPOSIT",r.direction);assertEquals(64298056,r.amount)}
    @Test fun tourismReversal(){val r=SmsParser.parse("*بانک گردشگری*\nسند برگشت عملیات\nواريز به: 132.8501.3202.1\nمبلغ: 116,000 ريال\n05/07/10_11:35\nموجودي: 10,205,975,004 ريال",accounts);assertEquals("REVERSAL",r.nature)}
    @Test fun blueSeparateDateAndTime(){val r=SmsParser.parse("بلو\nواریز پول\n130,000,000 ریال به حساب شما نشست.\nموجودی: 137,586,460 ریال\n۱:۴۴\n۱۴۰۵.۰۶.۰۳",accounts);assertTrue(r.matched);assertEquals(2,r.accountId);assertEquals("1405/06/03 01:44",r.dateText)}
    @Test fun pasargadPlusBeforeAmount(){val r=SmsParser.parse("256.800.3585.1\n+1,000,000\n07/14_18:42\nمانده: 5,151,078",accounts);assertEquals(1000000,r.amount)}
    @Test fun tejaratWithdrawal(){val r=SmsParser.parse("*بانک تجارت*\nحساب: 0279025419489\nبرداشت: 47,075,373 ریال\nاز طريق: شعبه ديجيتال\nمانده: 2,589,176 ریال\n1405/05/26\n07:21",accounts);assertEquals(5,r.accountId);assertEquals("WITHDRAWAL",r.direction)}
    @Test fun mellatWithdrawal(){val r=SmsParser.parse("حساب4230512174\nبرداشت2,970,000\nمانده1,609,160\n05/07/14-14:29",accounts);assertTrue(r.matched);assertEquals(6,r.accountId)}
    @Test fun saderatMinusAfterAmount(){val r=SmsParser.parse("پايانه فروش: 4,634,450-\nحساب:13008\nمانده:11,191,304\n0713 - 17:40",accounts);assertTrue(r.matched);assertEquals(4634450,r.amount);assertEquals(7,r.accountId)}
    @Test fun melliWarningWithoutDateGoesReview(){val r=SmsParser.parse("بانک ملي (هشدار)\nشناسه تاييد برداشت از حساب شما:89727\nمبلغ: 51,680,295",accounts);assertFalse(r.matched);assertEquals("WITHDRAWAL",r.direction)}
}
