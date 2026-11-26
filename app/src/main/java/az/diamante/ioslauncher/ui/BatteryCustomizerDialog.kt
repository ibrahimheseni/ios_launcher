package az.diamante.ioslauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Premium Batareya Nişan Modeli
 */
data class PremiumBadge(
    val id: String,
    val name: String,
    val displayContent: String,
    val isTextBadge: Boolean = false,
    val accentColor: Color = Color(0xFF007AFF)
)

val PREMIUM_BATTERY_BADGES = listOf(
    PremiumBadge("az_flag", "Azərbaycan Bayrağı", "🇦🇿", false, Color(0xFF00A3E0)),
    PremiumBadge("centraldev", "centraldev.az", "centraldev.az", true, Color(0xFF00F5D4)),
    PremiumBadge("ibrahim", "İbrahim Həsəni", "İbrahim Həsəni", true, Color(0xFFFFD700)),
    PremiumBadge("tank", "Mini Tank", "🛡️ TANK", true, Color(0xFF6B7280)),
    PremiumBadge("weapon", "Taktik Silah", "⚔️ SİLAH", true, Color(0xFFE63946)),
    PremiumBadge("eagle", "Qartal", "🦅", false, Color(0xFFFF9F0A)),
    PremiumBadge("diamond", "Brilliant", "💎", false, Color(0xFF00B4D8)),
    PremiumBadge("crown", "Kral Tacı", "👑", false, Color(0xFFFFD700)),
    PremiumBadge("lightning", "Neon Şimşək", "⚡", false, Color(0xFFFFD60A)),
    PremiumBadge("fire", "Alov", "🔥", false, Color(0xFFFF453A))
)

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

/**
 * Premium Batareya Mini İkon & Şarj Animasiyası Seçim Pəncərəsi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryCustomizerBottomSheet(
    currentBadgeId: String,
    currentStyle: ChargingStyle,
    onBadgeSelected: (PremiumBadge) -> Unit,
    onStyleSelected: (ChargingStyle) -> Unit,
    onPreviewAnimation: (ChargingStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFF141416)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Premium Batareya & Şarj Mərkəzi",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Premium İkon və Xüsusi Nişanlar (centraldev.az, İbrahim Həsəni, Tank, Silah, Bayraq)
            Text(
                text = "Fərdi Nişan / İkon Seçin",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PREMIUM_BATTERY_BADGES) { badge ->
                    val isSelected = badge.id == currentBadgeId

                    if (badge.isTextBadge) {
                        // Mətn tipli Premium Kapsul (centraldev.az, İbrahim Həsəni, Tank, Silah)
                        Box(
                            modifier = Modifier
                                .height(46.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(
                                    if (isSelected) badge.accentColor.copy(alpha = 0.25f)
                                    else Color.White.copy(alpha = 0.1f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) badge.accentColor else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(23.dp)
                                )
                                .clickable { onBadgeSelected(badge) }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badge.displayContent,
                                color = if (isSelected) badge.accentColor else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // İkon / Bayraq tipli Dairəvi Nişan (🇦🇿, 💎, 🦅, ⚡)
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) badge.accentColor.copy(alpha = 0.3f)
                                    else Color.White.copy(alpha = 0.1f)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.5.dp,
                                    color = if (isSelected) badge.accentColor else Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable { onBadgeSelected(badge) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(badge.displayContent, fontSize = 22.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Şarj Animasiyası Stili Seçimi
            Text(
                text = "Şarja Taxanda Xüsusi Animasiyalar (5 Çeşid)",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ChargingStyle.values().forEach { style ->
                    val isSelected = style == currentStyle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFF2C2C2E) else Color.White.copy(alpha = 0.08f))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) Color(0xFF007AFF) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onStyleSelected(style) }
                            .padding(horizontal = 16.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(style.badge, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = style.title,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        IconButton(
                            onClick = { onPreviewAnimation(style) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Test et",
                                tint = Color(0xFF007AFF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Təsdiq et", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
