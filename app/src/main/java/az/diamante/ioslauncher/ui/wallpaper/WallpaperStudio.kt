package az.diamante.ioslauncher.ui.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.LinearGradient as AndroidLinearGradient
import android.graphics.Shader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import az.diamante.ioslauncher.data.ObsidianPalette
import az.diamante.ioslauncher.data.WallpaperConfig
import az.diamante.ioslauncher.data.WallpaperMotionType
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

data class WallpaperPairDef(
    val id: String,
    val titleAz: String,
    val topColor: Color,
    val midColor: Color,
    val bottomColor: Color,
    val accentColor: Color
)

val WALLPAPER_PAIRS = listOf(
    WallpaperPairDef("cd_obsidian", "CentralDev Obsidian", Color(0xFF0E1014), Color(0xFF161920), Color(0xFF1F1619), Color(0xFFE53935)),
    WallpaperPairDef("tesla_studio", "Tesla Model S Studio", Color(0xFF0A0C10), Color(0xFF151922), Color(0xFF231317), Color(0xFFE53935)),
    WallpaperPairDef("baku_night", "Bakı gecəsi", Color(0xFF070B18), Color(0xFF0F1B33), Color(0xFF10243E), Color(0xFFF59E0B)),
    WallpaperPairDef("az_heritage", "Azərbaycan irsi", Color(0xFF09131D), Color(0xFF14212B), Color(0xFF13261D), Color(0xFFD4AF37)),
    WallpaperPairDef("mil_titan", "Hərbi Titan", Color(0xFF0F1410), Color(0xFF1B241B), Color(0xFF242E22), Color(0xFF84A98C)),
    WallpaperPairDef("liquid_glass", "Liquid Glass", Color(0xFF0E1624), Color(0xFF1B2B44), Color(0xFF283C5A), Color(0xFF60A5FA))
)

/**
 * Ana ekran və Kilid ekranı üçün Parallax + Real vaxt 3D ("İH" titan monoqramı) mühərriki.
 * Görünməyəndə sensor dinləməsini və render dövrünü avtomatik dayandırır.
 */
