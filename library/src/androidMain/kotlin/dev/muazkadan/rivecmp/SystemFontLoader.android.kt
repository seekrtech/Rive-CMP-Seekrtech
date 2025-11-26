package dev.muazkadan.rivecmp

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import app.rive.runtime.kotlin.core.ContextAssetLoader
import app.rive.runtime.kotlin.core.FileAsset
import app.rive.runtime.kotlin.core.FontAsset
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
        // Only handle text/font assets
        if (!(asset is FontAsset)) {
            return false
        }

        val systemFontsDir = File("/system/fonts")

        if (!systemFontsDir.exists() || !systemFontsDir.isDirectory) {
            Log.w(TAG, "System fonts directory not found")
            return false
        }

        // Get font family name from the asset
        val fontFamily = asset.name
        Log.d(TAG, "Loading font family: $fontFamily for locale: $locale")

        // Get all available font files
        val allFontFiles = systemFontsDir.listFiles { file ->
            file.extension.lowercase() in listOf("ttf", "otf", "ttc")
        } ?: emptyArray()

        if (allFontFiles.isEmpty()) {
            Log.w(TAG, "No font files found in system fonts directory")
            return false
        }

        // Try to find fonts matching the requested family name
        val matchingFonts = findFontsForFamily(fontFamily, allFontFiles)

        if (matchingFonts.isNotEmpty()) {
            // Try matching fonts first
            for (fontFile in matchingFonts) {
                try {
                    val success = asset.decode(fontFile.readBytes())
                    if (success) {
                        Log.d(TAG, "Successfully loaded font: ${fontFile.name} for family: $fontFamily")
                        return true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load font ${fontFile.name}: ${e.message}")
                }
            }
        }

        // If no matching font found by family name, fall back to locale-based priority font families
        val priorityFontFamilies = getPriorityFontFamiliesForLocale(locale)

        for (familyName in priorityFontFamilies) {
            val priorityFonts = findFontsForFamily(familyName, allFontFiles)
            for (fontFile in priorityFonts) {
                try {
                    val success = asset.decode(fontFile.readBytes())
                    if (success) {
                        Log.d(TAG, "Successfully loaded priority font: ${fontFile.name}")
                        return true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load font ${fontFile.name}: ${e.message}")
                }
            }
        }

        // Last resort: try all remaining system fonts
        for (fontFile in allFontFiles) {
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
        return false
    }

    /**
     * Finds font files that match the requested font family name.
     * Supports various font weight and style variants (Regular, Bold, Italic, etc.)
     */
    private fun findFontsForFamily(familyName: String, allFonts: Array<File>): List<File> {
        val normalizedFamily = familyName.lowercase().replace(" ", "")

        // Find all fonts that match the family name
        return allFonts.filter { fontFile ->
            val fileName = fontFile.nameWithoutExtension.lowercase().replace("-", "").replace(" ", "")
            fileName.startsWith(normalizedFamily) || fileName.contains(normalizedFamily)
        }.sortedWith(compareBy(
            // Prioritize exact matches and common weights
            { file ->
                val name = file.nameWithoutExtension.lowercase()
                when {
                    name.contains("regular") -> 0
                    name.contains("medium") -> 1
                    name.contains("bold") -> 2
                    name.contains("light") -> 3
                    else -> 4
                }
            },
            { it.name }
        ))
    }

    /**
     * Returns priority font family names based on locale, matching Android's system defaults
     */
    private fun getPriorityFontFamiliesForLocale(locale: String): List<String> {
        val normalizedLocale = locale.lowercase()
        return when {
            // Chinese Simplified
            normalizedLocale.startsWith("zh_cn") || normalizedLocale == "zh" -> listOf(
                "NotoSansSC",
                "NotoSansCJK",
                "DroidSansFallback",
                "Roboto"
            )

            // Chinese Traditional
            normalizedLocale.startsWith("zh_tw") || normalizedLocale.startsWith("zh_hk") -> listOf(
                "NotoSansTC",
                "NotoSansHK",
                "NotoSansCJK",
                "DroidSansFallback",
                "Roboto"
            )

            // Japanese
            normalizedLocale.startsWith("ja") -> listOf(
                "NotoSansJP",
                "NotoSansCJK",
                "DroidSansFallback",
                "Roboto"
            )

            // Korean
            normalizedLocale.startsWith("ko") -> listOf(
                "NotoSansKR",
                "NotoSansCJK",
                "DroidSansFallback",
                "Roboto"
            )

            // Arabic
            normalizedLocale.startsWith("ar") -> listOf(
                "NotoSansArabic",
                "NotoSansArabicUI",
                "NotoNaskhArabic",
                "NotoNaskhArabicUI",
                "Roboto"
            )

            // Thai
            normalizedLocale.startsWith("th") -> listOf(
                "NotoSansThai",
                "NotoSansThaiUI",
                "Roboto"
            )

            // Russian (Cyrillic)
            normalizedLocale.startsWith("ru") -> listOf(
                "Roboto",  // Roboto includes Cyrillic
                "DroidSans"
            )

            // Latin languages (en, de, fr, es, it, pt_BR, pt, tr, etc.)
            else -> listOf(
                "Roboto",
                "DroidSans"
            )
        }
    }

    companion object {
        private const val TAG = "AndroidSystemFontLoader"
    }
}
