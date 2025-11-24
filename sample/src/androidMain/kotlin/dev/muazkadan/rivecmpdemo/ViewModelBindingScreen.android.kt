package dev.muazkadan.rivecmpdemo

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView

/**
 * Example Composable showing how to use AndroidView with RiveAnimationView
 * and a custom system font loader.
 */
@Composable
fun RiveAnimationWithCustomLoader(
    @RawRes riveRes: Int,
    modifier: Modifier = Modifier,
    locale: String = "en"
) {
    val context = LocalContext.current
    val assetLoader = dev.muazkadan.rivecmp.createSystemFontLoader(locale)

    AndroidView(
        factory = { ctx ->
            RiveAnimationView(ctx).apply {
                // Set custom asset loader before loading the resource
                (assetLoader as? app.rive.runtime.kotlin.core.FileAssetLoader)?.let {
                    setAssetLoader(it)
                }
                setRiveResource(riveRes)
            }
        },
        modifier = modifier
    )
}
