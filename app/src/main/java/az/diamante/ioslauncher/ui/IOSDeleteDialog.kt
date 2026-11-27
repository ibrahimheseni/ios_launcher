package az.diamante.ioslauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.diamante.ioslauncher.model.AppModel

/**
 * iOS 18 Üslublu Tətbiqi Silmə / Gizlətmə Seçim Dialoqu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IOSDeleteAppBottomSheet(
    app: AppModel,
    onUninstallApp: (AppModel) -> Unit,
    onHideApp: (AppModel) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color(0xFF1E1E1E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\"${app.displayLabel}\" Silinsin?",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Tətbiqi telefondan tamamilə silə və ya yalnız ana ekrandan gizlədə bilərsiniz.",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Telefondan Tamamilə Sil Düyməsi (Qırmızı)
            Button(
                onClick = {
                    onUninstallApp(app)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Tətbiqi Telefondan Sil (Uninstall)", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Ana Ekrandan Gizlət Düyməsi (PIN Seyfinə göndərir)
            Button(
                onClick = {
                    onHideApp(app)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Ana Ekrandan Gizlət (Seyfə at)", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Ləğv et
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ləğv et", color = Color(0xFF007AFF), fontSize = 16.sp)
            }
        }
    }
}
