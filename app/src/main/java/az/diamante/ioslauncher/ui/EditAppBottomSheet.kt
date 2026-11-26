package az.diamante.ioslauncher.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.diamante.ioslauncher.model.AppModel

/**
 * iOS 18 Üslublu Tətbiq Redaktə və Fərdiləşdirmə Menyusu (Bottom Sheet)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppBottomSheet(
    app: AppModel,
    onDismiss: () -> Unit,
    onSave: (newName: String?, newIconUri: String?) -> Unit,
    onHide: () -> Unit,
    onReset: () -> Unit
) {
    var nameState by remember { mutableStateOf(app.displayLabel) }
    var selectedIconUri by remember { mutableStateOf<String?>(app.customIconUri) }

    // Qalereyadan şəkil seçmək üçün Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedIconUri = uri.toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFF1E1E1E) // iOS Dark Blur fon rəngi
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tətbiq Tənzimləmələri",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tətbiq Adı Redaktəsi
            OutlinedTextField(
                value = nameState,
                onValueChange = { nameState = it },
                label = { Text("Tətbiqin Yeni Adı", color = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF007AFF), // iOS Göy rəngi
                    unfocusedBorderColor = Color.DarkGray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // İkonu Dəyiş Düyməsi (Qalereyadan)
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF007AFF))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedIconUri != null) "İkon Seçildi (Dəyiş)" else "Qalereyadan İkon Seç",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tətbiqi Gizlət Düyməsi
            Button(
                onClick = onHide,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A2828)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = Color(0xFFFF453A))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tətbiqi Gizlət (Hidden Apps)", color = Color(0xFFFF453A))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Orijinala Qaytar
            if (app.customLabel != null || app.customIconUri != null) {
                TextButton(onClick = onReset) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Orijinal İkon və Ada Qaytar", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Yadda saxla düyməsi
            Button(
                onClick = {
                    onSave(nameState, selectedIconUri)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Yadda saxla", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
