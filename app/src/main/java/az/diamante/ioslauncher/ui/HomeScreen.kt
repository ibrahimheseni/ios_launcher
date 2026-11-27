package az.diamante.ioslauncher.ui

import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import az.diamante.ioslauncher.data.*
import az.diamante.ioslauncher.manager.AppManager
import az.diamante.ioslauncher.manager.BatterySnapshot
import az.diamante.ioslauncher.manager.ChargingMonitor
import az.diamante.ioslauncher.model.AppModel
import az.diamante.ioslauncher.ui.battery.BatteryStudioSheet
import az.diamante.ioslauncher.ui.battery.CentralDevBatteryIndicator
import az.diamante.ioslauncher.ui.charging.ChargingCinemaGallerySheet
import az.diamante.ioslauncher.ui.charging.ChargingCinemaOverlay
import az.diamante.ioslauncher.ui.icons.CentralDevAppIcon
import az.diamante.ioslauncher.ui.settings.CentralDevSettingsDialog
import az.diamante.ioslauncher.ui.settings.launchDefaultHomeChooser
import az.diamante.ioslauncher.ui.wallpaper.CentralDevWallpaperCanvas
import az.diamante.ioslauncher.ui.wallpaper.WallpaperStudioSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed interface HomeGridSlot {
    data class AppSlot(val app: AppModel) : HomeGridSlot
    data class FolderSlot(val folder: FolderModel, val apps: List<AppModel>) : HomeGridSlot
}