@Composable
fun CentralDevWallpaperCanvas(
    config: WallpaperConfig,
    pageOffsetRatio: Float = 0f,
    isHomeVisible: Boolean = true,
    isPowerSaveMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isResumed by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isResumed = event == Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val shouldRenderActive = isHomeVisible && isResumed && !isPowerSaveMode
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }
    var touchYaw by remember { mutableFloatStateOf(0f) }
    var lightPhase by remember { mutableFloatStateOf(0f) }

    // Parallaks və 3D üçün sensor dinləməsi — yalnız görünəndə aktiv olur
    DisposableEffect(config.motionType, shouldRenderActive) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accel = sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(ev: SensorEvent) {
                tiltX = (tiltX * 0.85f) + ((-ev.values[0] / 9.8f).coerceIn(-1f, 1f) * 0.15f)
                tiltY = (tiltY * 0.85f) + ((ev.values[1] / 9.8f).coerceIn(-1f, 1f) * 0.15f)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (shouldRenderActive && accel != null &&
            (config.motionType == WallpaperMotionType.PARALLAX || config.motionType == WallpaperMotionType.REALTIME_3D)
        ) {
            sm.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose {
            sm?.unregisterListener(listener)
        }
    }

    // Real vaxt 3D işıq/fırlanma dövrü — görünməyəndə dayanır
    LaunchedEffect(config.motionType, shouldRenderActive) {
        if (shouldRenderActive && config.motionType == WallpaperMotionType.REALTIME_3D) {
            while (true) {
                lightPhase = (lightPhase + 0.025f) % (Math.PI.toFloat() * 2f)
                delay(32)
            }
        }
    }

    val pair = remember(config.pairId) {
        WALLPAPER_PAIRS.find { it.id == config.pairId } ?: WALLPAPER_PAIRS.first()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (config.motionType == WallpaperMotionType.REALTIME_3D) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            touchYaw = (touchYaw + dragAmount.x * 0.008f).coerceIn(-1.2f, 1.2f)
                        }
                    }
                } else Modifier
            )
    ) {
        if (config.customHomePath != null && File(config.customHomePath).exists()) {
            AsyncImage(
                model = File(config.customHomePath),
                contentDescription = "Ana ekran fonu",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.22f)))
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val px = if (config.motionType == WallpaperMotionType.PARALLAX && !isPowerSaveMode) tiltX * 26f - pageOffsetRatio * 20f else 0f
                val py = if (config.motionType == WallpaperMotionType.PARALLAX && !isPowerSaveMode) tiltY * 20f else 0f

                // Arxa qat
                drawRect(
                    brush = Brush.verticalGradient(listOf(pair.topColor, pair.midColor, pair.bottomColor))
                )

                // Orta qat (parallaks ilə cüzi hərəkət edir, mərkəzi obyekt sahəsi qorunur)
                drawPairedHeroMotif(
                    pair = pair,
                    isLockScreen = false,
                    offsetX = px * 0.6f,
                    offsetY = py * 0.6f
                )

                // Real vaxt 3D: Qara şüşə üzərində fırlanan titan "İH" monoqramı
                if (config.motionType == WallpaperMotionType.REALTIME_3D && !isPowerSaveMode) {
                    drawRealtime3dIhMonogram(
                        angle = lightPhase + touchYaw + tiltX * 0.6f,
                        tiltY = tiltY,
                        accent = pair.accentColor
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawPairedHeroMotif(
    pair: WallpaperPairDef,
    isLockScreen: Boolean,
    offsetX: Float,
    offsetY: Float
) {
    // Ana ekranda obyekt sağ-aşağıya çəkilir ki, solda ikonlar üçün sakit sahə qalsın;
    // Kilid ekranında obyekt daha iri və aşağıda (saatın altında) yerləşir.
    val cx = (if (isLockScreen) size.width * 0.50f else size.width * 0.68f) + offsetX
    val cy = (if (isLockScreen) size.height * 0.66f else size.height * 0.56f) + offsetY
    val scale = if (isLockScreen) 1.25f else 0.92f

    when (pair.id) {
        "cd_obsidian" -> {
            drawCircle(
                brush = Brush.radialGradient(listOf(pair.accentColor.copy(alpha = 0.20f), Color.Transparent)),
                radius = size.minDimension * 0.42f * scale,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = pair.accentColor.copy(alpha = 0.28f),
                radius = size.minDimension * 0.22f * scale,
                center = Offset(cx, cy),
                style = Stroke(2f)
            )
        }
        "tesla_studio" -> {
            val w = size.minDimension * 0.44f * scale
            val h = w * 0.42f
            val body = Path().apply {
                moveTo(cx - w * 0.48f, cy + h * 0.1f)
                cubicTo(cx - w * 0.4f, cy - h * 0.4f, cx + w * 0.4f, cy - h * 0.4f, cx + w * 0.48f, cy + h * 0.1f)
                lineTo(cx + w * 0.4f, cy + h * 0.4f)
                lineTo(cx - w * 0.4f, cy + h * 0.4f)
                close()
            }
            drawPath(body, Color(0xFF1A1E27))
            drawLine(pair.accentColor.copy(alpha = 0.7f), Offset(cx - w * 0.36f, cy), Offset(cx - w * 0.2f, cy - h * 0.06f), 3f)
            drawLine(pair.accentColor.copy(alpha = 0.7f), Offset(cx + w * 0.36f, cy), Offset(cx + w * 0.2f, cy - h * 0.06f), 3f)
        }
        "baku_night" -> {
            val horizon = cy + size.height * 0.12f
            for (i in -1..1) {
                val tx = cx + i * 38f * scale
                drawRect(Color(0xFF1E293B), Offset(tx - 10f * scale, horizon - 90f * scale), Size(20f * scale, 90f * scale))
                drawLine(Color(0xFFF59E0B).copy(alpha = 0.45f), Offset(tx, horizon - 90f * scale), Offset(tx, horizon), 2f)
            }
        }
        "az_heritage" -> {
            drawCircle(Color(0xFF00A3E0).copy(alpha = 0.16f), size.minDimension * 0.30f * scale, Offset(cx, cy - 20f))
            drawCircle(Color(0xFFEF3340).copy(alpha = 0.14f), size.minDimension * 0.22f * scale, Offset(cx, cy))
            drawCircle(Color(0xFF239F40).copy(alpha = 0.14f), size.minDimension * 0.16f * scale, Offset(cx, cy + 20f))
        }
        "mil_titan" -> {
            val w = size.minDimension * 0.42f * scale
            drawRoundRect(Color(0xFF2D3A2D), Offset(cx - w / 2, cy), Size(w, w * 0.24f), CornerRadius(14f))
            drawRoundRect(Color(0xFF3E4E3D), Offset(cx - w * 0.28f, cy - w * 0.16f), Size(w * 0.5f, w * 0.18f), CornerRadius(10f))
        }
        "liquid_glass" -> {
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFF60A5FA).copy(alpha = 0.32f), Color.Transparent)),
                radius = size.minDimension * 0.36f * scale,
                center = Offset(cx, cy)
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.16f), Color.Transparent)),
                radius = size.minDimension * 0.20f * scale,
                center = Offset(cx - 40f, cy - 50f)
            )
        }
    }
}

