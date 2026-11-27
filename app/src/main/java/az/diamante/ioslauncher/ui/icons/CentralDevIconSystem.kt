package az.diamante.ioslauncher.ui.icons

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.diamante.ioslauncher.data.IconVisualStyle
import az.diamante.ioslauncher.model.AppCategory
import az.diamante.ioslauncher.model.AppModel
import coil.compose.AsyncImage
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

/**
 * CentralDev 4-rejimli optik balanslı ikon sistemi (Açıq, Tünd, Şəffaf, Rəng tonlu).
 * Hər xüsusi ikon: əsas səth + optik balanslı simvol + kənarda incə işıq qatından ibarətdir.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CentralDevAppIcon(
    app: AppModel,
    iconStyle: IconVisualStyle,
    accentColor: Color,
    iconSize: Dp = 58.dp,
    showLabel: Boolean = true,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = iconSize * 0.24f
    val shape = RoundedCornerShape(cornerRadius)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .semantics { contentDescription = app.displayLabel }
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .shadow(
                    elevation = if (iconStyle == IconVisualStyle.CLEAR) 3.dp else 7.dp,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.45f),
                    spotColor = Color.Black.copy(alpha = 0.55f)
                )
                .clip(shape)
                .then(
                    if (isSelected) Modifier.border(1.5.dp, accentColor, shape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                app.customIconPath != null && File(app.customIconPath).exists() -> {
                    AsyncImage(
                        model = File(app.customIconPath),
                        contentDescription = app.displayLabel,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                !app.useOriginalIcon && app.category != AppCategory.OTHER -> {
                    CentralDevVectorIconSurface(
                        category = app.category,
                        style = iconStyle,
                        accentColor = accentColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    val bmp = remember(app.componentKey) { drawableToBitmap(app.originalIcon) }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = app.displayLabel,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // Şəffaf və ya Rəng tonlu rejimdə orijinal ikon üzərinə incə material qatı
                    if (iconStyle == IconVisualStyle.TINTED) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(accentColor.copy(alpha = 0.24f))
                        )
                    }
                }
            }

            // İncə kənar işıq qatı (Rim Light)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 0.75.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (iconStyle == IconVisualStyle.LIGHT) 0.7f else 0.28f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        ),
                        shape = shape
                    )
            )

            // İş profili nişanı
            if (app.isWorkProfile) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(3.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = app.displayLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    color = Color(0xFFF4F6F8),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.85f),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 5f
                    )
                ),
                modifier = Modifier.widthIn(max = iconSize + 18.dp)
            )
        }
    }
}

@Composable
fun CentralDevVectorIconSurface(
    category: AppCategory,
    style: IconVisualStyle,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val categoryColor = remember(category) {
        when (category) {
            AppCategory.PHONE -> Color(0xFF2ECC71)
            AppCategory.MESSAGES -> Color(0xFF27AE60)
            AppCategory.CAMERA -> Color(0xFF636E72)
            AppCategory.GALLERY -> Color(0xFF00A8FF)
            AppCategory.BROWSER -> Color(0xFF0984E3)
            AppCategory.CALENDAR -> Color(0xFFE53935)
            AppCategory.CLOCK -> Color(0xFF1E272E)
            AppCategory.CALCULATOR -> Color(0xFFFF9F1A)
            AppCategory.CONTACTS -> Color(0xFF8395A7)
            AppCategory.FILES -> Color(0xFF3498DB)
            AppCategory.MUSIC -> Color(0xFFE84118)
            AppCategory.MAIL -> Color(0xFF1B9CFC)
            AppCategory.SETTINGS -> Color(0xFF57606F)
            AppCategory.OTHER -> Color(0xFF2F3640)
        }
    }

    val bgBrush = when (style) {
        IconVisualStyle.LIGHT -> Brush.linearGradient(
            listOf(Color(0xFFF5F7FA), Color(0xFFE1E6EE))
        )
        IconVisualStyle.DARK -> Brush.linearGradient(
            colors = listOf(Color(0xFF23272F), Color(0xFF14171C)),
            start = Offset(0f, 0f),
            end = Offset(140f, 140f)
        )
        IconVisualStyle.CLEAR -> Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.22f), Color.Black.copy(alpha = 0.28f))
        )
        IconVisualStyle.TINTED -> Brush.linearGradient(
            listOf(accentColor.copy(alpha = 0.92f), accentColor.copy(alpha = 0.62f))
        )
    }

    val fgColor = when (style) {
        IconVisualStyle.LIGHT -> categoryColor
        IconVisualStyle.DARK -> categoryColor
        IconVisualStyle.CLEAR -> Color.White
        IconVisualStyle.TINTED -> Color.White
    }

    Box(modifier = modifier.background(bgBrush)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Yandan gələn zəif işıq (Tünd rejimdə)
            if (style == IconVisualStyle.DARK) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(categoryColor.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width * 0.22f, size.height * 0.22f),
                        radius = size.width * 0.75f
                    )
                )
            }
            drawCategoryGlyph(category, fgColor, style)
        }
    }
}

private fun DrawScope.drawCategoryGlyph(
    category: AppCategory,
    fg: Color,
    style: IconVisualStyle
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val u = size.minDimension / 24f // 24-vahidli optik koordinat toru

    when (category) {
        AppCategory.PHONE -> {
            val path = Path().apply {
                moveTo(cx - 5f * u, cy - 6.5f * u)
                cubicTo(cx - 7f * u, cy - 3f * u, cx - 3f * u, cy + 5f * u, cx + 5.5f * u, cy + 6.5f * u)
                lineTo(cx + 7f * u, cy + 3.5f * u)
                lineTo(cx + 3.5f * u, cy + 1.8f * u)
                lineTo(cx + 1.5f * u, cy + 3f * u)
                cubicTo(cx - 1f * u, cy + 1.8f * u, cx - 2.2f * u, cy - 0.5f * u, cx - 2.8f * u, cy - 2.5f * u)
                lineTo(cx - 1.5f * u, cy - 4.2f * u)
                lineTo(cx - 3.2f * u, cy - 7f * u)
                close()
            }
            drawPath(path, fg)
        }
        AppCategory.MESSAGES -> {
            drawOval(fg, topLeft = Offset(cx - 7f * u, cy - 5.5f * u), size = Size(14f * u, 10.5f * u))
            val tail = Path().apply {
                moveTo(cx - 4f * u, cy + 3.5f * u)
                lineTo(cx - 6.5f * u, cy + 7f * u)
                lineTo(cx - 1f * u, cy + 4.5f * u)
                close()
            }
            drawPath(tail, fg)
        }
        AppCategory.CAMERA -> {
            drawRoundRect(
                color = fg,
                topLeft = Offset(cx - 7.5f * u, cy - 5f * u),
                size = Size(15f * u, 10.5f * u),
                cornerRadius = CornerRadius(2.4f * u, 2.4f * u),
                style = Stroke(width = 1.8f * u)
            )
            drawCircle(fg, radius = 3.2f * u, center = Offset(cx, cy + 0.3f * u), style = Stroke(width = 1.6f * u))
            drawCircle(fg, radius = 1.1f * u, center = Offset(cx, cy + 0.3f * u))
        }
        AppCategory.GALLERY -> {
            val petalColors = if (style == IconVisualStyle.CLEAR || style == IconVisualStyle.TINTED) {
                listOf(fg.copy(alpha = 0.9f), fg.copy(alpha = 0.65f), fg.copy(alpha = 0.8f), fg.copy(alpha = 0.55f))
            } else {
                listOf(Color(0xFFFF5252), Color(0xFFFFB142), Color(0xFF2ED573), Color(0xFF1E90FF))
            }
            val offsets = listOf(Offset(0f, -3.4f * u), Offset(3.4f * u, 0f), Offset(0f, 3.4f * u), Offset(-3.4f * u, 0f))
            offsets.forEachIndexed { i, off ->
                drawCircle(petalColors[i], radius = 3.1f * u, center = Offset(cx + off.x, cy + off.y))
            }
        }
        AppCategory.BROWSER -> {
            drawCircle(fg, radius = 7.2f * u, center = Offset(cx, cy), style = Stroke(width = 1.7f * u))
            val needle = Path().apply {
                moveTo(cx + 3.6f * u, cy - 3.6f * u)
                lineTo(cx + 1.2f * u, cy + 1.2f * u)
                lineTo(cx - 3.6f * u, cy + 3.6f * u)
                lineTo(cx - 1.2f * u, cy - 1.2f * u)
                close()
            }
            drawPath(needle, fg)
        }
        AppCategory.CALENDAR -> {
            drawRoundRect(fg, topLeft = Offset(cx - 6.8f * u, cy - 6.5f * u), size = Size(13.6f * u, 13f * u), cornerRadius = CornerRadius(2.2f * u), style = Stroke(1.7f * u))
            drawLine(fg, Offset(cx - 6.8f * u, cy - 2.2f * u), Offset(cx + 6.8f * u, cy - 2.2f * u), strokeWidth = 1.7f * u)
            drawCircle(fg, radius = 1.3f * u, center = Offset(cx, cy + 2.4f * u))
        }
        AppCategory.CLOCK -> {
            drawCircle(fg, radius = 7.2f * u, center = Offset(cx, cy), style = Stroke(1.7f * u))
            drawLine(fg, Offset(cx, cy), Offset(cx, cy - 4.2f * u), strokeWidth = 1.7f * u, cap = StrokeCap.Round)
            drawLine(fg, Offset(cx, cy), Offset(cx + 3.4f * u, cy + 1.5f * u), strokeWidth = 1.5f * u, cap = StrokeCap.Round)
        }
        AppCategory.CALCULATOR -> {
            val s = 1.8f * u
            drawLine(fg, Offset(cx - 5f * u, cy - 3.2f * u), Offset(cx - 1.4f * u, cy - 3.2f * u), s, StrokeCap.Round)
            drawLine(fg, Offset(cx - 3.2f * u, cy - 5f * u), Offset(cx - 3.2f * u, cy - 1.4f * u), s, StrokeCap.Round)
            drawLine(fg, Offset(cx + 1.4f * u, cy - 3.2f * u), Offset(cx + 5f * u, cy - 3.2f * u), s, StrokeCap.Round)
            drawLine(fg, Offset(cx - 4.8f * u, cy + 2.2f * u), Offset(cx - 1.6f * u, cy + 5.2f * u), s, StrokeCap.Round)
            drawLine(fg, Offset(cx - 1.6f * u, cy + 2.2f * u), Offset(cx - 4.8f * u, cy + 5.2f * u), s, StrokeCap.Round)
            drawLine(fg, Offset(cx + 1.4f * u, cy + 2.6f * u), Offset(cx + 5f * u, cy + 2.6f * u), s, StrokeCap.Round)
            drawLine(fg, Offset(cx + 1.4f * u, cy + 4.8f * u), Offset(cx + 5f * u, cy + 4.8f * u), s, StrokeCap.Round)
        }
        AppCategory.CONTACTS -> {
            drawCircle(fg, radius = 2.8f * u, center = Offset(cx, cy - 2.8f * u))
            drawArc(fg, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(cx - 5.5f * u, cy + 0.5f * u), size = Size(11f * u, 9f * u))
        }
        AppCategory.FILES -> {
            val folder = Path().apply {
                moveTo(cx - 7f * u, cy - 5f * u)
                lineTo(cx - 1.5f * u, cy - 5f * u)
                lineTo(cx + 0.5f * u, cy - 3f * u)
                lineTo(cx + 7f * u, cy - 3f * u)
                lineTo(cx + 7f * u, cy + 5.5f * u)
                lineTo(cx - 7f * u, cy + 5.5f * u)
                close()
            }
            drawPath(folder, fg, style = Stroke(1.8f * u))
        }
        AppCategory.MUSIC -> {
            drawCircle(fg, radius = 2.1f * u, center = Offset(cx - 2.6f * u, cy + 4f * u))
            drawCircle(fg, radius = 2.1f * u, center = Offset(cx + 3.4f * u, cy + 2.8f * u))
            drawLine(fg, Offset(cx - 0.7f * u, cy + 4f * u), Offset(cx - 0.7f * u, cy - 5.2f * u), 1.8f * u)
            drawLine(fg, Offset(cx + 5.3f * u, cy + 2.8f * u), Offset(cx + 5.3f * u, cy - 6.4f * u), 1.8f * u)
            drawLine(fg, Offset(cx - 0.7f * u, cy - 5.2f * u), Offset(cx + 5.3f * u, cy - 6.4f * u), 2.4f * u)
        }
        AppCategory.MAIL -> {
            drawRoundRect(fg, topLeft = Offset(cx - 7.2f * u, cy - 5f * u), size = Size(14.4f * u, 10f * u), cornerRadius = CornerRadius(1.8f * u), style = Stroke(1.7f * u))
            val flap = Path().apply {
                moveTo(cx - 6.8f * u, cy - 4.4f * u)
                lineTo(cx, cy + 1.2f * u)
                lineTo(cx + 6.8f * u, cy - 4.4f * u)
            }
            drawPath(flap, fg, style = Stroke(1.7f * u))
        }
        AppCategory.SETTINGS -> {
            drawCircle(fg, radius = 4.2f * u, center = Offset(cx, cy), style = Stroke(1.8f * u))
            drawCircle(fg, radius = 1.6f * u, center = Offset(cx, cy))
            for (i in 0 until 8) {
                val a = (i * 45.0) * Math.PI / 180.0
                val x1 = cx + cos(a).toFloat() * 4.8f * u
                val y1 = cy + sin(a).toFloat() * 4.8f * u
                val x2 = cx + cos(a).toFloat() * 6.8f * u
                val y2 = cy + sin(a).toFloat() * 6.8f * u
                drawLine(fg, Offset(x1, y1), Offset(x2, y2), 1.8f * u, StrokeCap.Round)
            }
        }
        AppCategory.OTHER -> {}
    }
}

fun drawableToBitmap(drawable: Drawable): Bitmap? {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtMost(192) else 128
    val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtMost(192) else 128
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bmp
}
