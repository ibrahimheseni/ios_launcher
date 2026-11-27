package az.diamante.ioslauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import az.diamante.ioslauncher.data.IconVisualStyle
import az.diamante.ioslauncher.data.ObsidianPalette
import az.diamante.ioslauncher.model.AppModel
import az.diamante.ioslauncher.ui.icons.CentralDevAppIcon

/**
 * iOS 18 Gizli Tətbiqlər (Locked & Hidden Apps) Pəncərəsi
 * PIN dəyişmə və tam gizlilik dəstəkli.
 */
@Composable
fun HiddenAppsVaultDialog(
    hiddenApps: List<AppModel>,
    onVerifyPin: (String) -> Boolean,
    onChangePin: (String) -> Unit,
    onAppClick: (AppModel) -> Unit,
    onUnhideApp: (AppModel) -> Unit,
    onDismiss: () -> Unit
) {
    var isUnlocked by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // PIN dəyişmə rejimi
    var isChangingPinMode by remember { mutableStateOf(false) }
    var newPinStep by remember { mutableIntStateOf(1) } // 1: yeni pin daxil et, 2: təkrar et
    var tempNewPin by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212).copy(alpha = 0.97f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Yuxarı çıxış düyməsi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Bağla", tint = Color.White)
                    }
                }

                if (!isUnlocked) {
                    // PIN Giriş Ekranı
                    Spacer(modifier = Modifier.height(30.dp))
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Gizli Tətbiqlər Seyfi",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Açmaq üçün 4 rəqəmli PIN daxil edin",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // 4 dairəvi PIN indikatoru
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index < pinInput.length) Color.White else Color.DarkGray
                                    )
                            )
                        }
                    }

                    errorMessage?.let {
                        Text(it, color = Color(0xFFFF453A), fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    IOSNumberPad(
                        onNumberClick = { digit ->
                            if (pinInput.length < 4) {
                                val nextPin = pinInput + digit
                                pinInput = nextPin
                                errorMessage = null
                                if (nextPin.length == 4) {
                                    if (onVerifyPin(nextPin)) {
                                        isUnlocked = true
                                        pinInput = ""
                                    } else {
                                        errorMessage = "PIN kod yalnışdır! (İlkin kod: 0000)"
                                        pinInput = ""
                                    }
                                }
                            }
                        },
                        onDeleteClick = {
                            if (pinInput.isNotEmpty()) {
                                pinInput = pinInput.dropLast(1)
                                errorMessage = null
                            }
                        }
                    )
                } else if (isChangingPinMode) {
                    // PIN Dəyişmə Ekranı
                    Spacer(modifier = Modifier.height(30.dp))
                    Icon(
                        Icons.Default.Password,
                        contentDescription = null,
                        tint = Color(0xFF007AFF),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (newPinStep == 1) "Yeni 4 Rəqəmli PIN Yazın" else "Yeni PIN-i Təkrar Edin",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index < pinInput.length) Color(0xFF007AFF) else Color.DarkGray
                                    )
                            )
                        }
                    }

                    statusMessage?.let {
                        Text(it, color = Color(0xFFFF453A), fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    IOSNumberPad(
                        onNumberClick = { digit ->
                            if (pinInput.length < 4) {
                                val nextPin = pinInput + digit
                                pinInput = nextPin
                                if (nextPin.length == 4) {
                                    if (newPinStep == 1) {
                                        tempNewPin = nextPin
                                        pinInput = ""
                                        newPinStep = 2
                                        statusMessage = null
                                    } else {
                                        if (nextPin == tempNewPin) {
                                            onChangePin(nextPin)
                                            isChangingPinMode = false
                                            pinInput = ""
                                            newPinStep = 1
                                        } else {
                                            statusMessage = "PIN kodlar uyğun gəlmədi! Yenidən cəhd edin."
                                            pinInput = ""
                                            newPinStep = 1
                                        }
                                    }
                                }
                            }
                        },
                        onDeleteClick = {
                            if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { isChangingPinMode = false }) {
                        Text("Geri qayıt", color = Color.Gray)
                    }
                } else {
                    // Uğurla açıldıqda: Gizlədilmiş Tətbiqlər Siyahısı & PIN Dəyişmə Düyməsi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gizli Tətbiqlər (${hiddenApps.size})",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // PIN Kodu Dəyiş Düyməsi
                        Button(
                            onClick = { isChangingPinMode = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Password, contentDescription = null, tint = Color(0xFF007AFF), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PIN-i Dəyiş", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (hiddenApps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Heç bir gizlədilmiş tətbiq yoxdur.\nİstənilən proqrama uzun basaraq onu bura gizlədə bilərsiniz.", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(hiddenApps, key = { it.packageName }) { app ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    CentralDevAppIcon(
                                        app = app,
                                        iconStyle = IconVisualStyle.DARK,
                                        accentColor = ObsidianPalette.ControlledRed,
                                        iconSize = 54.dp,
                                        showLabel = true,
                                        onClick = { onAppClick(app) },
                                        onLongClick = { }
                                    )
                                    IconButton(
                                        onClick = { onUnhideApp(app) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Visibility,
                                            contentDescription = "Görünən et",
                                            tint = Color(0xFF007AFF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IOSNumberPad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { item ->
                    when (item) {
                        "DEL" -> {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clickable(onClick = onDeleteClick),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Backspace, contentDescription = "Sil", tint = Color.White)
                            }
                        }
                        "" -> {
                            Spacer(modifier = Modifier.size(70.dp))
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable { onNumberClick(item) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item,
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
