package az.diamante.ioslauncher.ui

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * iOS 18 İdarəetmə Paneli (Control Center)
 */
@Composable
fun IOSControlCenter(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Vəziyyətlər (States)
    var isWifiOn by remember { mutableStateOf(true) }
    var isBluetoothOn by remember { mutableStateOf(false) }
    var isFlashlightOn by remember { mutableStateOf(false) }
    var isAirplaneOn by remember { mutableStateOf(false) }
    var isRotationLocked by remember { mutableStateOf(true) }

    // Səs və Parlaqlıq slider-ləri
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    var currentVolume by remember {
        mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume)
    }
    var currentBrightness by remember { mutableStateOf(0.7f) }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(onClick = onDismiss)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {} // Arxa fona basanda bağlanması üçün
            ) {
                // Üst Zolaq
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "İdarəetmə Mərkəzi",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Bağla", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Üst 2 Böyük Blok: Şəbəkələr və Pleyer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Şəbəkə Qovluğu (Wi-Fi, Bluetooth, Təyyarə, Mobil)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceAround
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                ControlCircleButton(
                                    icon = Icons.Default.AirplanemodeActive,
                                    isActive = isAirplaneOn,
                                    activeColor = Color(0xFFFF9500), // Narıncı
                                    onClick = { isAirplaneOn = !isAirplaneOn }
                                )
                                ControlCircleButton(
                                    icon = Icons.Default.Wifi,
                                    isActive = isWifiOn,
                                    activeColor = Color(0xFF007AFF), // iOS Göy
                                    onClick = { isWifiOn = !isWifiOn }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                ControlCircleButton(
                                    icon = Icons.Default.Bluetooth,
                                    isActive = isBluetoothOn,
                                    activeColor = Color(0xFF007AFF),
                                    onClick = { isBluetoothOn = !isBluetoothOn }
                                )
                                ControlCircleButton(
                                    icon = Icons.Default.SignalCellularAlt,
                                    isActive = true,
                                    activeColor = Color(0xFF34C759), // Yaşıl
                                    onClick = { }
                                )
                            }
                        }
                    }

                    // Musiqi Pleyeri Bloku
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Musiqi", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text("Hazırda oxunmur", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White)
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                                Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Orta Zolaq: Parlaqlıq və Səs Şaquli Slider-ləri
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Parlaqlıq Slider Kapsulu
                    IOSSliderPill(
                        value = currentBrightness,
                        onValueChange = { currentBrightness = it },
                        icon = Icons.Default.BrightnessMedium,
                        modifier = Modifier.weight(1f)
                    )

                    // Səs Slider Kapsulu
                    IOSSliderPill(
                        value = currentVolume,
                        onValueChange = {
                            currentVolume = it
                            val targetVol = (it * maxVolume).toInt()
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                        },
                        icon = Icons.Default.VolumeUp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Alt Kvadrat Düymələr (Fənər, Kalkulyator, Kamera, Ekran Fırlanması)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Fənər (Əsl Flashlight)
                    ControlSquareButton(
                        icon = Icons.Default.FlashlightOn,
                        isActive = isFlashlightOn,
                        label = "Fənər",
                        onClick = {
                            isFlashlightOn = !isFlashlightOn
                            toggleTorch(context, isFlashlightOn)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Ekran kilidi
                    ControlSquareButton(
                        icon = Icons.Default.ScreenRotation,
                        isActive = isRotationLocked,
                        label = "Kilid",
                        onClick = { isRotationLocked = !isRotationLocked },
                        modifier = Modifier.weight(1f)
                    )

                    // Kalkulyator
                    ControlSquareButton(
                        icon = Icons.Default.Calculate,
                        isActive = false,
                        label = "Hesabla",
                        onClick = { launchCalculator(context) },
                        modifier = Modifier.weight(1f)
                    )

                    // Kamera
                    ControlSquareButton(
                        icon = Icons.Default.PhotoCamera,
                        isActive = false,
                        label = "Kamera",
                        onClick = {
                            context.startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlCircleButton(
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(if (isActive) activeColor else Color.White.copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun ControlSquareButton(
    icon: ImageVector,
    isActive: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) Color.White else Color.White.copy(alpha = 0.2f))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = if (isActive) Color.Black else Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun IOSSliderPill(
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.25f))
            .border(0.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Doldurma Hissəsi (Slider Fill)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(value.coerceIn(0f, 1f))
                .background(Color.White)
        )

        // İkon (Aşağıda mərkəzdə)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (value > 0.3f) Color.Black else Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        // Toxunma idarəsi
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}

private fun toggleTorch(context: Context, turnOn: Boolean) {
    try {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        cameraManager.setTorchMode(cameraId, turnOn)
    } catch (_: Exception) {}
}

private fun launchCalculator(context: Context) {
    try {
        val intent = Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_APP_CALCULATOR)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
