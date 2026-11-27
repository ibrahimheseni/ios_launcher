package az.diamante.ioslauncher.model

import android.graphics.drawable.Drawable
import android.os.UserHandle

enum class AppCategory {
    PHONE,
    MESSAGES,
    CAMERA,
    GALLERY,
    BROWSER,
    CALENDAR,
    CLOCK,
    CALCULATOR,
    CONTACTS,
    FILES,
    MUSIC,
    MAIL,
    SETTINGS,
    OTHER
}

data class AppShortcutItem(
    val id: String,
    val label: String,
    val packageName: String
)

/**
 * Komponent və profil (şəxsi / iş profili) əsaslı unikal tətbiq modeli.
 */
data class AppModel(
    val packageName: String,
    val activityName: String,
    val userSerial: Long,
    val userHandle: UserHandle?,
    val originalLabel: String,
    val customLabel: String? = null,
    val originalIcon: Drawable,
    val customIconPath: String? = null,
    val useOriginalIcon: Boolean = false,
    val isHidden: Boolean = false,
    val isWorkProfile: Boolean = false,
    val category: AppCategory = AppCategory.OTHER,
    val shortcuts: List<AppShortcutItem> = emptyList()
) {
    val componentKey: String
        get() = "$packageName/$activityName#$userSerial"

    val displayLabel: String
        get() = customLabel?.takeIf { it.isNotBlank() } ?: originalLabel
}
