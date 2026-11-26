package az.diamante.ioslauncher.model

import android.graphics.drawable.Drawable

/**
 * Hər bir quraşdırılmış tətbiqin model sinfi.
 * Orijinal və istifadəçi tərəfindən fərdiləşdirilmiş ad və ikon məlumatlarını saxlayır.
 */
data class AppModel(
    val packageName: String,
    val activityName: String,
    val originalLabel: String,
    var customLabel: String? = null,
    val originalIcon: Drawable,
    var customIconUri: String? = null,
    var isHidden: Boolean = false
) {
    /**
     * Ekranda istifadəçiyə görünəcək ad:
     * Əgər xüsusi ad təyin edilibsə onu, yoxsa orijinal tətbiq adını qaytarır.
     */
    val displayLabel: String
        get() = customLabel?.takeIf { it.isNotBlank() } ?: originalLabel
}
