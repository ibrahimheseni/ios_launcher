package az.diamante.ioslauncher.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.diamante.ioslauncher.data.FolderModel
import az.diamante.ioslauncher.data.ObsidianPalette
import az.diamante.ioslauncher.model.AppModel
import coil.compose.AsyncImage
import java.io.File

/**
 * Tətbiqə uzun basdıqda açılan Azərbaycan dilində tam əməliyyat menyusu:
 * Adını dəyiş, İkonu dəyiş (kəsmə/miqyas önizləməsi), Gizlət, Ana ekrana əlavə et/götür,
 * Qısayollar, Qovluğa əlavə et, Tətbiq məlumatı, Zavod görünüşünə qaytar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContextMenuSheet(
    app: AppModel,
    isOnHome: Boolean,
    isLayoutLocked: Boolean,
    folders: List<FolderModel>,
    onRename: (String?) -> Unit,
    onIconPicked: (Uri) -> Unit,
    onToggleOriginalIcon: (Boolean) -> Unit,
    onResetFactory: () -> Unit,
    onHideApp: () -> Unit,
    onToggleHomePresence: () -> Unit,
    onMoveOrder: (Int) -> Unit,
    onAddToFolder: (String) -> Unit,
    onCreateFolderWithApp: (String) -> Unit,
    onLaunchShortcut: (String) -> Unit,
    onOpenAppInfo: () -> Unit,
    onUninstallApp: () -> Unit,
    onDismiss: () -> Unit
) {
    var nameInput by remember(app.componentKey) { mutableStateOf(app.displayLabel) }
    var newFolderName by remember { mutableStateOf("") }
    var iconPreviewScale by remember { mutableFloatStateOf(1.0f) }

    val iconPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onIconPicked(uri)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ObsidianPalette.GraphiteSurface,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(app.displayLabel, color = ObsidianPalette.SoftWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (app.customLabel != null) {
                Text("Orijinal ad: ${app.originalLabel}", color = ObsidianPalette.MutedText, fontSize = 12.sp)
            }

            // Tətbiqin öz qısayolları (shortcuts)
            if (app.shortcuts.isNotEmpty()) {
                Text("Tətbiq qısayolları", color = ObsidianPalette.MutedText, fontSize = 12.sp)
                app.shortcuts.forEach { sc ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ObsidianPalette.GraphiteCard)
                            .clickable { onLaunchShortcut(sc.id); onDismiss() }
                            .padding(12.dp)
                    ) {
                        Text(sc.label, color = ObsidianPalette.SoftWhite, fontSize = 13.5.sp)
                    }
                }
            }

            // Adını dəyiş
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Adını dəyiş") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onRename(nameInput); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianPalette.ControlledRed),
                    modifier = Modifier.weight(1f)
                ) { Text("Adı saxla") }

                if (app.customLabel != null) {
                    OutlinedButton(
                        onClick = { onRename(null); onDismiss() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Orijinal adı qaytar", fontSize = 12.sp) }
                }
            }

            // İkonu dəyiş & miqyas önizləməsi
            if (app.customIconPath != null && File(app.customIconPath).exists()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Transparent)
                            .border(0.8.dp, ObsidianPalette.SubtleBorder, RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = File(app.customIconPath),
                            contentDescription = "İkon önizləməsi",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Kəsmə / Miqyas önizləməsi", color = ObsidianPalette.MutedText, fontSize = 11.5.sp)
                        Slider(value = iconPreviewScale, onValueChange = { iconPreviewScale = it }, valueRange = 0.8f..1.3f)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { iconPicker.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) { Text("İkonu dəyiş", fontSize = 12.sp) }

                OutlinedButton(
                    onClick = { onToggleOriginalIcon(!app.useOriginalIcon); onDismiss() },
                    modifier = Modifier.weight(1f)
                ) { Text(if (app.useOriginalIcon) "Tema ikonu" else "Orijinal ikon", fontSize = 12.sp) }
            }

            if (app.customLabel != null || app.customIconPath != null || app.useOriginalIcon) {
                TextButton(onClick = { onResetFactory(); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Zavod görünüşünə qaytar", color = ObsidianPalette.MutedText)
                }
            }

            // Ana ekrana əlavə et / götür və yerini dəyiş
            if (!isLayoutLocked) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onMoveOrder(-1) }, modifier = Modifier.weight(1f)) { Text("⬅️ Sola") }
                    OutlinedButton(onClick = { onMoveOrder(1) }, modifier = Modifier.weight(1f)) { Text("Sağa ➡️") }
                }
            }
            OutlinedButton(
                onClick = { onToggleHomePresence(); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isOnHome) "Ana ekrandan götür" else "Ana ekrana əlavə et")
            }

            // Qovluğa əlavə et / yarat
            if (folders.isNotEmpty()) {
                Text("Qovluğa əlavə et", color = ObsidianPalette.MutedText, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    folders.forEach { f ->
                        FilterChip(
                            selected = f.itemKeys.contains(app.componentKey),
                            onClick = { onAddToFolder(f.id); onDismiss() },
                            label = { Text(f.name, fontSize = 12.sp) }
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    placeholder = { Text("Yeni qovluq adı", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onCreateFolderWithApp(newFolderName.trim())
                            onDismiss()
                        }
                    }
                ) { Text("Yarat") }
            }

            // Gizlət + Düzgün izah
            Button(
                onClick = { onHideApp(); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianPalette.GraphiteCard),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Gizlət", color = ObsidianPalette.SoftWhite) }
            Text(
                text = "Bu seçim tətbiqi CentralDev Launcher-də gizlədir. Android ayarlarında görünə bilər.",
                color = ObsidianPalette.MutedText,
                fontSize = 11.sp
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onOpenAppInfo(); onDismiss() },
                    modifier = Modifier.weight(1f)
                ) { Text("Tətbiq məlumatı", fontSize = 12.sp) }

                Button(
                    onClick = { onUninstallApp(); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianPalette.ControlledRed),
                    modifier = Modifier.weight(1f)
                ) { Text("Sil", color = Color.White, fontSize = 12.sp) }
            }
        }
    }
}
