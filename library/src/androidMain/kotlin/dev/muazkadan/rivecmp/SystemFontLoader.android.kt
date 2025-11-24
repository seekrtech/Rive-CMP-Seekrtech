package dev.muazkadan.rivecmp

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import app.rive.runtime.kotlin.core.ContextAssetLoader
import app.rive.runtime.kotlin.core.FileAsset
import java.io.File

/**
 * Creates an Android system font asset loader for the specified locale.
 * This loader selects the appropriate Android system font based on the language/locale.
 */
@Composable
actual fun createSystemFontLoader(locale: String): Any? {
    val context = LocalContext.current
    return AndroidSystemFontLoader(context, locale)
}

/**
 * Android implementation of system font loader.
 * Loads Android system fonts from /system/fonts based on locale.
 *
 * @param context Android context
 * @param locale Language/locale code (e.g., "en", "zh_CN", "zh_TW", "ja", "ko", "ar", "th")
 */
class AndroidSystemFontLoader(
    context: Context,
    private val locale: String = "en"
) : ContextAssetLoader(context) {

    override fun loadContents(asset: FileAsset, inBandBytes: ByteArray): Boolean {
        val systemFontsDir = File("/system/fonts")

        if (!systemFontsDir.exists() || !systemFontsDir.isDirectory) {
            Log.w(TAG, "System fonts directory not found")
            return false
        }

        Log.d(TAG, "Loading font for locale: $locale")

        // Select priority fonts based on the current locale
        val priorityFontNames = getPriorityFontsForLocale(locale)

        // Get all available font files
        val allFontFiles = systemFontsDir.listFiles { file ->
            file.extension.lowercase() in listOf("ttf", "otf", "ttc")
        } ?: emptyArray()

        if (allFontFiles.isEmpty()) {
            Log.w(TAG, "No font files found in system fonts directory")
            return false
        }

        // Create a map for quick lookup
        val fontFileMap = allFontFiles.associateBy { it.name }

        // Try priority fonts first - this ensures system defaults are used
        for (fontName in priorityFontNames) {
            fontFileMap[fontName]?.let { fontFile ->
                try {
                    val success = asset.decode(fontFile.readBytes())
                    if (success) {
                        Log.d(TAG, "Successfully loaded system font: ${fontFile.name}")
                        return true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load font $fontName: ${e.message}")
                }
            }
        }

        // If no priority fonts worked, try all remaining system fonts
        for (fontFile in allFontFiles) {
            if (fontFile.name in priorityFontNames) continue // Already tried

            try {
                val success = asset.decode(fontFile.readBytes())
                if (success) {
                    Log.d(TAG, "Successfully loaded fallback font: ${fontFile.name}")
                    return true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load font ${fontFile.name}: ${e.message}")
            }
        }

        Log.w(TAG, "No suitable font found for asset among ${allFontFiles.size} system fonts")
        // Return false to indicate this loader can't handle the asset
        // The Rive runtime will try other loaders (e.g., CDN, embedded assets)
        return false
    }

    /**
     * Returns priority font list based on locale, matching Android's system defaults
     */
    private fun getPriorityFontsForLocale(locale: String): List<String> {
        return when {
            // Chinese Simplified
            locale.startsWith("zh_CN") || locale == "zh" -> listOf(
                "NotoSansSC-Regular.otf",
                "NotoSansCJK-Regular.ttc",
                "DroidSansFallback.ttf",
                "Roboto-Regular.ttf"
            )

            // Chinese Traditional
            locale.startsWith("zh_TW") || locale.startsWith("zh_HK") -> listOf(
                "NotoSansTC-Regular.otf",
                "NotoSansHK-Regular.otf",
                "NotoSansCJK-Regular.ttc",
                "DroidSansFallback.ttf",
                "Roboto-Regular.ttf"
            )

            // Japanese
            locale.startsWith("ja") -> listOf(
                "NotoSansJP-Regular.otf",
                "NotoSansCJK-Regular.ttc",
                "DroidSansFallback.ttf",
                "Roboto-Regular.ttf"
            )

            // Korean
            locale.startsWith("ko") -> listOf(
                "NotoSansKR-Regular.otf",
                "NotoSansCJK-Regular.ttc",
                "DroidSansFallback.ttf",
                "Roboto-Regular.ttf"
            )

            // Arabic
            locale.startsWith("ar") -> listOf(
                "NotoSansArabic-Regular.ttf",
                "NotoSansArabicUI-Regular.ttf",
                "NotoNaskhArabic-Regular.ttf",
                "NotoNaskhArabicUI-Regular.ttf",
                "Roboto-Regular.ttf"
            )

            // Thai
            locale.startsWith("th") -> listOf(
                "NotoSansThai-Regular.ttf",
                "NotoSansThaiUI-Regular.ttf",
                "Roboto-Regular.ttf"
            )

            // Russian (Cyrillic)
            locale.startsWith("ru") -> listOf(
                "Roboto-Regular.ttf",  // Roboto includes Cyrillic
                "DroidSans.ttf"
            )

            // Latin languages (en, de, fr, es, it, pt_BR, pt, tr, etc.)
            else -> listOf(
                "Roboto-Regular.ttf",
                "DroidSans.ttf"
            )
        }
    }

    companion object {
        private const val TAG = "AndroidSystemFontLoader"
    }
}
