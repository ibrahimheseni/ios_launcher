package az.diamante.ioslauncher.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class WallpaperItem(
    val id: String,
    val title: String,
    val url: String,
    val tag: String = "4K"
)

val PRESET_WALLPAPERS = listOf(
    WallpaperItem(
        "ios18_dark",
        "iOS 18 Rəsmi Dark",
        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=1200&auto=format&fit=crop",
        "iOS 18"
    ),
    WallpaperItem(
        "3d_liquid",
        "3D Maye Kristal",
        "https://images.unsplash.com/photo-1634017839464-5c339ebe3cb4?q=80&w=1200&auto=format&fit=crop",
        "3D 8K"
    ),
    WallpaperItem(
        "space_nebula",
        "Kosmos Dumanlığı",
        "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=1200&auto=format&fit=crop",
        "8K"
    ),
    WallpaperItem(
        "cyber_sphere",
        "3D Kiber Halqa",
        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=1200&auto=format&fit=crop",
        "3D 4K"
    ),
    WallpaperItem(
        "dark_aurora",
        "Gecə Şəfəqi",
        "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?q=80&w=1200&auto=format&fit=crop",
        "4K"
    ),
    WallpaperItem(
        "3d_geometric",
        "3D Qara Qrafika",
        "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?q=80&w=1200&auto=format&fit=crop",
        "3D"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperSelectorBottomSheet(
    currentWallpaperUri: String?,
    onWallpaperSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onWallpaperSelected(uri.toString())
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFF141416)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "4K / 8K & 3D Divar Kağızları",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF007AFF))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Qalereyadan Şəxsi 4K/8K Şəkil Seç", color = Color.White, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(380.dp)
            ) {
                items(PRESET_WALLPAPERS) { item ->
                    val isSelected = currentWallpaperUri == item.url

                    Box(
                        modifier = Modifier
                            .height(170.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 0.5.dp,
                                color = if (isSelected) Color(0xFF007AFF) else Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                onWallpaperSelected(item.url)
                                onDismiss()
                            }
                    ) {
                        AsyncImage(
                            model = item.url,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(item.tag, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(vertical = 6.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF007AFF),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }
        }
    }
}
