package az.diamante.ioslauncher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * iOS 18 Vidcetlər Zolağı (Saat, Batareya və Premium Nişanlar ilə)
 */
@Composable
fun IOSWidgetsRow(
    activeBadge: PremiumBadge,
    onBatteryWidgetClick: () -> Unit,
    onChargingDetected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var batteryLevel by remember { mutableStateOf(getBatteryPercentage(context)) }
    var isCharging by remember { mutableStateOf(false) }

    var currentTime by remember { mutableStateOf(getCurrentTimeString()) }
    var currentDate by remember { mutableStateOf(getCurrentDateString()) }

    // Şarj bağlantısını və faiz dəyişikliyini dinləyirik
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        isCharging = true
                        onChargingDetected()
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        isCharging = false
                    }
                    Intent.ACTION_BATTERY_CHANGED -> {
                        batteryLevel = getBatteryPercentage(context)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        context.registerReceiver(receiver, filter)

        onDispose {
            try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = getCurrentTimeString()
            currentDate = getCurrentDateString()
            batteryLevel = getBatteryPercentage(context)
            kotlinx.coroutines.delay(10000)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. iOS Saat və Tarix Vidceti
        Box(
            modifier = Modifier
                .weight(1f)
                .height(138.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.22f))
                .border(0.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFFFF3B30),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentDate.uppercase(),
                        color = Color(0xFFFF3B30),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = currentTime,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Gözəl günlər arzusu ilə",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }
        }

        // 2. iOS 18 Premium Batareya Vidceti (Xüsusi Nişanlar və Brend Mətnləri)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(138.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.22f))
                .border(0.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .clickable(onClick = onBatteryWidgetClick)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Üst Başlıq
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PhoneIphone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Batareya",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "%$batteryLevel",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Mərkəzi Dairə və Xüsusi Nişan
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { batteryLevel / 100f },
                        modifier = Modifier.size(54.dp),
                        color = if (batteryLevel > 20) Color(0xFF34C759) else Color(0xFFFF3B30),
                        strokeWidth = 5.5.dp,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )

                    // Əgər nişan bayraq və ya emojidirsə dairənin mərkəzində böyük göstərilir
                    if (!activeBadge.isTextBadge) {
                        Text(
                            text = activeBadge.displayContent,
                            fontSize = if (activeBadge.id == "az_flag") 22.sp else 18.sp
                        )
                    } else {
                        // Əgər mətndirsə (centraldev.az, İbrahim Həsəni, Tank və s.) mərkəzdə parlaq şimşək
                        Text("⚡", fontSize = 18.sp)
                    }
                }

                // Alt Kapsul: Fərdi Brend Yazısı (centraldev.az / İbrahim Həsəni / TANK / SİLAH / Bayraq)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(activeBadge.accentColor.copy(alpha = 0.25f))
                        .border(0.5.dp, activeBadge.accentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(vertical = 3.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (activeBadge.isTextBadge) activeBadge.displayContent else activeBadge.name,
                        color = activeBadge.accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun getBatteryPercentage(context: Context): Int {
    val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus = context.registerReceiver(null, intentFilter)
    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    return if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 85
}

private fun getCurrentTimeString(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}

private fun getCurrentDateString(): String {
    return SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date())
}
