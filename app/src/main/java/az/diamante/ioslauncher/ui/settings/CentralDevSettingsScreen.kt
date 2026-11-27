package az.diamante.ioslauncher.ui.settings

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import az.diamante.ioslauncher.data.*

/**
 * CentralDev Launcher Parametrləri:
 * 5 aydın qrup: Görünüş, Batareya və şarj, Divar kağızları, Tətbiqlər, Jestlər və ehtiyat nüsxə.
 */
@Composable
fun CentralDevSettingsDialog(
    settings: CentralDevSettings,
    installedPackages: Set<String>,
    repo: LauncherRepository,
    onSettingsChange: (CentralDevSettings) -> Unit,
    onOpenBatteryStudio: () -> Unit,
    onOpenChargingGallery: () -> Unit,
    onOpenWallpaperStudio: () -> Unit,
    onOpenHiddenVault: () -> Unit,
    onBackupRestored: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pendingRestoreReport by remember { mutableStateOf<BackupPreviewReport?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(repo.exportBackupJson().toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Ehtiyat nüsxə saxlanıldı", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "İxrac uğursuz oldu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
                val report = repo.analyzeBackupJson(json, installedPackages)
                if (report.valid) {
                    pendingRestoreReport = report
                } else {
                    Toast.makeText(context, "Ehtiyat nüsxə faylı oxunmadı", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val homeRoleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* Sistem seçimi tamamlandı */ }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ObsidianPalette.GraphiteBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CentralDev Ayarlar",
                        color = ObsidianPalette.SoftWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Bağla", tint = ObsidianPalette.SoftWhite)
                    }
                }

                // 1. GÖRÜNÜŞ
                SettingsGroupCard("Görünüş") {
                    Text("İkon materialı", color = ObsidianPalette.MutedText, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconVisualStyle.values().forEach { st ->
                            FilterChip(
                                selected = settings.iconStyle == st,
                                onClick = { onSettingsChange(settings.copy(iconStyle = st)) },
                                label = { Text(st.titleAz, fontSize = 11.5.sp) }
                            )
                        }
                    }

                    Text("Ana ekran toru", color = ObsidianPalette.MutedText, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GridPreset.values().forEach { gp ->
                            FilterChip(
                                selected = settings.gridPreset == gp,
                                onClick = { onSettingsChange(settings.copy(gridPreset = gp)) },
                                label = { Text(gp.label, fontSize = 12.sp) }
                            )
                        }
                    }

                    Text("İkon ölçüsü: ${settings.iconSizeDp} dp", color = ObsidianPalette.MutedText, fontSize = 12.sp)
                    Slider(
                        value = settings.iconSizeDp.toFloat(),
                        onValueChange = { onSettingsChange(settings.copy(iconSizeDp = it.toInt())) },
                        valueRange = 48f..66f
                    )

                    Text("Dock tutumu", color = ObsidianPalette.MutedText, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(4, 5).forEach { cap ->
                            FilterChip(
                                selected = settings.dockCapacity == cap,
                                onClick = { onSettingsChange(settings.copy(dockCapacity = cap)) },
                                label = { Text("$cap ikon") }
                            )
                        }
                    }

                    Text("Vurğu rəngi", color = ObsidianPalette.MutedText, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(0xFFE53935L, 0xFF007AFFL, 0xFF34C759L, 0xFFD4AF37L).forEach { hex ->
                            val selected = settings.accentColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .border(if (selected) 2.5.dp else 0.dp, Color.White, CircleShape)
                                    .clickable { onSettingsChange(settings.copy(accentColorHex = hex)) }
                            )
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("İkon adlarını göstər", color = ObsidianPalette.SoftWhite, fontSize = 14.sp)
                        Switch(checked = settings.showLabels, onCheckedChange = { onSettingsChange(settings.copy(showLabels = it)) })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Düzülüşü kilidlə", color = ObsidianPalette.SoftWhite, fontSize = 14.sp)
                        Switch(checked = settings.isLayoutLocked, onCheckedChange = { onSettingsChange(settings.copy(isLayoutLocked = it)) })
                    }
                }

                // 2. BATAREYA VƏ ŞARJ
                SettingsGroupCard("Batareya və şarj") {
                    SettingsActionRow("Batareya göstəricisi studiyası", settings.battery.symbolId) {
                        onDismiss(); onOpenBatteryStudio()
                    }
                    SettingsActionRow("Şarj animasiyası qalereyası", settings.charging.sceneId.titleAz) {
                        onDismiss(); onOpenChargingGallery()
                    }
                }

                // 3. DİVAR KAĞIZLARI
                SettingsGroupCard("Divar kağızları") {
                    SettingsActionRow("Fon studiyası (Ana / Kilid / 3D)", settings.wallpaper.motionType.titleAz) {
                        onDismiss(); onOpenWallpaperStudio()
                    }
                }

                // 4. TƏTBİQLƏR
                SettingsGroupCard("Tətbiqlər") {
                    SettingsActionRow("Gizli tətbiqlər bölməsi", "PIN qorumalı") {
                        onDismiss(); onOpenHiddenVault()
                    }
                    Button(
                        onClick = {
                            launchDefaultHomeChooser(context, homeRoleLauncher::launch)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ObsidianPalette.ControlledRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Standart ana ekran seç", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                // 5. JESTLƏR VƏ EHTİYAT NÜSXƏ
                SettingsGroupCard("Jestlər və ehtiyat nüsxə") {
                    Text("Boş sahəyə iki toxunuş", color = ObsidianPalette.MutedText, fontSize = 12.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DoubleTapAction.values().forEach { act ->
                            FilterChip(
                                selected = settings.doubleTapAction == act,
                                onClick = { onSettingsChange(settings.copy(doubleTapAction = act)) },
                                label = { Text(act.titleAz, fontSize = 12.sp) }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { exportLauncher.launch("centraldev_backup.json") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Nüsxə ixrac et", fontSize = 12.sp) }

                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Nüsxə bərpa et", fontSize = 12.sp) }
                    }
                }
            }
        }
    }

    // Bərpadan öncə dəyişikliklər və çatışmayan tətbiqlər haqqında hesabat
    pendingRestoreReport?.let { rep ->
        AlertDialog(
            onDismissRequest = { pendingRestoreReport = null },
            containerColor = ObsidianPalette.GraphiteSurface,
            title = { Text("Ehtiyat nüsxə hesabatı", color = ObsidianPalette.SoftWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Fərdi tətbiq sayı: ${rep.totalCustomApps}", color = ObsidianPalette.SoftWhite, fontSize = 13.sp)
                    Text("Qovluq sayı: ${rep.folderCount}", color = ObsidianPalette.SoftWhite, fontSize = 13.sp)
                    if (rep.missingPackages.isNotEmpty()) {
                        Text(
                            "Bu telefonda tapılmayan tətbiqlər (${rep.missingPackages.size}): ${rep.missingPackages.take(4).joinToString()}",
                            color = ObsidianPalette.ControlledRed,
                            fontSize = 12.sp
                        )
                    }
                    if (rep.missingIconFiles.isNotEmpty()) {
                        Text(
                            "Çatışmayan fərdi ikon faylları: ${rep.missingIconFiles.size}",
                            color = Color(0xFFFFB142),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (repo.applyBackupJson(rep.rawJson)) {
                        onBackupRestored()
                        Toast.makeText(context, "Ehtiyat nüsxə bərpa olundu", Toast.LENGTH_SHORT).show()
                    }
                    pendingRestoreReport = null
                }) { Text("Bərpa et") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreReport = null }) { Text("Ləğv et") }
            }
        )
    }
}

@Composable
private fun SettingsGroupCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ObsidianPalette.GraphiteSurface)
            .border(0.6.dp, ObsidianPalette.SubtleBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = {
            Text(title, color = ObsidianPalette.SoftWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            content()
        }
    )
}

@Composable
private fun SettingsActionRow(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianPalette.GraphiteCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = ObsidianPalette.SoftWhite, fontSize = 14.sp)
        Text(value, color = ObsidianPalette.MutedText, fontSize = 12.sp)
    }
}

fun launchDefaultHomeChooser(context: Context, launchForResult: (Intent) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val rm = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
        if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME)) {
            launchForResult(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
            return
        }
    }
    runCatching {
        context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.onFailure {
        Toast.makeText(context, "Ayarlar > Standart Tətbiqlər > Ana Ekran bölməsindən seçin", Toast.LENGTH_LONG).show()
    }
}