@Composable
fun CentralDevHomeScreen(
    appManager: AppManager,
    chargingMonitor: ChargingMonitor,
    openChargingSignal: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repo = appManager.repo
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var settings by remember { mutableStateOf(repo.loadSettings()) }
    var allApps by remember { mutableStateOf<List<AppModel>>(emptyList()) }
    var homeOrder by remember { mutableStateOf(repo.loadHomeOrder()) }
    var dockKeys by remember { mutableStateOf(repo.loadDockKeys()) }
    var folders by remember { mutableStateOf(repo.loadFolders()) }

    var batterySnapshot by remember { mutableStateOf(chargingMonitor.readCurrentSnapshot()) }
    var isForeground by remember { mutableStateOf(true) }
    var showChargingCinema by remember { mutableStateOf<ChargingSceneId?>(null) }

    // Panellər
    var showAppDrawer by remember { mutableStateOf(false) }
    var showSearchSheet by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showBatteryStudio by remember { mutableStateOf(false) }
    var showChargingGallery by remember { mutableStateOf(false) }
    var showWallpaperStudio by remember { mutableStateOf(false) }
    var showHiddenVault by remember { mutableStateOf(false) }
    var contextApp by remember { mutableStateOf<AppModel?>(null) }
    var openedFolder by remember { mutableStateOf<FolderModel?>(null) }

    val powerManager = remember { context.getSystemService(android.content.Context.POWER_SERVICE) as? PowerManager }
    val isPowerSave = powerManager?.isPowerSaveMode == true

    fun reloadAll() {
        scope.launch {
            allApps = appManager.getInstalledApps()
            homeOrder = repo.loadHomeOrder()
            dockKeys = repo.loadDockKeys()
            folders = repo.loadFolders()
            settings = repo.loadSettings()
        }
    }

    fun updateSettings(next: CentralDevSettings) {
        settings = next
        repo.saveSettings(next)
    }

    LaunchedEffect(Unit) { reloadAll() }
    LaunchedEffect(openChargingSignal) {
        if (openChargingSignal > 0) showChargingCinema = settings.charging.sceneId
    }

    // Lifecycle & Paket dəyişiklikləri
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            isForeground = event == Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        val pkgCb = appManager.registerPackageCallback { reloadAll() }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(obs)
            appManager.unregisterPackageCallback(pkgCb)
        }
    }

    // Real batareya və yalnız yeni qoşulmada bir dəfə şarj səhnəsi
    DisposableEffect(Unit) {
        var wasPlugged = chargingMonitor.readCurrentSnapshot().isPlugged
        val receiver = chargingMonitor.createReceiver { snap, isNewPlugEvent ->
            batterySnapshot = snap
            if (isNewPlugEvent && !wasPlugged) {
                if (isForeground) {
                    showChargingCinema = settings.charging.sceneId
                } else if (settings.charging.notifyWhenOtherApp) {
                    chargingMonitor.postOptionalChargingNotification(snap.level)
                }
            }
            wasPlugged = snap.isPlugged
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }

    val homeRoleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    // Görünən tətbiqlər (gizli tətbiqlər daxil deyil)
    val visibleApps = remember(allApps) { allApps.filter { !it.isHidden } }
    val hiddenApps = remember(allApps) { allApps.filter { it.isHidden } }
    val appByKey = remember(visibleApps) { visibleApps.associateBy { it.componentKey } }

    // Dock tətbiqləri (4 və ya 5 ikon)
    val dockApps = remember(visibleApps, dockKeys, settings.dockCapacity) {
        val picked = dockKeys.mapNotNull { appByKey[it] }.toMutableList()
        if (picked.isEmpty()) {
            picked.addAll(visibleApps.take(settings.dockCapacity))
        }
        picked.take(settings.dockCapacity)
    }

    // Ana ekran slotları (Qovluqlar + tətbiqlər, tor kiçildikdə artıq ikonlar növbəti səhifəyə keçir)
    val homeSlots = remember(visibleApps, homeOrder, folders, dockApps) {
        val dockSet = dockApps.map { it.componentKey }.toSet()
        val folderKeySet = folders.flatMap { it.itemKeys }.toSet()
        val slots = mutableListOf<HomeGridSlot>()

        folders.forEach { f ->
            val fApps = f.itemKeys.mapNotNull { appByKey[it] }
            if (fApps.isNotEmpty()) slots.add(HomeGridSlot.FolderSlot(f, fApps))
        }

        val candidates = visibleApps.filter { !dockSet.contains(it.componentKey) && !folderKeySet.contains(it.componentKey) }
        val ordered = if (homeOrder.isEmpty()) candidates else {
            val map = candidates.associateBy { it.componentKey }
            val list = homeOrder.mapNotNull { map[it] }.toMutableList()
            candidates.forEach { if (!homeOrder.contains(it.componentKey)) list.add(it) }
            list
        }
        ordered.forEach { slots.add(HomeGridSlot.AppSlot(it)) }
        slots
    }

    // 1-ci səhifənin üst hissəsi saat və fonun əsas obyektinə ayrılır (aşağıda ikonlar),
    // növbəti səhifələr tam tor tutumundan istifadə edir.
    val firstPageCapacity = (settings.gridPreset.cols * 2).coerceAtLeast(6)
    val otherPageCapacity = settings.gridPreset.cols * settings.gridPreset.rows
    val pages = remember(homeSlots, firstPageCapacity, otherPageCapacity) {
        if (homeSlots.isEmpty()) listOf(emptyList())
        else {
            val result = mutableListOf<List<HomeGridSlot>>()
            result.add(homeSlots.take(firstPageCapacity))
            val remaining = homeSlots.drop(firstPageCapacity)
            if (remaining.isNotEmpty()) {
                result.addAll(remaining.chunked(otherPageCapacity))
            }
            result
        }
    }
    val pagerState = rememberPagerState(pageCount = { pages.size.coerceAtLeast(1) })
    val accentColor = Color(settings.accentColorHex)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(settings.doubleTapAction) {
                detectTapGestures(
                    onDoubleTap = {
                        when (settings.doubleTapAction) {
                            DoubleTapAction.OPEN_SEARCH -> showSearchSheet = true
                            DoubleTapAction.OPEN_SETTINGS -> showSettings = true
                            DoubleTapAction.OPEN_BATTERY_STUDIO -> showBatteryStudio = true
                            DoubleTapAction.OPEN_CHARGING_CINEMA -> showChargingCinema = settings.charging.sceneId
                            DoubleTapAction.NONE -> {}
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onVerticalDrag = { _, dragAmount -> totalDrag += dragAmount },
                    onDragEnd = {
                        if (totalDrag > 90f) showSearchSheet = true
                        else if (totalDrag < -90f) showAppDrawer = true
                    }
                )
            }
    ) {
        // CentralDev Obsidian / Parallax / 3D Fon
        CentralDevWallpaperCanvas(
            config = settings.wallpaper,
            pageOffsetRatio = pagerState.currentPageOffsetFraction,
            isHomeVisible = isForeground && !showAppDrawer && !showSearchSheet && showChargingCinema == null,
            isPowerSaveMode = isPowerSave
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Yuxarı saat + kompakt/geniş batareya göstəricisi (reklamsız, təmiz)
            TopClockAndBatteryBar(
                settings = settings,
                battery = batterySnapshot,
                onBatteryClick = { showBatteryStudio = true },
                onSettingsClick = { showSettings = true }
            )

            // İlk açılış onboarding kartı (bir kliklə standart ana ekran)
            if (settings.showOnboardingBanner) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianPalette.GraphiteSurface.copy(alpha = 0.88f))
                        .border(0.8.dp, ObsidianPalette.SubtleBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("CentralDev Obsidian", color = ObsidianPalette.SoftWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Tətbiqlər üçün yuxarı, axtarış üçün aşağı sürüşdürün.", color = ObsidianPalette.MutedText, fontSize = 11.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { launchDefaultHomeChooser(context, homeRoleLauncher::launch) },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Əsas ekran et", fontSize = 11.sp, color = Color.White)
                        }
                        IconButton(
                            onClick = { updateSettings(settings.copy(showOnboardingBanner = false)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Bağla", tint = ObsidianPalette.MutedText, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Səhifə-səhifə ana ekran
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val pageSlots = pages.getOrElse(pageIndex) { emptyList() }
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1-ci səhifənin orta hissəsi fonun əsas obyektinə ayrılan boş sahədir
                    if (pageIndex == 0) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(settings.gridPreset.cols),
                        userScrollEnabled = false,
                        verticalArrangement = Arrangement.spacedBy(settings.rowSpacingDp.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        items(pageSlots, key = {
                            when (it) {
                                is HomeGridSlot.AppSlot -> it.app.componentKey
                                is HomeGridSlot.FolderSlot -> "folder_${it.folder.id}"
                            }
                        }) { slot ->
                            when (slot) {
                                is HomeGridSlot.AppSlot -> {
                                    CentralDevAppIcon(
                                        app = slot.app,
                                        iconStyle = settings.iconStyle,
                                        accentColor = accentColor,
                                        iconSize = settings.iconSizeDp.dp,
                                        showLabel = settings.showLabels,
                                        onClick = { appManager.launchApp(slot.app) },
                                        onLongClick = { contextApp = slot.app }
                                    )
                                }
                                is HomeGridSlot.FolderSlot -> {
                                    FolderIconItem(
                                        folder = slot.folder,
                                        apps = slot.apps,
                                        iconStyle = settings.iconStyle,
                                        accentColor = accentColor,
                                        iconSize = settings.iconSizeDp.dp,
                                        showLabel = settings.showLabels,
                                        onClick = { openedFolder = slot.folder }
                                    )
                                }
                            }
                        }
                    }
                    if (pageIndex > 0) Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Səhifə indikatoru + Tətbiqlər / Axtarış qısa düymələri
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tətbiqlər",
                    color = ObsidianPalette.SoftWhite.copy(alpha = 0.78f),
                    fontSize = 11.5.sp,
                    modifier = Modifier.clickable { showAppDrawer = true }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(pages.size) { idx ->
                        val active = pagerState.currentPage == idx
                        Box(
                            modifier = Modifier
                                .size(if (active) 7.dp else 5.dp)
                                .clip(CircleShape)
                                .background(if (active) accentColor else Color.White.copy(alpha = 0.35f))
                        )
                    }
                }
                Text(
                    text = "Axtarış",
                    color = ObsidianPalette.SoftWhite.copy(alpha = 0.78f),
                    fontSize = 11.5.sp,
                    modifier = Modifier.clickable { showSearchSheet = true }
                )
            }

            // Açıq fon üzərində də seçilən 4/5 ikonlu Şüşə Dock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1C2026).copy(alpha = 0.72f), Color(0xFF121418).copy(alpha = 0.84f))
                        )
                    )
                    .border(0.8.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dockApps.forEach { app ->
                        CentralDevAppIcon(
                            app = app,
                            iconStyle = settings.iconStyle,
                            accentColor = accentColor,
                            iconSize = settings.iconSizeDp.dp,
                            showLabel = false,
                            onClick = { appManager.launchApp(app) },
                            onLongClick = { contextApp = app }
                        )
                    }
                }
            }
        }

        // Tətbiqlər Siyahısı (App Drawer — yuxarı sürüşdürmə ilə açılır)
        AnimatedVisibility(
            visible = showAppDrawer,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AppDrawerOverlay(
                apps = visibleApps,
                settings = settings,
                onLaunchApp = { appManager.launchApp(it); showAppDrawer = false },
                onLongClickApp = { contextApp = it },
                onOpenHiddenVault = { showHiddenVault = true },
                onOpenSettings = { showSettings = true },
                onDismiss = { showAppDrawer = false }
            )
        }

        // Axtarış (Spotlight — aşağı sürüşdürmə ilə açılır, bir əllə əlçatan nəticələr)
        AnimatedVisibility(visible = showSearchSheet, enter = fadeIn(), exit = fadeOut()) {
            SpotlightSearchOverlay(
                apps = visibleApps,
                settings = settings,
                onLaunchApp = { appManager.launchApp(it); showSearchSheet = false },
                onLongClickApp = { contextApp = it },
                onDismiss = { showSearchSheet = false }
            )
        }

        // Qovluq pəncərəsi
        openedFolder?.let { folder ->
            val fApps = folder.itemKeys.mapNotNull { appByKey[it] }
            FolderModalDialog(
                folder = folder,
                apps = fApps,
                settings = settings,
                onRenameFolder = { newName ->
                    val updated = folders.map { if (it.id == folder.id) it.copy(name = newName) else it }
                    folders = updated
                    repo.saveFolders(updated)
                    openedFolder = updated.find { it.id == folder.id }
                },
                onRemoveAppFromFolder = { key ->
                    val updated = folders.map {
                        if (it.id == folder.id) it.copy(itemKeys = it.itemKeys.filterNot { k -> k == key }) else it
                    }.filter { it.itemKeys.isNotEmpty() }
                    folders = updated
                    repo.saveFolders(updated)
                    openedFolder = updated.find { it.id == folder.id }
                },
                onLaunchApp = { appManager.launchApp(it); openedFolder = null },
                onDismiss = { openedFolder = null }
            )
        }

        // Uzun basma menyusu
        contextApp?.let { target ->
            val isOnHome = homeOrder.isEmpty() || homeOrder.contains(target.componentKey) || dockKeys.contains(target.componentKey)
            AppContextMenuSheet(
                app = target,
                isOnHome = isOnHome,
                isLayoutLocked = settings.isLayoutLocked,
                folders = folders,
                onRename = { newName ->
                    appManager.saveCustomNameAndIcon(target, newName, target.customIconPath, target.useOriginalIcon)
                    reloadAll()
                },
                onIconPicked = { uri ->
                    val path = appManager.copyUriToInternalFile(uri, "app_icon")
                    if (path != null) {
                        appManager.saveCustomNameAndIcon(target, target.customLabel, path, false)
                        reloadAll()
                    }
                },
                onToggleOriginalIcon = { useOrig ->
                    appManager.saveCustomNameAndIcon(target, target.customLabel, if (useOrig) null else target.customIconPath, useOrig)
                    reloadAll()
                },
                onResetFactory = { appManager.resetToFactory(target); reloadAll() },
                onHideApp = { appManager.setHidden(target, true); reloadAll() },
                onToggleHomePresence = {
                    val base = if (homeOrder.isEmpty()) visibleApps.map { it.componentKey } else homeOrder
                    val next = if (base.contains(target.componentKey)) base.filterNot { it == target.componentKey } else base + target.componentKey
                    repo.saveHomeOrder(next)
                    reloadAll()
                },
                onMoveOrder = { delta ->
                    val base = (if (homeOrder.isEmpty()) visibleApps.map { it.componentKey } else homeOrder).toMutableList()
                    val idx = base.indexOf(target.componentKey)
                    val dst = idx + delta
                    if (idx in base.indices && dst in base.indices) {
                        val tmp = base[idx]; base[idx] = base[dst]; base[dst] = tmp
                        repo.saveHomeOrder(base)
                        homeOrder = base
                    }
                },
                onAddToFolder = { fid ->
                    val updated = folders.map {
                        if (it.id == fid && !it.itemKeys.contains(target.componentKey)) it.copy(itemKeys = it.itemKeys + target.componentKey) else it
                    }
                    folders = updated
                    repo.saveFolders(updated)
                },
                onCreateFolderWithApp = { fname ->
                    val created = FolderModel(id = System.currentTimeMillis().toString(), name = fname, itemKeys = listOf(target.componentKey))
                    val updated = folders + created
                    folders = updated
                    repo.saveFolders(updated)
                },
                onLaunchShortcut = { scId -> appManager.launchShortcut(target, scId) },
                onOpenAppInfo = { appManager.openAppInfo(target) },
                onUninstallApp = { appManager.uninstallApp(target) },
                onDismiss = { contextApp = null }
            )
        }

        if (showSettings) {
            CentralDevSettingsDialog(
                settings = settings,
                installedPackages = allApps.map { it.packageName }.toSet(),
                repo = repo,
                onSettingsChange = { updateSettings(it) },
                onOpenBatteryStudio = { showBatteryStudio = true },
                onOpenChargingGallery = { showChargingGallery = true },
                onOpenWallpaperStudio = { showWallpaperStudio = true },
                onOpenHiddenVault = { showHiddenVault = true },
                onBackupRestored = { reloadAll() },
                onDismiss = { showSettings = false }
            )
        }

        if (showBatteryStudio) {
            BatteryStudioSheet(
                config = settings.battery,
                realBatteryLevel = batterySnapshot.level,
                isPlugged = batterySnapshot.isPlugged,
                isFull = batterySnapshot.isFull,
                onConfigChange = { updateSettings(settings.copy(battery = it)) },
                onImportCustomPng = { uri, target ->
                    val path = appManager.copyUriToInternalFile(uri, "battery_$target")
                    if (path != null) {
                        val b = when (target) {
                            "ih_png" -> settings.battery.copy(symbolId = "ih_png", ihCustomPngPath = path)
                            "char_custom" -> settings.battery.copy(symbolId = "char_custom", characterCustomPngPath = path)
                            else -> settings.battery.copy(symbolId = "cd_png", customPngPath = path)
                        }
                        updateSettings(settings.copy(battery = b))
                    }
                },
                onDismiss = { showBatteryStudio = false }
            )
        }

        if (showChargingGallery) {
            ChargingCinemaGallerySheet(
                config = settings.charging,
                batteryConfig = settings.battery,
                realBatteryLevel = batterySnapshot.level,
                onConfigChange = { updateSettings(settings.copy(charging = it)) },
                onImportCustomVenom = { uri ->
                    val path = appManager.copyUriToInternalFile(uri, "venom_custom")
                    if (path != null) {
                        updateSettings(settings.copy(charging = settings.charging.copy(customVenomPngPath = path)))
                    }
                },
                onFullScreenPreview = { scene ->
                    showChargingGallery = false
                    showChargingCinema = scene
                },
                onDismiss = { showChargingGallery = false }
            )
        }

        if (showWallpaperStudio) {
            WallpaperStudioSheet(
                config = settings.wallpaper,
                onConfigChange = { updateSettings(settings.copy(wallpaper = it)) },
                onImportCustomImage = { uri, _ ->
                    val path = appManager.copyUriToInternalFile(uri, "wallpaper_home")
                    if (path != null) {
                        updateSettings(settings.copy(wallpaper = settings.wallpaper.copy(customHomePath = path)))
                    }
                },
                onDismiss = { showWallpaperStudio = false }
            )
        }

        if (showHiddenVault) {
            HiddenAppsVaultDialog(
                hiddenApps = hiddenApps,
                onVerifyPin = { repo.checkPin(it) },
                onChangePin = { repo.setPin(it) },
                onAppClick = { appManager.launchApp(it); showHiddenVault = false },
                onUnhideApp = { appManager.setHidden(it, false); reloadAll() },
                onDismiss = { showHiddenVault = false }
            )
        }

        showChargingCinema?.let { activeScene ->
            ChargingCinemaOverlay(
                sceneId = activeScene,
                config = settings.charging,
                batteryConfig = settings.battery,
                batteryLevel = batterySnapshot.level,
                isPlugged = batterySnapshot.isPlugged,
                onDismiss = { showChargingCinema = null }
            )
        }
    }
}

