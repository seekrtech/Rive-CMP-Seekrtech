package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable

/**
 * Creates an iOS system font asset loader for the specified locale.
 *
 * On iOS, UIFont.systemFont automatically provides appropriate fallback fonts
 * for all scripts (Latin, CJK, Arabic, Thai, etc.), so the locale parameter
 * is currently not used but is kept for API consistency with Android.
 *
 * @param locale Language/locale code (currently unused on iOS)
 * @return Swift closure for loading system fonts
 */
@Composable
actual fun createSystemFontLoader(locale: String): Any? {
    // iOS uses UIFont.systemFont which has automatic fallback for all scripts
    // The locale parameter could be used in the future to select specific font variants
    return nativeIosShared.RiveAnimationController.createSystemFontLoader()
}
