package az.diamante.ioslauncher.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import az.diamante.ioslauncher.data.AppOverride
import az.diamante.ioslauncher.data.LauncherRepository
import az.diamante.ioslauncher.model.AppCategory
import az.diamante.ioslauncher.model.AppModel
import az.diamante.ioslauncher.model.AppShortcutItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * LauncherApps + UserManager əsaslı tətbiq meneceri.
 * Şəxsi və iş profillərini ayırır, kilidli/sakit profilləri gizlədir,
 * Azərbaycan dilində İ/i və I/ı axtarışını dəstəkləyir.
 */
class AppManager(
    private val context: Context,
    val repo: LauncherRepository
) {
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    private val myUser = Process.myUserHandle()

    suspend fun getInstalledApps(): List<AppModel> = withContext(Dispatchers.IO) {
        val result = mutableListOf<AppModel>()
        val profiles = runCatching { userManager.userProfiles }.getOrElse { listOf(myUser) }

        for (profile in profiles) {
            // Kilidli və ya sakit rejimdə olan profil tətbiqləri gizli saxlanır
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && userManager.isQuietModeEnabled(profile)) {
                continue
            }
            val serial = userManager.getSerialNumberForUser(profile)
            val isWork = profile != myUser
            val activities = runCatching { launcherApps.getActivityList(null, profile) }.getOrElse { emptyList() }

            for (info in activities) {
                val pkg = info.componentName.packageName
                if (pkg == context.packageName) continue

                val cls = info.componentName.className
                val componentKey = "$pkg/$cls#$serial"
                val override = repo.getAppOverride(componentKey)
                val label = info.label.toString()
                val icon = info.getIcon(0)

                val shortcuts = runCatching {
                    val query = LauncherApps.ShortcutQuery().apply {
                        setPackage(pkg)
                        setQueryFlags(
                            LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                        )
                    }
                    launcherApps.getShortcuts(query, profile)?.take(3)?.map { sc ->
                        AppShortcutItem(
                            id = sc.id,
                            label = (sc.shortLabel ?: sc.longLabel ?: sc.id).toString(),
                            packageName = pkg
                        )
                    } ?: emptyList()
                }.getOrElse { emptyList() }

                result.add(
                    AppModel(
                        packageName = pkg,
                        activityName = cls,
                        userSerial = serial,
                        userHandle = profile,
                        originalLabel = label,
                        customLabel = override.customLabel,
                        originalIcon = icon,
                        customIconPath = override.customIconPath,
                        useOriginalIcon = override.useOriginalIcon,
                        isHidden = override.isHidden,
                        isWorkProfile = isWork,
                        category = detectCategory(pkg, label),
                        shortcuts = shortcuts
                    )
                )
            }
        }
        result.sortedBy { azNormalize(it.displayLabel) }
    }

    fun launchApp(app: AppModel) {
        repo.recordRecentApp(app.componentKey)
        val component = ComponentName(app.packageName, app.activityName)
        val handle = app.userHandle ?: myUser
        runCatching {
            launcherApps.startMainActivity(component, handle, null, null)
        }.onFailure {
            val fallback = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                this.component = component
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            runCatching { context.startActivity(fallback) }
        }
    }

    fun launchShortcut(app: AppModel, shortcutId: String) {
        val handle = app.userHandle ?: myUser
        runCatching {
            launcherApps.startShortcut(app.packageName, shortcutId, null, null, handle)
        }.onFailure {
            launchApp(app)
        }
    }

    fun openAppInfo(app: AppModel) {
        val component = ComponentName(app.packageName, app.activityName)
        val handle = app.userHandle ?: myUser
        runCatching {
            launcherApps.startAppDetailsActivity(component, handle, null, null)
        }.onFailure {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${app.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            runCatching { context.startActivity(intent) }
        }
    }

    fun uninstallApp(app: AppModel) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:${app.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    fun saveCustomNameAndIcon(
        app: AppModel,
        newName: String?,
        newIconPath: String?,
        useOriginalIcon: Boolean
    ) {
        val existing = repo.getAppOverride(app.componentKey)
        repo.saveAppOverride(
            app.componentKey,
            existing.copy(
                customLabel = newName?.trim()?.takeIf { it.isNotBlank() },
                customIconPath = newIconPath,
                useOriginalIcon = useOriginalIcon
            )
        )
    }

    fun setHidden(app: AppModel, hidden: Boolean) {
        val existing = repo.getAppOverride(app.componentKey)
        repo.saveAppOverride(app.componentKey, existing.copy(isHidden = hidden))
    }

    fun resetToFactory(app: AppModel) {
        repo.resetAppOverride(app.componentKey)
    }

    fun copyUriToInternalFile(uri: Uri, prefix: String): String? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val outFile = File(context.filesDir, "${prefix}_${System.currentTimeMillis()}.png")
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
            input.close()
            outFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun registerPackageCallback(onChanged: () -> Unit): LauncherApps.Callback {
        val cb = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                val serial = userManager.getSerialNumberForUser(user)
                repo.loadHomeOrder().filter { it.startsWith("$packageName/") && it.endsWith("#$serial") }
                    .forEach { repo.cleanupRemovedComponent(it) }
                onChanged()
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) = onChanged()
            override fun onPackageChanged(packageName: String, user: UserHandle) = onChanged()
            override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = onChanged()
            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = onChanged()
        }
        launcherApps.registerCallback(cb)
        return cb
    }

    fun unregisterPackageCallback(cb: LauncherApps.Callback) {
        runCatching { launcherApps.unregisterCallback(cb) }
    }

    companion object {
        private val AZ_LOCALE = Locale("az", "AZ")

        /**
         * Azərbaycan dilində "İ/i" və "I/ı" hərflərinin axtarışda düzgün uyğunlaşması.
         */
        fun azNormalize(text: String): String {
            return text
                .replace('İ', 'i')
                .replace('I', 'ı')
                .lowercase(AZ_LOCALE)
                .trim()
        }

        fun matchesSearch(app: AppModel, query: String): Boolean {
            if (app.isHidden) return false
            val q = azNormalize(query)
            if (q.isEmpty()) return true
            return azNormalize(app.displayLabel).contains(q) ||
                azNormalize(app.originalLabel).contains(q)
        }

        fun detectCategory(pkg: String, label: String): AppCategory {
            val p = pkg.lowercase()
            val l = azNormalize(label)
            return when {
                p.contains("dialer") || p.contains("telecom") || p == "com.android.phone" || l.contains("telefon") || l == "zəng" -> AppCategory.PHONE
                p.contains("messaging") || p.contains("mms") || p.contains("apps.messaging") || l.contains("mesaj") -> AppCategory.MESSAGES
                p.contains("camera") || l.contains("kamera") -> AppCategory.CAMERA
                p.contains("gallery") || p.contains("photos") || l.contains("qalereya") || l.contains("foto") -> AppCategory.GALLERY
                p.contains("chrome") || p.contains("browser") || p.contains("firefox") || p.contains("opera") || l.contains("brauzer") -> AppCategory.BROWSER
                p.contains("calendar") || l.contains("təqvim") || l.contains("takvim") -> AppCategory.CALENDAR
                p.contains("deskclock") || p.contains("clock") || l.contains("saat") -> AppCategory.CLOCK
                p.contains("calculator") || l.contains("hesabla") || l.contains("kalkulyator") -> AppCategory.CALCULATOR
                p.contains("contacts") || p.contains("kontakt") || l.contains("kişiler") -> AppCategory.CONTACTS
                p.contains("documentsui") || p.contains("filemanager") || p.contains("files") || l.contains("fayl") || l.contains("dosya") -> AppCategory.FILES
                p.contains("music") || p.contains("spotify") || l.contains("musiqi") || l.contains("müzik") -> AppCategory.MUSIC
                p.contains("gm") || p.contains("mail") || p.contains("outlook") || l.contains("poçt") -> AppCategory.MAIL
                p.contains("settings") || l.contains("ayarlar") || l.contains("tənzimləmə") -> AppCategory.SETTINGS
                else -> AppCategory.OTHER
            }
        }
    }
}
