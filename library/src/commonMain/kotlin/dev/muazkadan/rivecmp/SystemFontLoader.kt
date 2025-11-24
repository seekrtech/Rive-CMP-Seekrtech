package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable

/**
 * Creates a system font asset loader for the specified locale.
 * This loader provides platform-specific system fonts that match the native OS rendering.
 *
 * Supported locales:
 * - Latin languages: en, de, fr, es, it, pt, pt_BR, tr, ru
 * - Chinese: zh, zh_CN (Simplified), zh_TW (Traditional), zh_HK
 * - Japanese: ja
 * - Korean: ko
 * - Arabic: ar
 * - Thai: th
 *
 * @param locale Language/locale code (e.g., "en", "zh_CN", "zh_TW", "ja", "ko", "ar", "th")
 * @return Platform-specific asset loader instance, or null if not supported
 */
@Composable
expect fun createSystemFontLoader(locale: String): Any?
