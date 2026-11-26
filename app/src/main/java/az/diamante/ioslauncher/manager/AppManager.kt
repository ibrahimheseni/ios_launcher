package az.diamante.ioslauncher.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import az.diamante.ioslauncher.model.AppModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Telefondakı tətbiqləri idarə edən, fərdiləşdirmələri və gizlətmə statusunu
 * daimi yaddaşda saxlayan əsas menecer.
 */
class AppManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ios_launcher_settings", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_PIN = "vault_security_pin"
        private const val DEFAULT_PIN = "0000"
    }

    /**
     * Telefondakı bütün tətbiqləri arxa planda oxuyur və fərdiləşdirmələri tətbiq edir.
     */
    suspend fun getInstalledApps(): List<AppModel> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val appList = mutableListOf<AppModel>()

        for (info in resolveInfos) {
            val packageName = info.activityInfo.packageName
            // Öz launcher-imizi siyahıda göstərmirik
            if (packageName == context.packageName) continue

            val originalLabel = info.loadLabel(pm).toString()
            val originalIcon = info.loadIcon(pm)

            val customLabel = prefs.getString("custom_label_$packageName", null)
            val customIcon = prefs.getString("custom_icon_$packageName", null)
            val isHidden = prefs.getBoolean("is_hidden_$packageName", false)

            appList.add(
                AppModel(
                    packageName = packageName,
                    activityName = info.activityInfo.name,
                    originalLabel = originalLabel,
                    customLabel = customLabel,
                    originalIcon = originalIcon,
                    customIconUri = customIcon,
                    isHidden = isHidden
                )
            )
        }

        // Əlifba sırası ilə düzürük
        appList.sortedBy { it.displayLabel.lowercase() }
    }

    /**
     * Tətbiqi işə salmaq
     */
    fun launchApp(app: AppModel) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        context.startActivity(intent)
    }

    /**
     * Tətbiqin adını və ya ikonunu yaddaşda yeniləmək
     */
    fun saveCustomization(packageName: String, newName: String?, newIconUri: String?) {
        prefs.edit().apply {
            putString("custom_label_$packageName", newName?.trim())
            putString("custom_icon_$packageName", newIconUri)
            apply()
        }
    }

    /**
     * Tətbiqi gizlətmək və ya görünən etmək
     */
    fun setAppHidden(packageName: String, hide: Boolean) {
        prefs.edit().apply {
            putBoolean("is_hidden_$packageName", hide)
            apply()
        }
    }

    /**
     * Gizli tətbiqlər qovluğu üçün PIN yoxlanışı
     */
    fun checkPin(inputPin: String): Boolean {
        val savedPin = prefs.getString(PREF_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
        return inputPin == savedPin
    }

    /**
     * Yeni PIN təyin etmək
     */
    fun setPin(newPin: String) {
        prefs.edit().putString(PREF_PIN, newPin).apply()
    }
}
