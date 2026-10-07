package com.hashemisrh.hesabyar

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hashemisrh.hesabyar.ui.theme.HesabYarTheme

class MainActivity : ComponentActivity() {

    private val smsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestRequiredPermissions()

        setContent {
            HesabYarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Dashboard()
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        smsPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }
}

@Composable
private fun Dashboard() {
    val teal = Color(0xFF0F5C5A)
    val dark = Color(0xFF102A2A)
    val ivory = Color(0xFFF5F3EE)
    val gold = Color(0xFFC7A86B)
    val green = Color(0xFF2E8B75)
    val red = Color(0xFFB85C5C)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ivory)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Menu, contentDescription = "منو", tint = dark)
            }
            Text(
                text = "حساب‌یار",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = dark,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {}) {
                Icon(Icons.Default.NotificationsNone, contentDescription = "اعلان‌ها", tint = dark)
            }
        }

        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = teal),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = gold,
                        modifier = Modifier.size(34.dp)
                    )
                    Text(
                        text = "موجودی کل",
                        color = ivory,
                        fontSize = 15.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "۵۶٬۷۲۳٬۹۰۰",
                    modifier = Modifier.fillMaxWidth(),
                    color = ivory,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
                Text(
                    text = "ریال",
                    modifier = Modifier.fillMaxWidth(),
                    color = gold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "دریافتی امروز",
                amount = "۱۲٬۴۵۰٬۰۰۰",
                icon = Icons.Default.ArrowUpward,
                accent = green
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "پرداختی امروز",
                amount = "۵٬۷۳۰٬۰۰۰",
                icon = Icons.Default.ArrowDownward,
                accent = red
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "تراکنش‌های اخیر",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            color = dark,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        TransactionRow("واریز", "۱۰٬۰۰۰٬۰۰۰", "انتقال بانکی", green)
        TransactionRow("برداشت", "۲٬۹۷۰٬۰۰۰", "خرید از کارت", red)
        TransactionRow("واریز", "۲۰٬۰۰۰٬۰۰۰", "حقوق", green)
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    title: String,
    amount: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.End
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color(0xFF334444), fontSize = 13.sp)
            Text(amount, color = Color(0xFF102A2A), fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("ریال", color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun TransactionRow(title: String, amount: String, subtitle: String, accent: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(title, color = Color(0xFF102A2A), fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Text(amount, color = accent, fontWeight = FontWeight.Bold)
        }
    }
}
