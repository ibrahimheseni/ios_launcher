package az.diamante.ioslauncher.data

import androidx.compose.ui.graphics.Color

enum class IconVisualStyle(val titleAz: String) {
    LIGHT("Açıq"),
    DARK("Tünd"),
    CLEAR("Şəffaf"),
    TINTED("Rəng tonlu")
}

enum class GridPreset(val cols: Int, val rows: Int, val label: String) {
    G4X5(4, 5, "4 × 5"),
    G4X6(4, 6, "4 × 6"),
    G5X6(5, 6, "5 × 6")
}

enum class PercentPosition(val titleAz: String) {
    RIGHT("Sağda"),
    LEFT("Solda"),
    BELOW("Altda"),
    INSIDE("İçində")
}

enum class BatteryPlacement(val titleAz: String) {
    TOP_BAR("Yuxarı panel"),
    WIDGET_COMPACT("Kompakt vidcet"),
    WIDGET_WIDE("Geniş vidcet")
}

enum class ChargingSceneId(val titleAz: String, val subtitleAz: String) {
    CENTRALDEV_CORE("CentralDev Core", "Qara səthdə nazik qırmızı işıq konturu"),
    IH_SIGNATURE("İbrahim Həsəni Signature", "Tünd metal üzərində İH monoqramı"),
    TESLA_MODEL_S("Tesla Model S Studio", "2014 Model S studiya faraları və enerji zolağı"),
    MILITARY_TITAN("Hərbi Titan", "Palıd-yaşıl metal lövhə və relyef işığı"),
    OBSIDIAN_LIQUID("Obsidian Maye", "Kənarlardan toplanan abstrakt maye forması"),
    AZ_HERITAGE("Azərbaycan Gerbi", "Sakit fonda tam rəsmi gerb və bayraq işığı"),
    BAKU_NIGHT("Bakı gecəsi", "Sahil işıqları və suda əks olunan şəhər xətti"),
    TWO_CATS("İki pişik", "Narıncı zolaqlı və yumruüz boz pişik səhnəsi")
}

enum class WallpaperMotionType(val titleAz: String) {
    STATIC("Statik"),
    PARALLAX("Dərinlik / parallaks"),
    REALTIME_3D("Real vaxt 3D"),
    LIVE_SYSTEM("Canlı fon (sistem)")
}

enum class DoubleTapAction(val titleAz: String) {
    NONE("Heç nə"),
    OPEN_SEARCH("Axtarışı aç"),
    OPEN_SETTINGS("Ayarları aç"),
    OPEN_BATTERY_STUDIO("Batareya studiyası"),
    OPEN_CHARGING_CINEMA("Şarj kinoteatrı")
}

data class FolderModel(
    val id: String,
    val name: String,
    val itemKeys: List<String>
)

data class BatteryConfig(
    val symbolId: String = "cd_text",
    val percentPosition: PercentPosition = PercentPosition.RIGHT,
    val sizeScale: Float = 1.0f,
    val accentColorHex: Long = 0xFFE53935,
    val placement: BatteryPlacement = BatteryPlacement.TOP_BAR,
    val applyToChargingScene: Boolean = true,
    val customPngPath: String? = null,
    val ihCustomPngPath: String? = null,
    val characterCustomPngPath: String? = null,
    val favorites: Set<String> = setOf("cd_text", "ih_monogram", "az_crest"),
    val recents: List<String> = listOf("cd_text")
)

data class ChargingConfig(
    val sceneId: ChargingSceneId = ChargingSceneId.CENTRALDEV_CORE,
    val durationSec: Int = 5,
    val soundEnabled: Boolean = false,
    val vibrateEnabled: Boolean = false,
    val reducedMotion: Boolean = false,
    val notifyWhenOtherApp: Boolean = false,
    val customVenomPngPath: String? = null
)

data class WallpaperConfig(
    val pairId: String = "cd_obsidian",
    val motionType: WallpaperMotionType = WallpaperMotionType.PARALLAX,
    val customHomePath: String? = null,
    val customLockPath: String? = null
)

data class CentralDevSettings(
    val themeName: String = "CentralDev Obsidian",
    val accentColorHex: Long = 0xFFE53935, // İdarəli qırmızı
    val iconStyle: IconVisualStyle = IconVisualStyle.DARK,
    val iconSizeDp: Int = 58,
    val showLabels: Boolean = true,
    val rowSpacingDp: Int = 10,
    val gridPreset: GridPreset = GridPreset.G4X5,
    val dockCapacity: Int = 4,
    val isLayoutLocked: Boolean = false,
    val doubleTapAction: DoubleTapAction = DoubleTapAction.OPEN_SEARCH,
    val showOnboardingBanner: Boolean = true,
    val battery: BatteryConfig = BatteryConfig(),
    val charging: ChargingConfig = ChargingConfig(),
    val wallpaper: WallpaperConfig = WallpaperConfig()
)

object ObsidianPalette {
    val GraphiteBg = Color(0xFF121417)
    val GraphiteSurface = Color(0xFF1A1D22)
    val GraphiteCard = Color(0xFF22262D)
    val ControlledRed = Color(0xFFE53935)
    val SoftWhite = Color(0xFFF4F6F8)
    val MutedText = Color(0xFF9AA3AF)
    val SubtleBorder = Color(0x26FFFFFF)
}
