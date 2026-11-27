package az.diamante.ioslauncher.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.diamante.ioslauncher.manager.AppManager
import az.diamante.ioslauncher.model.AppModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IOSHomeScreen(appManager: AppManager, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ios_launcher_battery_prefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var allApps by remember { mutableStateOf<List<AppModel>>(emptyList()) }
    var customOrder by remember {
        val saved = prefs.getString("custom_app_order", "") ?: ""
        mutableStateOf(if (saved.isBlank()) emptyList() else saved.split(","))
    }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppForEdit by remember { mutableStateOf<AppModel?>(null) }
    var selectedAppForDelete by remember { mutableStateOf<AppModel?>(null) }
    var showHiddenVault by remember { mutableStateOf(false) }
    var showControlCenter by remember { mutableStateOf(false) }
    var showWallpaperSelector by remember { mutableStateOf(false) }
    var isJiggleMode by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var selectedWallpaperUri by remember { mutableStateOf(prefs.getString("selected_wallpaper_uri", null)) }

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

    fun refreshApps() { coroutineScope.launch { allApps = appManager.getInstalledApps() } }
    fun moveApp(pkg: String, delta: Int) {
        val currentList = allApps.filter { !it.isHidden }.map { it.packageName }.toMutableList()
        val ordered = if (customOrder.isNotEmpty()) {
            val base = customOrder.filter { currentList.contains(it) }.toMutableList()
            currentList.forEach { if (!base.contains(it)) base.add(it) }
            base
        } else currentList
        val idx = ordered.indexOf(pkg)
        val target = idx + delta
        if (idx in ordered.indices && target in ordered.indices) {
            val tmp = ordered[idx]; ordered[idx] = ordered[target]; ordered[target] = tmp
            customOrder = ordered
            prefs.edit().putString("custom_app_order", ordered.joinToString(",")).apply()
        }
    }

    LaunchedEffect(Unit) { allApps = appManager.getInstalledApps() }

    val visibleApps = remember(allApps, searchQuery, customOrder) {
        val filtered = allApps.filter { !it.isHidden && (searchQuery.isBlank() || it.displayLabel.contains(searchQuery.trim(), ignoreCase = true)) }
        if (customOrder.isEmpty()) filtered else filtered.sortedBy { app -> val idx = customOrder.indexOf(app.packageName); if (idx == -1) Int.MAX_VALUE else idx }
    }
    val hiddenApps = remember(allApps) { allApps.filter { it.isHidden } }
    val appsPerPage = 20
    val pages = remember(visibleApps) { if (visibleApps.isEmpty()) listOf(emptyList()) else visibleApps.chunked(appsPerPage) }
    val pagerState = rememberPagerState(pageCount = { pages.size.coerceAtLeast(1) })

    Box(modifier = modifier.fillMaxSize().combinedClickable(onClick = { if (isJiggleMode) isJiggleMode = false }, onLongClick = { isJiggleMode = true })) {
        if (selectedWallpaperUri != null) {
            AsyncImage(model = selectedWallpaperUri, contentDescription = "Wallpaper", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)))
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0A1128), Color(0xFF1C2541), Color(0xFF3A506B), Color(0xFF1B4332)))))
        }

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            IOSStatusBar(activeBadge = activeBadge, isCharging = showChargingAnimation, onBatteryClick = { showBatteryCustomizer = true })

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(19.dp)).background(Color.White.copy(alpha = 0.22f)).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = "Axtar", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery, onValueChange = { searchQuery = it }, singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp), cursorBrush = SolidColor(Color.White), modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField -> if (searchQuery.isEmpty()) Text(text = "Axtarış...", color = Color.White.copy(alpha = 0.65f), fontSize = 14.sp); innerTextField() }
                    )
                    if (searchQuery.isNotEmpty()) Icon(Icons.Default.Close, contentDescription = "Təmizlə", tint = Color.White, modifier = Modifier.size(16.dp).clickable { searchQuery = "" })
                }
                Spacer(modifier = Modifier.width(6.dp))
                if (isJiggleMode) {
                    Button(onClick = { isJiggleMode = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Bitti", color = Color.White, fontSize = 13.sp)
                    }
                } else {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.2f)).clickable { isLocked = true }, contentAlignment = Alignment.Center) { Icon(Icons.Default.PhonelinkLock, contentDescription = "Ekranı Kilidlə", tint = Color.White, modifier = Modifier.size(17.dp)) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.2f)).clickable { showWallpaperSelector = true }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Image, contentDescription = "Divar Kağızı", tint = Color.White, modifier = Modifier.size(17.dp)) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.2f)).clickable { showControlCenter = true }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Tune, contentDescription = "İdarəetmə", tint = Color.White, modifier = Modifier.size(17.dp)) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.2f)).clickable { showHiddenVault = true }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Lock, contentDescription = "Gizli Seyf", tint = Color.White, modifier = Modifier.size(17.dp)) }
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { pageIndex ->
                val pageApps = pages.getOrElse(pageIndex) { emptyList() }
                LazyVerticalGrid(columns = GridCells.Fixed(4), userScrollEnabled = false, modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp)) {
                    if (pageIndex == 0 && searchQuery.isEmpty()) {
                        item(span = { GridItemSpan(4) }) {
                            IOSWidgetsRow(activeBadge = activeBadge, onBatteryWidgetClick = { showBatteryCustomizer = true }, onChargingDetected = { showChargingAnimation = true })
                        }
                    }
                    items(pageApps, key = { it.packageName }) { app ->
                        IOSIconItem(
                            app = app, isJiggleMode = isJiggleMode,
                            onClick = { if (isJiggleMode) selectedAppForEdit = app else appManager.launchApp(app) },
                            onLongClick = { isJiggleMode = true; selectedAppForEdit = app },
                            onRemoveClick = { selectedAppForDelete = app }
                        )
                    }
                }
            }

            if (pages.size > 1 && searchQuery.isEmpty()) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    repeat(pages.size) { index ->
                        val isCurrent = pagerState.currentPage == index
                        Box(modifier = Modifier.padding(horizontal = 3.5.dp).size(if (isCurrent) 7.5.dp else 5.5.dp).clip(CircleShape).background(if (isCurrent) Color.White else Color.White.copy(alpha = 0.35f)))
                    }
                }
            }

            if (visibleApps.isNotEmpty() && searchQuery.isEmpty()) {
                IOSDockView(dockApps = visibleApps.take(4), onAppClick = { appManager.launchApp(it) }, onAppLongClick = { isJiggleMode = true; selectedAppForEdit = it })
            }
        }

        AnimatedVisibility(visible = isLocked, enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(), exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()) {
            IOSLockScreen(activeBadge = activeBadge, onUnlock = { isLocked = false })
        }

        selectedAppForDelete?.let { app ->
            IOSDeleteAppBottomSheet(
                app = app,
                onUninstallApp = { targetApp ->
                    try { context.startActivity(Intent(Intent.ACTION_DELETE).apply { data = Uri.parse("package:${targetApp.packageName}"); flags = Intent.FLAG_ACTIVITY_NEW_TASK }) } catch (_: Exception) {}
                    selectedAppForDelete = null; refreshApps()
                },
                onHideApp = { targetApp -> appManager.setAppHidden(targetApp.packageName, true); selectedAppForDelete = null; refreshApps() },
                onDismiss = { selectedAppForDelete = null }
            )
        }

        selectedAppForEdit?.let { app ->
            EditAppBottomSheet(
                app = app, onDismiss = { selectedAppForEdit = null },
                onSave = { newName, newIconUri -> appManager.saveCustomization(app.packageName, newName, newIconUri); selectedAppForEdit = null; refreshApps() },
                onMoveLeft = { moveApp(app.packageName, -1) }, onMoveRight = { moveApp(app.packageName, 1) },
                onHide = { appManager.setAppHidden(app.packageName, true); selectedAppForEdit = null; refreshApps() },
                onReset = { appManager.saveCustomization(app.packageName, null, null); selectedAppForEdit = null; refreshApps() }
            )
        }

        if (showHiddenVault) {
            HiddenAppsVaultDialog(
                hiddenApps = hiddenApps, onVerifyPin = { appManager.checkPin(it) }, onChangePin = { newPin -> appManager.setPin(newPin) },
                onAppClick = { appManager.launchApp(it); showHiddenVault = false },
                onUnhideApp = { app -> appManager.setAppHidden(app.packageName, false); refreshApps() },
                onDismiss = { showHiddenVault = false }
            )
        }

        IOSControlCenter(isVisible = showControlCenter, onDismiss = { showControlCenter = false })

        if (showBatteryCustomizer) {
            BatteryCustomizerBottomSheet(
                currentBadgeId = activeBadge.id, currentStyle = chargingStyle,
                onBadgeSelected = { selected -> activeBadge = selected; prefs.edit().putString("battery_badge_id", selected.id).apply() },
                onStyleSelected = { newStyle -> chargingStyle = newStyle; prefs.edit().putString("charging_style", newStyle.name).apply() },
                onPreviewAnimation = { previewStyle -> chargingStyle = previewStyle; showChargingAnimation = true },
                onDismiss = { showBatteryCustomizer = false }
            )
        }

        if (showWallpaperSelector) {
            WallpaperSelectorBottomSheet(
                currentWallpaperUri = selectedWallpaperUri,
                onWallpaperSelected = { uri -> selectedWallpaperUri = uri; prefs.edit().putString("selected_wallpaper_uri", uri).apply() },
                onDismiss = { showWallpaperSelector = false }
            )
        }

        if (showChargingAnimation) {
            ChargingAnimationOverlay(batteryLevel = 85, style = chargingStyle, onDismiss = { showChargingAnimation = false })
        }
    }
}
