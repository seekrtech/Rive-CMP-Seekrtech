package dev.muazkadan.rivecmpdemo

import androidx.compose.runtime.Composable

@Composable
actual fun createCJKAssetLoader(): Any? {
    // iOS asset loader support not implemented yet
    return null
}

actual fun updateViewModelText(viewModelInstance: Any?, text: String) {
    // iOS implementation - will need to be implemented based on iOS Rive SDK
    // For now, this is a placeholder
}