private fun DrawScope.drawRealtime3dIhMonogram(angle: Float, tiltY: Float, accent: Color) {
    val cx = size.width / 2f
    val cy = size.height * 0.44f + tiltY * 18f
    val cosA = cos(angle)
    val sinA = sin(angle)
    val cardW = size.minDimension * 0.34f * cosA.let { kotlin.math.abs(it).coerceAtLeast(0.18f) }
    val cardH = size.minDimension * 0.34f

    // Qara şüşə əsas + yan qalınlıq (3D dərinlik)
    val thickness = sinA * 14f
    drawRoundRect(
        color = Color(0xFF0A0C0F),
        topLeft = Offset(cx - cardW / 2 + thickness, cy - cardH / 2),
        size = Size(cardW, cardH),
        cornerRadius = CornerRadius(24f)
    )
    drawRoundRect(
        brush = Brush.linearGradient(
            listOf(Color(0xFF323842), Color(0xFF14171D), Color(0xFF262B34))
        ),
        topLeft = Offset(cx - cardW / 2, cy - cardH / 2),
        size = Size(cardW, cardH),
        cornerRadius = CornerRadius(24f)
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.32f),
        topLeft = Offset(cx - cardW / 2, cy - cardH / 2),
        size = Size(cardW, cardH),
        cornerRadius = CornerRadius(24f),
        style = Stroke(2f)
    )

    // Titan "İH" qalın konturu (3D perspektiv miqyası ilə)
    val scaleX = kotlin.math.abs(cosA).coerceAtLeast(0.18f)
    val u = size.minDimension * 0.032f
    drawLine(Color(0xFFE5E7EB), Offset(cx - 1.8f * u * scaleX, cy - 1.2f * u), Offset(cx - 1.8f * u * scaleX, cy + 1.2f * u), 5f * scaleX, StrokeCap.Round)
    drawCircle(accent, 3.5f * scaleX, Offset(cx - 1.8f * u * scaleX, cy - 2f * u))
    drawLine(Color(0xFFE5E7EB), Offset(cx + 0.5f * u * scaleX, cy - 1.3f * u), Offset(cx + 0.5f * u * scaleX, cy + 1.3f * u), 5f * scaleX, StrokeCap.Round)
    drawLine(Color(0xFFE5E7EB), Offset(cx + 2.2f * u * scaleX, cy - 1.3f * u), Offset(cx + 2.2f * u * scaleX, cy + 1.3f * u), 5f * scaleX, StrokeCap.Round)
    drawLine(Color(0xFFE5E7EB), Offset(cx + 0.5f * u * scaleX, cy), Offset(cx + 2.2f * u * scaleX, cy), 4.5f, StrokeCap.Round)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperStudioSheet(
    config: WallpaperConfig,
    onConfigChange: (WallpaperConfig) -> Unit,
    onImportCustomImage: (Uri, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cfg = LocalConfiguration.current
    val dm = context.resources.displayMetrics
    val realWidth = dm.widthPixels
    val realHeight = dm.heightPixels
    var previewAsLockScreen by remember { mutableStateOf(false) }

    val homePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImportCustomImage(uri, false)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Fon studiyası", color = ObsidianPalette.SoftWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Text("Faktiki ekran ölçünüz: ${realWidth} × ${realHeight} px", color = ObsidianPalette.MutedText, fontSize = 12.sp)

            // Real nisbətli önizləmə (Ana ekran vs Kilid ekranı təhlükəsiz sahə)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !previewAsLockScreen, onClick = { previewAsLockScreen = false }, label = { Text("Ana ekran önizləmə") })
                FilterChip(selected = previewAsLockScreen, onClick = { previewAsLockScreen = true }, label = { Text("Kilid ekranı önizləmə") })
            }

            val selectedPair = WALLPAPER_PAIRS.find { it.id == config.pairId } ?: WALLPAPER_PAIRS.first()
            val aspectRatio = (cfg.screenWidthDp.toFloat() / cfg.screenHeightDp.coerceAtLeast(1).toFloat()).coerceIn(0.4f, 0.65f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(aspectRatio)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, ObsidianPalette.SubtleBorder, RoundedCornerShape(18.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(Brush.verticalGradient(listOf(selectedPair.topColor, selectedPair.midColor, selectedPair.bottomColor)))
                        drawPairedHeroMotif(selectedPair, previewAsLockScreen, 0f, 0f)
                    }
                    if (previewAsLockScreen) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp)
                        ) {
                            Text("09:41", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text("Saat sahəsi (təxmini)", color = Color.White.copy(alpha = 0.65f), fontSize = 9.sp)
                        }
                    } else {
                        // Ana ekran ikon düzülüşü önizləməsi
                        Column(
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                repeat(4) {
                                    Box(Modifier.size(16.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.28f)))
                                }
                            }
                            Box(
                                Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.2f))
                            )
                        }
                    }
                }
            }

            // 6 uyğun fon cütü
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(210.dp)
            ) {
                items(WALLPAPER_PAIRS, key = { it.id }) { pair ->
                    val isSelected = config.pairId == pair.id && config.customHomePath == null
                    Box(
                        modifier = Modifier
                            .height(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(pair.topColor, pair.accentColor.copy(alpha = 0.45f))))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) ObsidianPalette.ControlledRed else ObsidianPalette.SubtleBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                onConfigChange(config.copy(pairId = pair.id, customHomePath = null))
                            }
                            .padding(10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(pair.titleAz, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Hərəkət növü
            Text("Hərəkət növü", color = ObsidianPalette.SoftWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(WallpaperMotionType.STATIC, WallpaperMotionType.PARALLAX, WallpaperMotionType.REALTIME_3D).forEach { mt ->
                    FilterChip(
                        selected = config.motionType == mt,
                        onClick = { onConfigChange(config.copy(motionType = mt)) },
                        label = { Text(mt.titleAz, fontSize = 11.sp) }
                    )
                }
            }

            OutlinedButton(
                onClick = { homePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Qalereyadan fərdi fon seç")
            }

            // Sistemə tətbiq düymələri (Ana ekran, Kilid ekranı, Hər ikisi)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        applyWallpaperToSystem(context, selectedPair, realWidth, realHeight, WallpaperManager.FLAG_SYSTEM)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianPalette.GraphiteCard)
                ) { Text("Ana ekran", fontSize = 11.5.sp) }

                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            applyWallpaperToSystem(context, selectedPair, realWidth, realHeight, WallpaperManager.FLAG_LOCK)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianPalette.GraphiteCard)
                ) { Text("Kilid ekranı", fontSize = 11.5.sp) }

                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            applyWallpaperToSystem(
                                context,
                                selectedPair,
                                realWidth,
                                realHeight,
                                WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianPalette.ControlledRed)
                ) { Text("Hər ikisi", fontSize = 11.5.sp) }
            }
        }
    }
}

