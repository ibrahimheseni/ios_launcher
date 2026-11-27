package az.diamante.ioslauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Telefon şarja taxıldıqda kilidli ekranda animasiyanı başladan qəbuledici
 */
class ChargingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_POWER_CONNECTED) {
            val lockIntent = Intent(context, ChargingLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(lockIntent)
        }
    }
}