@Composable
private fun TopClockAndBatteryBar(
    settings: CentralDevSettings,
    battery: BatterySnapshot,
    onBatteryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var timeStr by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    var dateStr by remember { mutableStateOf(SimpleDateFormat("EEEE, d MMMM", Locale("az", "AZ")).format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            dateStr = SimpleDateFormat("EEEE, d MMMM", Locale("az", "AZ")).format(Date())
            delay(10000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = timeStr,
                    style = TextStyle(
                        color = ObsidianPalette.SoftWhite,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Light,
                        shadow = Shadow(Color.Black.copy(alpha = 0.6f), Offset(0f, 2f), 8f)
                    )
                )
                Text(
                    text = dateStr.replaceFirstChar { it.uppercase() },
                    style = TextStyle(
                        color = ObsidianPalette.SoftWhite.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        shadow = Shadow(Color.Black.copy(alpha = 0.6f), Offset(0f, 1f), 4f)
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CentralDevBatteryIndicator(
                    config = settings.battery,
                    batteryLevel = battery.level,
                    isPlugged = battery.isPlugged,
                    isFull = battery.isFull,
                    isWideWidget = settings.battery.placement == BatteryPlacement.WIDGET_WIDE,
                    onClick = onBatteryClick
                )
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1D22).copy(alpha = 0.72f))
                        .border(0.6.dp, ObsidianPalette.SubtleBorder, CircleShape)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = ObsidianPalette.SoftWhite, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun FolderIconItem(
    folder: FolderModel,
    apps: List<AppModel>,
    iconStyle: IconVisualStyle,
    accentColor: Color,
    iconSize: androidx.compose.ui.unit.Dp,
    showLabel: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(RoundedCornerShape(iconSize * 0.24f))
                .background(Color(0xFF1E2229).copy(alpha = 0.82f))
                .border(0.8.dp, ObsidianPalette.SubtleBorder, RoundedCornerShape(iconSize * 0.24f))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    apps.take(2).forEach {
                        Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(accentColor.copy(alpha = 0.75f)))
                    }
                }
                if (apps.size > 2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        apps.drop(2).take(2).forEach {
                            Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.55f)))
                        }
                    }
                }
            }
        }
        if (showLabel) {
            Spacer(Modifier.height(5.dp))
            Text(
                text = folder.name,
                color = ObsidianPalette.SoftWhite,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FolderModalDialog(
    folder: FolderModel,
    apps: List<AppModel>,
    settings: CentralDevSettings,
    onRenameFolder: (String) -> Unit,
    onRemoveAppFromFolder: (String) -> Unit,
    onLaunchApp: (AppModel) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(folder.id) { mutableStateOf(folder.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianPalette.GraphiteSurface,
        title = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; onRenameFolder(it) },
                singleLine = true,
                label = { Text("Qovluq adı") }
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(apps, key = { it.componentKey }) { app ->
                    CentralDevAppIcon(
                        app = app,
                        iconStyle = settings.iconStyle,
                        accentColor = Color(settings.accentColorHex),
                        iconSize = 52.dp,
                        showLabel = true,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onRemoveAppFromFolder(app.componentKey) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Bağla") }
        }
    )
}

@Composable
private fun AppDrawerOverlay(
    apps: List<AppModel>,
    settings: CentralDevSettings,
    onLaunchApp: (AppModel) -> Unit,
    onLongClickApp: (AppModel) -> Unit,
    onOpenHiddenVault: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ObsidianPalette.GraphiteBg.copy(alpha = 0.96f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bütün tətbiqlər (${apps.size})", color = ObsidianPalette.SoftWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = { onDismiss(); onOpenHiddenVault() }) {
                        Icon(Icons.Default.Lock, contentDescription = "Gizli tətbiqlər", tint = ObsidianPalette.SoftWhite)
                    }
                    IconButton(onClick = { onDismiss(); onOpenSettings() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = ObsidianPalette.SoftWhite)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Bağla", tint = ObsidianPalette.SoftWhite)
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(settings.gridPreset.cols),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
            ) {
                items(apps, key = { it.componentKey }) { app ->
                    CentralDevAppIcon(
                        app = app,
                        iconStyle = settings.iconStyle,
                        accentColor = Color(settings.accentColorHex),
                        iconSize = settings.iconSizeDp.dp,
                        showLabel = true,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onLongClickApp(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SpotlightSearchOverlay(
    apps: List<AppModel>,
    settings: CentralDevSettings,
    onLaunchApp: (AppModel) -> Unit,
    onLongClickApp: (AppModel) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val matches = remember(apps, query) {
        apps.filter { AppManager.matchesSearch(it, query) }
    }

    LaunchedEffect(Unit) {
        delay(80)
        runCatching { focusRequester.requestFocus() }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        color = ObsidianPalette.GraphiteBg.copy(alpha = 0.94f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Bottom // Bir əllə toxunmaq üçün nəticələr aşağıda əlçatan yerdə
        ) {
            if (matches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Uyğun tətbiq tapılmadı", color = ObsidianPalette.MutedText, fontSize = 14.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(settings.gridPreset.cols),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(matches, key = { it.componentKey }) { app ->
                        CentralDevAppIcon(
                            app = app,
                            iconStyle = settings.iconStyle,
                            accentColor = Color(settings.accentColorHex),
                            iconSize = settings.iconSizeDp.dp,
                            showLabel = true,
                            onClick = { onLaunchApp(app) },
                            onLongClick = { onLongClickApp(app) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianPalette.GraphiteCard)
                    .border(0.8.dp, ObsidianPalette.SubtleBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = ObsidianPalette.MutedText)
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = ObsidianPalette.SoftWhite, fontSize = 15.sp),
                    cursorBrush = SolidColor(Color(settings.accentColorHex)),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    decorationBox = { inner ->
                        if (query.isEmpty()) Text("Tətbiqlərdə axtar (İ/I dəstəkli)...", color = ObsidianPalette.MutedText, fontSize = 14.sp)
                        inner()
                    }
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Bağla", tint = ObsidianPalette.SoftWhite)
                }
            }
        }
    }
}