private fun applyWallpaperToSystem(
    context: Context,
    pair: WallpaperPairDef,
    width: Int,
    height: Int,
    whichFlags: Int
) {
    runCatching {
        val wm = WallpaperManager.getInstance(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !wm.isSetWallpaperAllowed) {
            Toast.makeText(context, "Bu cihazda divar kağızı dəyişdirilməsinə icazə verilmir.", Toast.LENGTH_SHORT).show()
            return
        }
        val bmp = Bitmap.createBitmap(width.coerceIn(720, 1440), height.coerceIn(1280, 3200), Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bmp)
        val paint = Paint().apply {
            shader = AndroidLinearGradient(
                0f, 0f, 0f, bmp.height.toFloat(),
                intArrayOf(pair.topColor.toArgbInt(), pair.midColor.toArgbInt(), pair.bottomColor.toArgbInt()),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat(), paint)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            wm.setBitmap(bmp, null, true, whichFlags)
        } else {
            wm.setBitmap(bmp)
        }
        Toast.makeText(context, "${pair.titleAz} tətbiq olundu", Toast.LENGTH_SHORT).show()
    }.onFailure {
        Toast.makeText(context, "Divar kağızı tətbiq edilə bilmədi", Toast.LENGTH_SHORT).show()
    }
}

private fun Color.toArgbInt(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}
