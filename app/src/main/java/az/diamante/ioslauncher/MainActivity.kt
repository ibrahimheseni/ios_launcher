package az.diamante.ioslauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import az.diamante.ioslauncher.data.LauncherRepository
import az.diamante.ioslauncher.manager.AppManager
import az.diamante.ioslauncher.manager.ChargingMonitor
import az.diamante.ioslauncher.ui.CentralDevHomeScreen

/**
 * CentralDev Launcher-in əsas Home Activity sinfi.
 */
class MainActivity : ComponentActivity() {

    private lateinit var appManager: AppManager
    private lateinit var chargingMonitor: ChargingMonitor
    private var openChargingSignal by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = LauncherRepository(this)
        appManager = AppManager(this, repo)
        chargingMonitor = ChargingMonitor(this)

        if (intent?.getBooleanExtra("open_charging_scene", false) == true) {
            openChargingSignal++
        }

        setContent {
            CentralDevHomeScreen(
                appManager = appManager,
                chargingMonitor = chargingMonitor,
                openChargingSignal = openChargingSignal
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("open_charging_scene", false)) {
            openChargingSignal++
        }
    }
}
