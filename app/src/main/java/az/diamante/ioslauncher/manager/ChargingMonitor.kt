package az.diamante.ioslauncher.manager

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import az.diamante.ioslauncher.MainActivity

data class BatterySnapshot(
    val level: Int = -1, // -1: müvəqqəti əlçatmaz
    val isPlugged: Boolean = false,
    val isFull: Boolean = false
)

/**
 * Real Android batareya vəziyyətini oxuyan və qoşulma hadisəsini yalnız yeni kabel/simsiz
 * qoşulması baş verdikdə bir dəfə ötürən monitor.
 */
class ChargingMonitor(private val context: Context) {

    fun readCurrentSnapshot(): BatterySnapshot {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return BatterySnapshot()
        return parseIntent(intent)
    }

    fun parseIntent(intent: Intent): BatterySnapshot {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt().coerceIn(0, 100) else -1
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val full = status == BatteryManager.BATTERY_STATUS_FULL || pct == 100
        return BatterySnapshot(level = pct, isPlugged = plugged, isFull = full)
    }

    fun createReceiver(onUpdate: (BatterySnapshot, Boolean) -> Unit): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent == null) return
                when (intent.action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        val snap = readCurrentSnapshot().copy(isPlugged = true)
                        onUpdate(snap, true)
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        val snap = readCurrentSnapshot().copy(isPlugged = false)
                        onUpdate(snap, false)
                    }
                    Intent.ACTION_BATTERY_CHANGED -> {
                        onUpdate(parseIntent(intent), false)
                    }
                }
            }
        }
    }

    fun postOptionalChargingNotification(level: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "centraldev_charging_scene"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Şarj səhnəsi", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("open_charging_scene", true)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            context,
            101,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = if (level in 0..100) "Şarj səviyyəsi: ${level}%. Səhnəni açmaq üçün toxunun." else "Şarj səhnəsini açmaq üçün toxunun."
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("CentralDev Şarj Kinoteatrı")
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        nm.notify(4041, notif)
    }
}
