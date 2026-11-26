package az.diamante.ioslauncher.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.diamante.ioslauncher.manager.AppManager
import az.diamante.ioslauncher.model.AppModel
import kotlinx.coroutines.launch

/**
 * Əsas iOS 18 Pro Ana Ekranı
 * Tam funksionallıq:
 * - Premium Batareya Nişanları (🇦🇿 Bayraq, centraldev.az, İbrahim Həsəni, Tank, Silah və s.)
 * - 5 Çeşidli Şarj Animasiyası (Apple Dynamic Island, Neon Vortex, Liquid Wave, Cyber Ring, Star)
 * - İdarəetmə Mərkəzi (Control Center)
 * - Jiggle Mode və Proqram Redaktəsi / Gizlətməsi
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IOSHomeScreen(
    appManager: AppManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ios_launcher_battery_prefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var allApps by remember { mutableStateOf<List<AppModel>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppForEdit by remember { mutableStateOf<AppModel?>(null) }
    var showHiddenVault by remember { mutableStateOf(false) }
    var showControlCenter by remember { mutableStateOf(false) }
    var isJiggleMode by remember { mutableStateOf(false) }

    // Premium Batareya Nişanı Vəziyyəti
    val defaultBadge = PREMIUM_BATTERY_BADGES.first { it.id == "az_flag" }
    var activeBadge by remember {
        val savedId = prefs.getString("battery_badge_id", "az_flag")
        mutableStateOf(PREMIUM_BATTERY_BADGES.find { it.id == savedId } ?: defaultBadge)
    }

    var chargingStyle by remember {
        val savedStyle = prefs.getString("charging_style", ChargingStyle.AZERBAIJAN_GERB.name)
        mutableStateOf(ChargingStyle.valueOf(savedStyle ?: ChargingStyle.AZERBAIJAN_GERB.name))
    }
    var showBatteryCustomizer by remember { mutableStateOf(false) }
    var showChargingAnimation by remember { mutableStateOf(false) }

    fun refreshApps() {
        coroutineScope.launch {
            allApps = appManager.getInstalledApps()
        }
    }

    LaunchedEffect(Unit) {
        allApps = appManager.getInstalledApps()
    }

    val visibleApps = remember(allApps, searchQuery) {
        allApps.filter { !it.isHidden && it.displayLabel.contains(searchQuery, ignoreCase = true) }
    }
    val hiddenApps = remember(allApps) {
        allApps.filter { it.isHidden }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0D1B2A),
            Color(0xFF1B263B),
            Color(0xFF415A77)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .combinedClickable(
                onClick = { if (isJiggleMode) isJiggleMode = false },
                onLongClick = { isJiggleMode = true }
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Üst iOS Paneli
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Axtarış Kapsulu
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Axtar",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Axtarış",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                if (isJiggleMode) {
                    Button(
                        onClick = { isJiggleMode = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bitti", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Control Center
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { showControlCenter = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "İdarəetmə Mərkəzi", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Gizli Tətbiqlər
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { showHiddenVault = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Gizli Tətbiqlər", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Vidcetlər və Tətbiqlər Şəbəkəsi
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp)
            ) {
                // iOS 18 Vidcetlər Zolağı (Premium Nişanlar ilə)
                item(span = { GridItemSpan(4) }) {
                    IOSWidgetsRow(
                        activeBadge = activeBadge,
                        onBatteryWidgetClick = { showBatteryCustomizer = true },
                        onChargingDetected = { showChargingAnimation = true }
                    )
                }

                // Tətbiqlər Şəbəkəsi
                items(visibleApps, key = { it.packageName }) { app ->
                    IOSIconItem(
                        app = app,
                        isJiggleMode = isJiggleMode,
                        onClick = {
                            if (isJiggleMode) selectedAppForEdit = app else appManager.launchApp(app)
                        },
                        onLongClick = {
                            isJiggleMode = true
                            selectedAppForEdit = app
                        },
                        onRemoveClick = {
                            appManager.setAppHidden(app.packageName, true)
                            refreshApps()
                        }
                    )
                }
            }

            // Alt iOS 18 Dock
            if (visibleApps.isNotEmpty()) {
                IOSDockView(
                    dockApps = visibleApps.take(4),
                    onAppClick = { appManager.launchApp(it) },
                    onAppLongClick = {
                        isJiggleMode = true
                        selectedAppForEdit = it
                    }
                )
            }
        }

        // Tətbiq Redaktə Menyusu
        selectedAppForEdit?.let { app ->
            EditAppBottomSheet(
                app = app,
                onDismiss = { selectedAppForEdit = null },
                onSave = { newName, newIconUri ->
                    appManager.saveCustomization(app.packageName, newName, newIconUri)
                    selectedAppForEdit = null
                    refreshApps()
                },
                onHide = {
                    appManager.setAppHidden(app.packageName, true)
                    selectedAppForEdit = null
                    refreshApps()
                },
                onReset = {
                    appManager.saveCustomization(app.packageName, null, null)
                    selectedAppForEdit = null
                    refreshApps()
                }
            )
        }

        // Gizli Tətbiqlər Qovluğu
        if (showHiddenVault) {
            HiddenAppsVaultDialog(
                hiddenApps = hiddenApps,
                onVerifyPin = { appManager.checkPin(it) },
                onAppClick = {
                    appManager.launchApp(it)
                    showHiddenVault = false
                },
                onUnhideApp = { app ->
                    appManager.setAppHidden(app.packageName, false)
                    refreshApps()
                },
                onDismiss = { showHiddenVault = false }
            )
        }

        // Control Center
        IOSControlCenter(
            isVisible = showControlCenter,
            onDismiss = { showControlCenter = false }
        )

        // Premium Batareya & Şarj Tənzimləmə Pəncərəsi
        if (showBatteryCustomizer) {
            BatteryCustomizerBottomSheet(
                currentBadgeId = activeBadge.id,
                currentStyle = chargingStyle,
                onBadgeSelected = { selected ->
                    activeBadge = selected
                    prefs.edit().putString("battery_badge_id", selected.id).apply()
                },
                onStyleSelected = { newStyle ->
                    chargingStyle = newStyle
                    prefs.edit().putString("charging_style", newStyle.name).apply()
                },
                onPreviewAnimation = { previewStyle ->
                    chargingStyle = previewStyle
                    showChargingAnimation = true
                },
                onDismiss = { showBatteryCustomizer = false }
            )
        }

        // Şarj Animasiyası
        if (showChargingAnimation) {
            ChargingAnimationOverlay(
                batteryLevel = 85,
                style = chargingStyle,
                onDismiss = { showChargingAnimation = false }
            )
        }
    }
}
