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
import az.diamante.ioslauncher.model.AppModel

/**
 * iOS 18 Gizli Tətbiqlər (Locked & Hidden Apps) Pəncərəsi
 * PIN kod təhlükəsizliyi ilə qorunur (Standart PIN: 0000).
 */
@Composable
fun HiddenAppsVaultDialog(
    hiddenApps: List<AppModel>,
    onVerifyPin: (String) -> Boolean,
    onAppClick: (AppModel) -> Unit,
    onUnhideApp: (AppModel) -> Unit,
    onDismiss: () -> Unit
) {
    var isUnlocked by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212).copy(alpha = 0.96f)
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
                    // PIN Giriş Ekranı (iOS Passcode)
                    Spacer(modifier = Modifier.height(40.dp))
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Gizli Tətbiqlər",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Açmaq üçün PIN kodu daxil edin",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // 4 dairəvi PIN indikatoru
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
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

                    Spacer(modifier = Modifier.height(30.dp))

                    // iOS Rəqəm Klaviaturası
                    IOSNumberPad(
                        onNumberClick = { digit ->
                            if (pinInput.length < 4) {
                                val nextPin = pinInput + digit
                                pinInput = nextPin
                                errorMessage = null
                                if (nextPin.length == 4) {
                                    if (onVerifyPin(nextPin)) {
                                        isUnlocked = true
                                    } else {
                                        errorMessage = "PIN kod yalnışdır! (Standart: 0000)"
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
                } else {
                    // Uğurla açıldıqda: Gizlədilmiş Tətbiqlərin Siyahısı
                    Text(
                        text = "Gizlədilmiş Tətbiqlər (${hiddenApps.size})",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (hiddenApps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Hazırda heç bir gizlədilmiş tətbiq yoxdur", color = Color.Gray)
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
                                    IOSIconItem(
                                        app = app,
                                        onClick = { onAppClick(app) },
                                        onLongClick = { }
                                    )
                                    // Gizlilikdən çıxar düyməsi
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

/**
 * iOS Stilində Dairəvi Rəqəm Düymələri
 */
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

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                    .size(72.dp)
                                    .clickable(onClick = onDeleteClick),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Backspace, contentDescription = "Sil", tint = Color.White)
                            }
                        }
                        "" -> {
                            Spacer(modifier = Modifier.size(72.dp))
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
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
