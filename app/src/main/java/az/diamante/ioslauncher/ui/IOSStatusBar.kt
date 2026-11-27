package az.diamante.ioslauncher.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * iOS 18 VIP Status Bar (Yuxarıda Sağda Xüsusi Şarj/Batareya İkonu və Dynamic Island)
 */
@Composable
fun IOSStatusBar(
    activeBadge: PremiumBadge,
    isCharging: Boolean,
    onBatteryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    var batteryLevel by remember { mutableIntStateOf(readBatteryLevel(context)) }
    var isIslandExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            batteryLevel = readBatteryLevel(context)
            kotlinx.coroutines.delay(5000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol: Canlı Saat
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Sağ: 5G + Yuxarıda Sağda VIP Batareya İkonu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onBatteryClick)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (activeBadge.isTextBadge) activeBadge.displayContent else activeBadge.displayContent,
                    color = if (activeBadge.isTextBadge) activeBadge.accentColor else Color.White,
                    fontSize = if (activeBadge.isTextBadge) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
                // Mini Batareya Kapsulu
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(10.dp)
                        .border(1.2.dp, Color.White, RoundedCornerShape(3.dp))
                        .padding(1.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((batteryLevel / 100f).coerceIn(0.1f, 1f))
                            .background(
                                if (isCharging || batteryLevel > 20) Color(0xFF34C759) else Color(0xFFFF3B30),
                                RoundedCornerShape(1.5.dp)
                            )
                    )
                }
                Text(
                    text = "$batteryLevel%",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Mərkəz: İnteraktiv Dynamic Island
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .animateContentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black)
                .clickable { isIslandExpanded = !isIslandExpanded }
                .padding(horizontal = if (isIslandExpanded || isCharging) 14.dp else 28.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isIslandExpanded || isCharging) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34C759))
                    )
                    Text(
                        text = if (isCharging) "⚡ Şarj olunur $batteryLevel%" else "iOS 18 Pro VIP",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

private fun readBatteryLevel(context: Context): Int {
    val status = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = status?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = status?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    return if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 85
}
