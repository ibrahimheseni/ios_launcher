package az.diamante.ioslauncher.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class AppOverride(
    val customLabel: String? = null,
    val customIconPath: String? = null,
    val useOriginalIcon: Boolean = false,
    val isHidden: Boolean = false
)

data class BackupPreviewReport(
    val valid: Boolean,
    val totalCustomApps: Int,
    val folderCount: Int,
    val missingPackages: List<String>,
    val missingIconFiles: List<String>,
    val rawJson: String
)

/**
 * CentralDev Launcher-in bütün tənzimləmələrini, qovluqlarını, fərdi ad/ikon seçimlərini
 * və ehtiyat nüsxə (Backup/Restore) prosesini idarə edən daimi yaddaş qatı.
 */
class LauncherRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("centraldev_launcher_v2", Context.MODE_PRIVATE)

    fun getAppOverride(componentKey: String): AppOverride {
        val jsonStr = prefs.getString("override_$componentKey", null) ?: return AppOverride()
        return try {
            val obj = JSONObject(jsonStr)
            val iconPath = obj.optString("icon", "").takeIf { it.isNotBlank() }
            val validIconPath = if (iconPath != null && File(iconPath).exists()) iconPath else null
            AppOverride(
                customLabel = obj.optString("label", "").takeIf { it.isNotBlank() },
                customIconPath = validIconPath,
                useOriginalIcon = obj.optBoolean("useOriginal", false),
                isHidden = obj.optBoolean("hidden", false)
            )
        } catch (_: Exception) {
            AppOverride()
        }
    }

    fun saveAppOverride(componentKey: String, override: AppOverride) {
        val obj = JSONObject().apply {
            put("label", override.customLabel ?: "")
            put("icon", override.customIconPath ?: "")
            put("useOriginal", override.useOriginalIcon)
            put("hidden", override.isHidden)
        }
        prefs.edit().putString("override_$componentKey", obj.toString()).apply()
    }

    fun resetAppOverride(componentKey: String) {
        val existing = getAppOverride(componentKey)
        saveAppOverride(
            componentKey,
            AppOverride(isHidden = existing.isHidden)
        )
    }

    fun cleanupRemovedComponent(componentKey: String) {
        val order = loadHomeOrder().filterNot { it == componentKey }
        saveHomeOrder(order)
        val dock = loadDockKeys().filterNot { it == componentKey }
        saveDockKeys(dock)
        val folders = loadFolders().map { f ->
            f.copy(itemKeys = f.itemKeys.filterNot { it == componentKey })
        }.filter { it.itemKeys.isNotEmpty() }
        saveFolders(folders)
    }

    fun loadHomeOrder(): List<String> {
        val raw = prefs.getString("home_order", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
    }

    fun saveHomeOrder(keys: List<String>) {
        prefs.edit().putString("home_order", keys.joinToString("|")).apply()
    }

    fun loadDockKeys(): List<String> {
        val raw = prefs.getString("dock_keys", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
    }

    fun saveDockKeys(keys: List<String>) {
        prefs.edit().putString("dock_keys", keys.joinToString("|")).apply()
    }

    fun loadRecentKeys(): List<String> {
        val raw = prefs.getString("recent_apps", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
    }

    fun recordRecentApp(componentKey: String) {
        val list = loadRecentKeys().toMutableList()
        list.remove(componentKey)
        list.add(0, componentKey)
        prefs.edit().putString("recent_apps", list.take(8).joinToString("|")).apply()
    }

    fun loadFolders(): List<FolderModel> {
        val raw = prefs.getString("folders_json", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val itemsArr = obj.optJSONArray("items") ?: JSONArray()
                val keys = (0 until itemsArr.length()).map { j -> itemsArr.getString(j) }
                FolderModel(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    itemKeys = keys
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveFolders(folders: List<FolderModel>) {
        val arr = JSONArray()
        folders.forEach { f ->
            val obj = JSONObject().apply {
                put("id", f.id)
                put("name", f.name)
                put("items", JSONArray(f.itemKeys))
            }
            arr.put(obj)
        }
        prefs.edit().putString("folders_json", arr.toString()).apply()
    }

    fun checkPin(input: String): Boolean {
        val saved = prefs.getString("vault_pin", "0000") ?: "0000"
        return input == saved
    }

    fun setPin(newPin: String) {
        prefs.edit().putString("vault_pin", newPin).apply()
    }

    fun loadSettings(): CentralDevSettings {
        val raw = prefs.getString("settings_json", null) ?: return CentralDevSettings()
        return try {
            val o = JSONObject(raw)
            val bObj = o.optJSONObject("battery") ?: JSONObject()
            val cObj = o.optJSONObject("charging") ?: JSONObject()
            val wObj = o.optJSONObject("wallpaper") ?: JSONObject()

            val favArr = bObj.optJSONArray("favorites")
            val favs = if (favArr != null) {
                (0 until favArr.length()).map { favArr.getString(it) }.toSet()
            } else setOf("cd_text", "ih_monogram", "az_crest")

            val recArr = bObj.optJSONArray("recents")
            val recs = if (recArr != null) {
                (0 until recArr.length()).map { recArr.getString(it) }
            } else listOf("cd_text")

            CentralDevSettings(
                themeName = o.optString("themeName", "CentralDev Obsidian"),
                accentColorHex = o.optLong("accentColorHex", 0xFFE53935),
                iconStyle = runCatching { IconVisualStyle.valueOf(o.optString("iconStyle", "DARK")) }.getOrDefault(IconVisualStyle.DARK),
                iconSizeDp = o.optInt("iconSizeDp", 58),
                showLabels = o.optBoolean("showLabels", true),
                rowSpacingDp = o.optInt("rowSpacingDp", 10),
                gridPreset = runCatching { GridPreset.valueOf(o.optString("gridPreset", "G4X5")) }.getOrDefault(GridPreset.G4X5),
                dockCapacity = o.optInt("dockCapacity", 4).coerceIn(4, 5),
                isLayoutLocked = o.optBoolean("isLayoutLocked", false),
                doubleTapAction = runCatching { DoubleTapAction.valueOf(o.optString("doubleTapAction", "OPEN_SEARCH")) }.getOrDefault(DoubleTapAction.OPEN_SEARCH),
                showOnboardingBanner = o.optBoolean("showOnboardingBanner", true),
                battery = BatteryConfig(
                    symbolId = bObj.optString("symbolId", "cd_text"),
                    percentPosition = runCatching { PercentPosition.valueOf(bObj.optString("percentPosition", "RIGHT")) }.getOrDefault(PercentPosition.RIGHT),
                    sizeScale = bObj.optDouble("sizeScale", 1.0).toFloat(),
                    accentColorHex = bObj.optLong("accentColorHex", 0xFFE53935),
                    placement = runCatching { BatteryPlacement.valueOf(bObj.optString("placement", "TOP_BAR")) }.getOrDefault(BatteryPlacement.TOP_BAR),
                    applyToChargingScene = bObj.optBoolean("applyToChargingScene", true),
                    customPngPath = bObj.optString("customPngPath", "").takeIf { it.isNotBlank() && File(it).exists() },
                    ihCustomPngPath = bObj.optString("ihCustomPngPath", "").takeIf { it.isNotBlank() && File(it).exists() },
                    characterCustomPngPath = bObj.optString("characterCustomPngPath", "").takeIf { it.isNotBlank() && File(it).exists() },
                    favorites = favs,
                    recents = recs
                ),
                charging = ChargingConfig(
                    sceneId = runCatching { ChargingSceneId.valueOf(cObj.optString("sceneId", "CENTRALDEV_CORE")) }.getOrDefault(ChargingSceneId.CENTRALDEV_CORE),
                    durationSec = cObj.optInt("durationSec", 5),
                    soundEnabled = cObj.optBoolean("soundEnabled", false),
                    vibrateEnabled = cObj.optBoolean("vibrateEnabled", false),
                    reducedMotion = cObj.optBoolean("reducedMotion", false),
                    notifyWhenOtherApp = cObj.optBoolean("notifyWhenOtherApp", false),
                    customVenomPngPath = cObj.optString("customVenomPngPath", "").takeIf { it.isNotBlank() && File(it).exists() }
                ),
                wallpaper = WallpaperConfig(
                    pairId = wObj.optString("pairId", "cd_obsidian"),
                    motionType = runCatching { WallpaperMotionType.valueOf(wObj.optString("motionType", "PARALLAX")) }.getOrDefault(WallpaperMotionType.PARALLAX),
                    customHomePath = wObj.optString("customHomePath", "").takeIf { it.isNotBlank() && File(it).exists() },
                    customLockPath = wObj.optString("customLockPath", "").takeIf { it.isNotBlank() && File(it).exists() }
                )
            )
        } catch (_: Exception) {
            CentralDevSettings()
        }
    }

    fun saveSettings(s: CentralDevSettings) {
        val bObj = JSONObject().apply {
            put("symbolId", s.battery.symbolId)
            put("percentPosition", s.battery.percentPosition.name)
            put("sizeScale", s.battery.sizeScale.toDouble())
            put("accentColorHex", s.battery.accentColorHex)
            put("placement", s.battery.placement.name)
            put("applyToChargingScene", s.battery.applyToChargingScene)
            put("customPngPath", s.battery.customPngPath ?: "")
            put("ihCustomPngPath", s.battery.ihCustomPngPath ?: "")
            put("characterCustomPngPath", s.battery.characterCustomPngPath ?: "")
            put("favorites", JSONArray(s.battery.favorites.toList()))
            put("recents", JSONArray(s.battery.recents))
        }
        val cObj = JSONObject().apply {
            put("sceneId", s.charging.sceneId.name)
            put("durationSec", s.charging.durationSec)
            put("soundEnabled", s.charging.soundEnabled)
            put("vibrateEnabled", s.charging.vibrateEnabled)
            put("reducedMotion", s.charging.reducedMotion)
            put("notifyWhenOtherApp", s.charging.notifyWhenOtherApp)
            put("customVenomPngPath", s.charging.customVenomPngPath ?: "")
        }
        val wObj = JSONObject().apply {
            put("pairId", s.wallpaper.pairId)
            put("motionType", s.wallpaper.motionType.name)
            put("customHomePath", s.wallpaper.customHomePath ?: "")
            put("customLockPath", s.wallpaper.customLockPath ?: "")
        }
        val root = JSONObject().apply {
            put("themeName", s.themeName)
            put("accentColorHex", s.accentColorHex)
            put("iconStyle", s.iconStyle.name)
            put("iconSizeDp", s.iconSizeDp)
            put("showLabels", s.showLabels)
            put("rowSpacingDp", s.rowSpacingDp)
            put("gridPreset", s.gridPreset.name)
            put("dockCapacity", s.dockCapacity)
            put("isLayoutLocked", s.isLayoutLocked)
            put("doubleTapAction", s.doubleTapAction.name)
            put("showOnboardingBanner", s.showOnboardingBanner)
            put("battery", bObj)
            put("charging", cObj)
            put("wallpaper", wObj)
        }
        prefs.edit().putString("settings_json", root.toString()).apply()
    }

    fun exportBackupJson(): String {
        val overridesObj = JSONObject()
        prefs.all.forEach { (k, v) ->
            if (k.startsWith("override_") && v is String) {
                overridesObj.put(k.removePrefix("override_"), JSONObject(v))
            }
        }
        return JSONObject().apply {
            put("version", 2)
            put("settings", JSONObject(prefs.getString("settings_json", "{}") ?: "{}"))
            put("homeOrder", JSONArray(loadHomeOrder()))
            put("dockKeys", JSONArray(loadDockKeys()))
            put("folders", JSONArray(prefs.getString("folders_json", "[]") ?: "[]"))
            put("overrides", overridesObj)
        }.toString(2)
    }

    fun analyzeBackupJson(rawJson: String, installedPackages: Set<String>): BackupPreviewReport {
        return try {
            val root = JSONObject(rawJson)
            val overrides = root.optJSONObject("overrides") ?: JSONObject()
            val folders = root.optJSONArray("folders") ?: JSONArray()
            val missingPkgs = mutableListOf<String>()
            val missingIcons = mutableListOf<String>()
            val keys = overrides.keys()
            var count = 0
            while (keys.hasNext()) {
                val key = keys.next()
                count++
                val pkg = key.substringBefore("/")
                if (!installedPackages.contains(pkg)) {
                    missingPkgs.add(pkg)
                }
                val iconPath = overrides.getJSONObject(key).optString("icon", "")
                if (iconPath.isNotBlank() && !File(iconPath).exists()) {
                    missingIcons.add(iconPath)
                }
            }
            BackupPreviewReport(
                valid = true,
                totalCustomApps = count,
                folderCount = folders.length(),
                missingPackages = missingPkgs.distinct(),
                missingIconFiles = missingIcons.distinct(),
                rawJson = rawJson
            )
        } catch (_: Exception) {
            BackupPreviewReport(false, 0, 0, emptyList(), emptyList(), rawJson)
        }
    }

    fun applyBackupJson(rawJson: String): Boolean {
        return try {
            val root = JSONObject(rawJson)
            val editor = prefs.edit()
            if (root.has("settings")) {
                editor.putString("settings_json", root.getJSONObject("settings").toString())
            }
            if (root.has("homeOrder")) {
                val arr = root.getJSONArray("homeOrder")
                val list = (0 until arr.length()).map { arr.getString(it) }
                editor.putString("home_order", list.joinToString("|"))
            }
            if (root.has("dockKeys")) {
                val arr = root.getJSONArray("dockKeys")
                val list = (0 until arr.length()).map { arr.getString(it) }
                editor.putString("dock_keys", list.joinToString("|"))
            }
            if (root.has("folders")) {
                editor.putString("folders_json", root.getJSONArray("folders").toString())
            }
            if (root.has("overrides")) {
                val overrides = root.getJSONObject("overrides")
                val keys = overrides.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    editor.putString("override_$key", overrides.getJSONObject(key).toString())
                }
            }
            editor.apply()
            true
        } catch (_: Exception) {
            false
        }
    }
}
