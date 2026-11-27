package az.diamante.ioslauncher

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import az.diamante.ioslauncher.ui.ChargingAnimationOverlay
import az.diamante.ioslauncher.ui.ChargingStyle

class ChargingLockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        val prefs = getSharedPreferences("ios_launcher_battery_prefs", Context.MODE_PRIVATE)
        val savedStyle = prefs.getString("charging_style", ChargingStyle.AZERBAIJAN_GERB.name)
        val style = ChargingStyle.valueOf(savedStyle ?: ChargingStyle.AZERBAIJAN_GERB.name)
        setContent {
            ChargingAnimationOverlay(
                batteryLevel = 85,
                style = style,
                onDismiss = { finish() }
            )
        }
    }
}
