package az.diamante.ioslauncher.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Şarj Animasiyası Növləri (Genişləndirilmiş Premium Çeşidlər)
 */
enum class ChargingStyle(val title: String, val badge: String) {
    AZERBAIJAN_GERB("Azərbaycan Gerbi & Alov", "🇦🇿"),
    VENOM("Venom Symbiote Dark", "🕷️"),
    TESLA("Tesla Supercharger", "⚡"),
    MILITARY_RADAR("Hərbi Taktik Radar", "🎖️"),
    APPLE_MAGSAFE("Apple MagSafe & Dynamic Island", "🍏"),
    NEON_VORTEX("Neon Enerji Burulğanı", "🌀"),
    LIQUID_WAVE("Maye Enerji Dalğası", "🌊"),
    CYBER_RING("Cyberpunk Neon Halqa", "🤖")
}

/**
 * Telefon şarja taxıldıqda ekranda çıxan xüsusi animasiya
 */
@Composable
fun ChargingAnimationOverlay(
    batteryLevel: Int,
    style: ChargingStyle,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5500)
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        // Yuxarı sağda çıxış düyməsi
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Bağla", tint = Color.White)
            }
        }

        when (style) {
            ChargingStyle.AZERBAIJAN_GERB -> AzerbaijanGerbAnimation(batteryLevel)
            ChargingStyle.VENOM -> VenomSymbioteAnimation(batteryLevel)
            ChargingStyle.TESLA -> TeslaSuperchargerAnimation(batteryLevel)
            ChargingStyle.MILITARY_RADAR -> MilitaryRadarAnimation(batteryLevel)
            ChargingStyle.APPLE_MAGSAFE -> AppleMagSafeAnimation(batteryLevel)
            ChargingStyle.NEON_VORTEX -> NeonVortexAnimation(batteryLevel)
            ChargingStyle.LIQUID_WAVE -> LiquidWaveAnimation(batteryLevel)
            ChargingStyle.CYBER_RING -> CyberRingAnimation(batteryLevel)
        }
    }
}

/**
 * 1. 🇦🇿 Azərbaycan Gerbi & Sönməz Alov Şarj Animasiyası
 */
@Composable
private fun AzerbaijanGerbAnimation(batteryLevel: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "GerbTransition")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "GerbAura"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "StarRotation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(auraScale),
            contentAlignment = Alignment.Center
        ) {
            // Qızılı Parıltı Halqası
            Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFFFFD700),
                            Color(0xFF00A3E0), // Göy
                            Color(0xFFEF3340), // Qırmızı
                            Color(0xFF239F40), // Yaşıl
                            Color(0xFFFFD700)
                        )
                    ),
                    style = Stroke(width = 8f)
                )
            }

            // Mərkəzdə Sönməz Alov və Səkkizguşəli Ulduz Rəmzi
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔥", fontSize = 48.sp)
                Text(
                    text = "%$batteryLevel",
                    color = Color(0xFFFFD700),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "ODLAR YURDU",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "CAN AZƏRBAYCAN",
            color = Color(0xFF00A3E0),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
    }
}

/**
 * 2. 🕷️ Venom Symbiote Dark Animasiyası
 */
@Composable
private fun VenomSymbioteAnimation(batteryLevel: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "VenomTransition")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "VenomPulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF2B0938), Color(0xFF100214), Color.Black)
                    )
                )
                .border(2.dp, Color(0xFF9D4EDD), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Kəskin Venom Gözləri və Qaranlıq Enerji
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(26.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👁️", fontSize = 34.sp)
                    Text("👁️", fontSize = 34.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "%$batteryLevel",
                    color = Color(0xFFE0AAFF),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "WE ARE VENOM",
                    color = Color(0xFFFF0055),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SYMBIOTE CHARGE",
            color = Color(0xFF9D4EDD),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 3. ⚡ Tesla Supercharger Animasiyası
 */
@Composable
private fun TeslaSuperchargerAnimation(batteryLevel: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "TeslaTransition")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "TeslaGlow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(Color(0xFF111111))
                .border(3.dp, Color(0xFFE82127).copy(alpha = glowAlpha), CircleShape), // Tesla Qırmızısı
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Tesla "T" Rəmzi
                Text("⚡ TESLA", color = Color(0xFFE82127), fontSize = 20.sp, fontWeight = FontWeight.Black)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "%$batteryLevel",
                    color = Color.White,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "250 kW SUPERCHARGING",
                    color = Color(0xFFE82127),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TESLA SUPERCHARGER",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp
        )
    }
}

