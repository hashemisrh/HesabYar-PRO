package com.hashemisrh.hesabyar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hashemisrh.hesabyar.data.*
import com.hashemisrh.hesabyar.security.*
import com.hashemisrh.hesabyar.ui.theme.HesabYarTheme
import java.text.DecimalFormat

class MainActivity : FragmentActivity() {
    private lateinit var db: AppDb
    private lateinit var security: SecurityStore
    private var exportPin=""
    private val createBackup=registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")){uri->if(uri!=null)contentResolver.openOutputStream(uri)?.use{it.write(BackupManager.export(this,exportPin))}}
    private val openBackup=registerForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)restore(uri)}
    private val smsPermission=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ }

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState);db=AppDb(this);security=SecurityStore(this);ToastState.context=this
        if(android.os.Build.VERSION.SDK_INT>=33) smsPermission.launch(arrayOf(Manifest.permission.RECEIVE_SMS,Manifest.permission.POST_NOTIFICATIONS)) else smsPermission.launch(arrayOf(Manifest.permission.RECEIVE_SMS))
        setContent{HesabYarTheme{HesabYarApp()}}
    }
    private fun restore(uri:Uri){try{val pin=exportPin;val text=contentResolver.openInputStream(uri)?.use{BackupManager.decrypt(it.readBytes(),pin)} ?: return;BackupManager.restore(this,text);ToastState.show("بازیابی با موفقیت انجام شد.")}catch(_:Throwable){ToastState.show("رمز پشتیبان نادرست است یا فایل معتبر نیست.")}}
    private fun biometric(onSuccess:()->Unit){val executor=ContextCompat.getMainExecutor(this);val prompt=BiometricPrompt(this,executor,object:BiometricPrompt.AuthenticationCallback(){override fun onAuthenticationSucceeded(r:BiometricPrompt.AuthenticationResult){onSuccess()}});val info=BiometricPrompt.PromptInfo.Builder().setTitle("ورود به حساب‌یار").setSubtitle("احراز هویت با اثر انگشت یا قفل دستگاه").setNegativeButtonText("لغو").build();prompt.authenticate(info)}

    @Composable fun HesabYarApp(){
        var refresh by remember{mutableIntStateOf(0)};var unlocked by remember{mutableStateOf(!security.hasPin())};var setup by remember{mutableStateOf(!security.hasPin())}
        if(setup){PinSetup{security.setPin(it);setup=false;unlocked=true;refresh++};return}
        if(!unlocked){LockScreen(canBio=security.biometricEnabled(),onPin={if(security.verify(it)){unlocked=true;refresh++}else ToastState.show("PIN نادرست است")},onBio={biometric{unlocked=true;refresh++}});return}
        MainShell(refresh){refresh++}
    }

    @Composable fun PinSetup(done:(String)->Unit){var pin by remember{mutableStateOf("")};var confirm by remember{mutableStateOf("")};Column(Modifier.fillMaxSize().background(Teal).padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text("حساب‌یار",fontSize=34.sp,fontWeight=FontWeight.Bold,color=Ivory);Text("امنیت اطلاعات مالی شما",color=Gold,modifier=Modifier.padding(bottom=30.dp));OutlinedTextField(pin,{if(it.length<=6&&it.all(Char::isDigit))pin=it},label={Text("PIN چهار تا شش رقمی")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword));Spacer(Modifier.height(12.dp));OutlinedTextField(confirm,{if(it.length<=6&&it.all(Char::isDigit))confirm=it},label={Text("تکرار PIN")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword));Button(onClick={if(pin.length in 4..6&&pin==confirm)done(pin)else ToastState.show("PIN را صحیح و یکسان وارد کنید")},modifier=Modifier.fillMaxWidth().padding(top=20.dp),colors=ButtonDefaults.buttonColors(containerColor=Gold,contentColor=Teal)){Text("شروع استفاده")}}
    }

    @Composable fun LockScreen(canBio:Boolean,onPin:(String)->Unit,onBio:()->Unit){var pin by remember{mutableStateOf("")};Column(Modifier.fillMaxSize().background(Teal).padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text("حساب‌یار",fontSize=34.sp,fontWeight=FontWeight.Bold,color=Ivory);Text("برای ورود PIN را وارد کنید",color=Gold,modifier=Modifier.padding(12.dp));OutlinedTextField(pin,{if(it.length<=6)pin=it},singleLine=true,label={Text("PIN")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword));Button(onClick={onPin(pin)},modifier=Modifier.fillMaxWidth().padding(top=18.dp)){Text("ورود")};if(canBio){TextButton(onClick=onBio){Icon(Icons.Default.Fingerprint,null);Spacer(Modifier.width(8.dp));Text("ورود با اثر انگشت")}}}}

    @Composable
    fun MainShell(refresh: Int, onRefresh: () -> Unit) {
        var tab by remember { mutableIntStateOf(0) }
        var showAdd by remember { mutableStateOf(false) }
        val tabs = listOf("خانه", "تراکنش‌ها", "حساب‌ها", "گزارش", "تنظیمات")

        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = TealDark) {
                    tabs.forEachIndexed { i, title ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = {
                                val icon = when (i) {
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.List
                                    2 -> Icons.Default.AccountBalance
                                    3 -> Icons.Default.BarChart
                                    else -> Icons.Default.Settings
                                }
                                Icon(icon, contentDescription = title)
                            },
                            label = { Text(title) }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (tab == 2) {
                    FloatingActionButton(
                        onClick = { showAdd = true },
                        containerColor = Gold,
                        contentColor = Teal
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "افزودن حساب")
                    }
                }
            }
        ) { paddingValues ->
            CompositionLocalProvider(
                LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
            ) {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    when (tab) {
                        0 -> HomeScreen(refresh)
                        1 -> TransactionsScreen(refresh)
                        2 -> AccountsScreen(refresh)
                        3 -> ReportsScreen(refresh)
                        4 -> SettingsScreen(onRefresh)
                    }
                }
            }
        }

        if (showAdd) {
            AddAccountDialog(
                close = { showAdd = false },
                done = {
                    showAdd = false
                    onRefresh()
                }
            )
        }
    }

    @Composable fun HomeScreen(refresh:Int){val accounts=db.accounts();val balance=accounts.sumOf{db.currentBalance(it.id)};val c=db.counts();Column(Modifier.fillMaxSize().background(Ivory).padding(16.dp)){Text("سلام",fontSize=18.sp,color=Teal);Text("مدیریت هوشمند پول شما",fontSize=14.sp,color=Color.Gray);Spacer(Modifier.height(14.dp));Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(containerColor=Teal)){Column(Modifier.padding(22.dp)){Text("موجودی کل",color=Ivory);Text(money(balance),fontSize=30.sp,fontWeight=FontWeight.Bold,color=Ivory);Text("ریال",color=Gold);}};Spacer(Modifier.height(12.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){SummaryCard("دریافتی",c.second,Emerald,Modifier.weight(1f));SummaryCard("پرداختی",c.third,MutedRed,Modifier.weight(1f))};Spacer(Modifier.height(18.dp));Text("تراکنش‌های اخیر",fontWeight=FontWeight.Bold,fontSize=18.sp,color=TealDark);Spacer(Modifier.height(6.dp));LazyColumn { items(items = db.recent()) { t: Transaction -> TransactionRow(t) } }}}
    @Composable fun SummaryCard(title:String,value:Long,color:Color,modifier:Modifier){Card(modifier,shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=color.copy(alpha=.12f))){Column(Modifier.padding(14.dp)){Text(title,color=color,fontWeight=FontWeight.Bold);Text(money(value),fontSize=17.sp,fontWeight=FontWeight.Bold,color=TealDark)}}}
    @Composable fun TransactionRow(t:Transaction){Card(Modifier.fillMaxWidth().padding(vertical=4.dp),shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(if(t.direction=="DEPOSIT")Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,null,tint=if(t.direction=="DEPOSIT")Emerald else MutedRed);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(t.title?:if(t.direction=="DEPOSIT")"واریز" else "برداشت",fontWeight=FontWeight.Bold);Text(t.dateText,fontSize=11.sp,color=Color.Gray)};Text(money(t.amount)+" ریال",fontWeight=FontWeight.Bold,color=if(t.direction=="DEPOSIT")Emerald else MutedRed)}}}

    @Composable fun TransactionsScreen(refresh:Int){var selected by remember{mutableStateOf<Transaction?>(null)};Column(Modifier.fillMaxSize().background(Ivory).padding(16.dp)){Text("تراکنش‌ها",fontSize=24.sp,fontWeight=FontWeight.Bold,color=Teal);Text("برای دسته‌بندی یک تراکنش، روی آن بزنید.",fontSize=12.sp,color=Color.Gray);LazyColumn(Modifier.padding(top=10.dp)) { items(items = db.recent(200)) { t: Transaction -> Box(Modifier.clickable { selected = t }) { TransactionRow(t) } } }};selected?.let{CategoryDialog(it){selected=null}}}
    @Composable fun AccountsScreen(refresh:Int){var selected by remember{mutableStateOf<Account?>(null)};Column(Modifier.fillMaxSize().background(Ivory).padding(16.dp)){Text("حساب‌ها",fontSize=24.sp,fontWeight=FontWeight.Bold,color=Teal);Text("برای اصلاح موجودی روی حساب بزنید.",fontSize=12.sp,color=Color.Gray);LazyColumn(Modifier.padding(top=10.dp)) { items(items = db.accounts()) { a: Account -> val bank=db.banks().firstOrNull { it.id==a.bankId };Card(Modifier.fillMaxWidth().padding(vertical=5.dp).clickable{selected=a},shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.padding(16.dp)){Text(a.name,fontWeight=FontWeight.Bold,fontSize=18.sp);Text(bank?.name?:"بانک",color=Color.Gray);Text(if(a.identifier.isBlank())"حساب بدون شماره قابل تشخیص" else a.identifier,fontSize=12.sp,color=Color.Gray);Text(money(db.currentBalance(a.id))+" ریال",fontSize=20.sp,fontWeight=FontWeight.Bold,color=Teal)}}}}};selected?.let{BalanceDialog(it){selected=null}}}
    @Composable fun ReportsScreen(refresh:Int){
        val c=db.counts();val tx=db.allTransactions();val byAccount=tx.groupBy{it.accountId}.map{(id,list)->db.account(id)?.name.orEmpty() to list.sumOf{if(it.direction=="DEPOSIT")it.amount else -it.amount}}.sortedByDescending{it.second};val byCategory=tx.filter{!it.category.isNullOrBlank()}.groupBy{it.category!!}.map{it.key to it.value.sumOf{t->if(t.direction=="DEPOSIT")t.amount else -t.amount}}.sortedByDescending{it.second};val byDate=tx.groupBy{it.dateText.take(10)}.map{it.key to it.value.sumOf{t->if(t.direction=="DEPOSIT")t.amount else -t.amount}}.sortedByDescending{it.first}.take(20)
        LazyColumn(Modifier.fillMaxSize().background(Ivory).padding(16.dp)){item{Text("گزارش مالی",fontSize=24.sp,fontWeight=FontWeight.Bold,color=Teal);SummaryCard("کل تراکنش‌ها",c.first,Gold,Modifier.fillMaxWidth().padding(top=14.dp));SummaryCard("کل دریافتی",c.second,Emerald,Modifier.fillMaxWidth().padding(top=8.dp));SummaryCard("کل پرداختی",c.third,MutedRed,Modifier.fillMaxWidth().padding(top=8.dp));Text("بر اساس حساب",fontWeight=FontWeight.Bold,fontSize=18.sp,color=Teal,modifier=Modifier.padding(top=18.dp));byAccount.forEach{(name,v)->ReportLine(name,v)};Text("بر اساس دسته",fontWeight=FontWeight.Bold,fontSize=18.sp,color=Teal,modifier=Modifier.padding(top=18.dp));byCategory.forEach{(name,v)->ReportLine(name,v)};Text("بر اساس تاریخ",fontWeight=FontWeight.Bold,fontSize=18.sp,color=Teal,modifier=Modifier.padding(top=18.dp));byDate.forEach{(name,v)->ReportLine(name,v)};Text("انتقال بین حساب‌های خودتان از درآمد و هزینه واقعی جدا نگه داشته می‌شود.",color=Color.Gray,fontSize=13.sp,modifier=Modifier.padding(top=18.dp,bottom=20.dp))}}
    }
    @Composable fun ReportLine(title:String,value:Long){Row(Modifier.fillMaxWidth().padding(vertical=5.dp)){Text(title,Modifier.weight(1f),color=TealDark);Text(money(value)+" ریال",fontWeight=FontWeight.Bold,color=if(value>=0)Emerald else MutedRed)}}

    @Composable fun SettingsScreen(onRefresh:()->Unit){var bio by remember{mutableStateOf(security.biometricEnabled())};var categoryManager by remember{mutableStateOf(false)};Column(Modifier.fillMaxSize().background(Ivory).padding(16.dp)){Text("تنظیمات",fontSize=24.sp,fontWeight=FontWeight.Bold,color=Teal);SettingCard("دریافت خودکار SMS","پردازش SMS در پس‌زمینه و بدون نیاز به باز بودن برنامه");Row(Modifier.fillMaxWidth().padding(vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Text("ورود با اثر انگشت",Modifier.weight(1f));Switch(bio,{bio=it;security.setBiometric(it)})};Button(onClick={exportPin="";PinDialog{p->exportPin=p;createBackup.launch("HesabYar-backup.hyb")}},modifier=Modifier.fillMaxWidth()){Text("پشتیبان رمزگذاری‌شده")};OutlinedButton(onClick={PinDialog{p->exportPin=p;openBackup.launch(arrayOf("application/octet-stream","*/*"))}},modifier=Modifier.fillMaxWidth()){Text("بازیابی پشتیبان")};OutlinedButton(onClick={categoryManager=true},modifier=Modifier.fillMaxWidth()){Text("مدیریت دسته‌بندی‌ها")};Text("SMSهای نیازمند بررسی: ${db.pendingRaw().size}",fontWeight=FontWeight.Bold,color=MutedRed,modifier=Modifier.padding(top=16.dp));Text("الگوهای داخلی فعال: گردشگری، بلو، پاسارگاد، ملی، تجارت، ملت، صادرات",fontSize=12.sp,color=Color.Gray,modifier=Modifier.padding(top=8.dp));Text("نسخه 1.0.0 • بانک‌ها و الگوهای SMS قابل توسعه هستند",color=Color.Gray,fontSize=12.sp,modifier=Modifier.padding(top=20.dp))};if(categoryManager)CategoryManagerDialog{categoryManager=false}}
    @Composable
    fun CategoryManagerDialog(close:()->Unit){
        var adding by remember{mutableStateOf(false)}
        var editingId by remember{mutableStateOf<Long?>(null)}
        var name by remember{mutableStateOf("")}
        AlertDialog(
            onDismissRequest=close,
            title={Text("مدیریت دسته‌بندی‌ها")},
            text={
                Column{
                    db.categories().forEach{c->
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                            Text(c.name,Modifier.weight(1f))
                            TextButton(onClick={name=c.name;editingId=c.id;adding=true}){Text("ویرایش")}
                            TextButton(onClick={db.deleteCategory(c.id)}){Text("حذف")}
                        }
                    }
                    TextButton(onClick={name="";editingId=null;adding=true}){Text("+ افزودن دسته جدید")}
                    if(adding)OutlinedTextField(name,{name=it},label={Text("نام دسته")},modifier=Modifier.fillMaxWidth())
                }
            },
            confirmButton={TextButton(onClick=close){Text("بستن")}}
        )
        if(adding){
            AlertDialog(
                onDismissRequest={adding=false},
                title={Text("ذخیره دسته")},
                text={Text("نام دسته را ثبت کنید.")},
                confirmButton={TextButton(onClick={if(name.isNotBlank()){if(editingId!=null)db.updateCategory(editingId!!,name)else if(db.categories().none{it.name==name})db.addCategory(name,"CUSTOM");adding=false}}){Text("ذخیره")}},
                dismissButton={TextButton(onClick={adding=false}){Text("انصراف")}}
            )
        }
    }

    @Composable fun SettingCard(title:String,desc:String){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=Teal.copy(alpha=.08f))){Column(Modifier.padding(16.dp)){Text(title,fontWeight=FontWeight.Bold,color=Teal);Text(desc,fontSize=12.sp,color=Color.Gray)}}}

    @Composable fun AddAccountDialog(close:()->Unit,done:()->Unit){var bank by remember{mutableStateOf(db.banks().firstOrNull())};var name by remember{mutableStateOf("")};var id by remember{mutableStateOf("")};var bal by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("افزودن حساب")},text={Column{Text("بانک");var expanded by remember{mutableStateOf(false)};Box{OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth()){Text(bank?.name?:"انتخاب بانک")};DropdownMenu(expanded,{expanded=false}){db.banks().forEach{b->DropdownMenuItem(text={Text(b.name)},onClick={bank=b;expanded=false})}}};OutlinedTextField(name,{name=it},label={Text("نام حساب")},modifier=Modifier.fillMaxWidth().padding(top=8.dp));OutlinedTextField(id,{id=it},label={Text("شماره/شناسه حساب؛ برای بلو خالی")},modifier=Modifier.fillMaxWidth().padding(top=8.dp));OutlinedTextField(bal,{if(it.all(Char::isDigit))bal=it},label={Text("موجودی اولیه به ریال")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth().padding(top=8.dp))}},confirmButton={TextButton(onClick={if(bank!=null&&name.isNotBlank()&&bal.isNotBlank()){db.addAccount(bank!!.id,name,id,bal.toLongOrNull()?:0);done() }else ToastState.show("اطلاعات حساب را کامل کنید")}){Text("ثبت")}},dismissButton={TextButton(onClick=close){Text("انصراف")}})}

    @Composable fun PinDialog(done:(String)->Unit){var pin by remember{mutableStateOf("")};AlertDialog(onDismissRequest={},title={Text("PIN امنیتی")},text={OutlinedTextField(pin,{if(it.length<=6)pin=it},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),label={Text("PIN")})},confirmButton={TextButton(onClick={if(security.verify(pin))done(pin)else ToastState.show("PIN نادرست است")}){Text("ادامه")}})}

    private fun jalaliToday():String{val c=android.icu.util.Calendar.getInstance(android.icu.util.ULocale("fa_IR@calendar=persian"));return "%04d/%02d/%02d 00:00".format(java.util.Locale.US,c.get(android.icu.util.Calendar.YEAR),c.get(android.icu.util.Calendar.MONTH)+1,c.get(android.icu.util.Calendar.DAY_OF_MONTH))}
    private fun money(v:Long)=DecimalFormat("#,###").format(v).replace('0','۰').replace('1','۱').replace('2','۲').replace('3','۳').replace('4','۴').replace('5','۵').replace('6','۶').replace('7','۷').replace('8','۸').replace('9','۹')
    companion object{val Teal=Color(0xFF0F5C5A);val TealDark=Color(0xFF102A2A);val Ivory=Color(0xFFF5F3EE);val Gold=Color(0xFFC7A86B);val Emerald=Color(0xFF2E8B75);val MutedRed=Color(0xFFB85C5C)}
}

object ToastState { lateinit var context: android.content.Context; private var message by mutableStateOf(""); fun show(m:String){message=m; android.widget.Toast.makeText(context,m,android.widget.Toast.LENGTH_SHORT).show()} }
