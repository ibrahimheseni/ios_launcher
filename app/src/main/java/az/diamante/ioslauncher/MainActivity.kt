package az.diamante.ioslauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import az.diamante.ioslauncher.manager.AppManager
import az.diamante.ioslauncher.ui.IOSHomeScreen

/**
 * iOS 18 Launcher-in Əsas Activity sinfi.
 * Tam ekran (Edge-to-Edge) rejimində işə düşür.
 */
class MainActivity : ComponentActivity() {

    private lateinit var appManager: AppManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appManager = AppManager(this)

        setContent {
            IOSHomeScreen(appManager = appManager)
        }
    }
}