/**
 * 4. 🎖️ Hərbi Taktik Radar Şarj Animasiyası (Military / Tactical)
 */
@Composable
private fun MilitaryRadarAnimation(batteryLevel: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "RadarSweep"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(230.dp)
                .clip(CircleShape)
                .background(Color(0xFF0A150A))
                .border(2.dp, Color(0xFF00FF66), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Radar Skan Xətləri və Dairələri
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = Color(0xFF00FF66).copy(alpha = 0.3f), radius = size.minDimension / 3.5f, style = Stroke(2f))
                drawCircle(color = Color(0xFF00FF66).copy(alpha = 0.2f), radius = size.minDimension / 2.2f, style = Stroke(2f))

                // X və Y Hədəf Xətləri
                drawLine(Color(0xFF00FF66).copy(alpha = 0.3f), Offset(0f, size.height / 2), Offset(size.width, size.height / 2))
                drawLine(Color(0xFF00FF66).copy(alpha = 0.3f), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height))
            }

            // Radar fırlanan yaşıl skaner şüası
            Canvas(modifier = Modifier.fillMaxSize().rotate(sweepAngle)) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(Color.Transparent, Color(0xFF00FF66).copy(alpha = 0.45f))),
                    startAngle = 0f,
                    sweepAngle = 60f,
                    useCenter = true
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎯", fontSize = 24.sp)
                Text(
                    text = "%$batteryLevel",
                    color = Color(0xFF00FF66),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "HƏRBİ GÜC",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "STATUS: COMBAT READY",
            color = Color(0xFF00FF66),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

/**
 * 5. 🍏 Apple MagSafe & Dynamic Island Animasiyası
 */
@Composable
private fun AppleMagSafeAnimation(batteryLevel: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "MagSafeTransition")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "MagSafeScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // MagSafe Dairəsi
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(ringScale)
                .clip(CircleShape)
                .background(Color(0xFF1C1C1E))
                .border(10.dp, Color(0xFF34C759), CircleShape), // Apple Neon Yaşıl
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color(0xFF34C759),
                    modifier = Modifier.size(44.dp)
                )
                Text(
                    text = "%$batteryLevel",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "MagSafe",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * 6. Neon Enerji Burulğanı (Vortex)
 */
@Composable
private fun NeonVortexAnimation(batteryLevel: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "VortexTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "VortexRotation"
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(230.dp).rotate(rotation)) {
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(Color(0xFF00F5D4), Color(0xFF7B2CBF), Color(0xFFFF007F), Color(0xFF00F5D4))
                ),
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(44.dp))
            Text("%$batteryLevel", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Text("Sürətli Şarj", color = Color(0xFF00F5D4), fontSize = 13.sp)
        }
    }
}

/**
 * 7. Maye Enerji Dalğası (Liquid Wave)
 */
@Composable
private fun LiquidWaveAnimation(batteryLevel: Int) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(Color(0xFF14213D)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fillHeight = size.height * (1f - (batteryLevel / 100f).coerceIn(0.1f, 0.95f))
            drawRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF00B4D8), Color(0xFF0077B6))),
                topLeft = Offset(0f, fillHeight),
                size = Size(size.width, size.height - fillHeight)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("%$batteryLevel", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
            Text("Maye Enerji", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
    }
}

/**
 * 8. Cyberpunk Neon Halqa
 */
@Composable
private fun CyberRingAnimation(batteryLevel: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "CyberTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "CyberPulse"
    )

    Box(
        modifier = Modifier.scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            drawCircle(color = Color(0xFFFFEA00), style = Stroke(width = 8f))
            drawCircle(color = Color(0xFFFF0055), radius = size.minDimension / 2.4f, style = Stroke(width = 4f))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("CYBER CHARGE", color = Color(0xFFFFEA00), fontSize = 12.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text("%$batteryLevel", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
            Text("MAX VOLTAGE", color = Color(0xFFFF0055), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
