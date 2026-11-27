package az.diamante.ioslauncher.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.diamante.ioslauncher.model.AppModel
import coil.compose.AsyncImage

/**
 * VIP iOS 18 Squircle İkon Görünüşü
 * Ağ kənarlıqsız, tam dolu şüşə parıltısı (specular glass) və hamar kölgə ilə.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun IOSIconItem(
    app: AppModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isJiggleMode: Boolean = false,
    onRemoveClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "JiggleTransition")
    val rotation by if (isJiggleMode) {
        infiniteTransition.animateFloat(
            initialValue = -1.8f,
            targetValue = 1.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 130, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "JiggleRotation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(vertical = 8.dp)
            .rotate(rotation)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(contentAlignment = Alignment.TopStart) {
            // VIP Tam Dolu Squircle Konteyneri (Ağ kənarlıqsız!)
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .border(0.6.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (app.customIconUri != null) {
                    AsyncImage(
                        model = Uri.parse(app.customIconUri),
                        contentDescription = app.displayLabel,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val bitmap = remember(app.packageName) {
                        drawableToBitmap(app.originalIcon)
                    }
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = app.displayLabel,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Apple üslublu üst şüşə parıltısı (Specular highlight)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.45f)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                )
            }

            if (isJiggleMode && onRemoveClick != null) {
                Box(
                    modifier = Modifier
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8E8E93))
                        .clickable(onClick = onRemoveClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Sil / Gizlət",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = app.displayLabel,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.8f),
                    offset = Offset(0f, 1.5f),
                    blurRadius = 4f
                )
            ),
            modifier = Modifier.width(76.dp)
        )
    }
}

private fun drawableToBitmap(drawable: Drawable): Bitmap? {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
