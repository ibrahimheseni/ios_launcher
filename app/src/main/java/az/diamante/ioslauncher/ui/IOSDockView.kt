package az.diamante.ioslauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import az.diamante.ioslauncher.model.AppModel

/**
 * iOS 18 Frosted Glass (Bulanıq Şüşə) Alt Dock Paneli
 */
@Composable
fun IOSDockView(
    dockApps: List<AppModel>,
    onAppClick: (AppModel) -> Unit,
    onAppLongClick: (AppModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.28f))
            .border(0.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(32.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dockApps.take(4).forEach { app ->
                IOSIconItem(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) },
                    modifier = Modifier.width(70.dp)
                )
            }
        }
    }
}
