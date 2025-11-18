package dev.muazkadan.rivecmpdemo

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.ContextAssetLoader
import app.rive.runtime.kotlin.core.FileAsset
import java.io.File

@Composable
actual fun createCJKAssetLoader(): Any? {
    val context = LocalContext.current
    return HandleSystemFont(context)
}

actual fun updateViewModelText(viewModelInstance: Any?, text: String) {
    Log.d("ViewModelBinding", "updateViewModelText called with text: $text")

    if (viewModelInstance == null) {
        Log.e("ViewModelBinding", "viewModelInstance is null!")
        return
    }

    Log.d("ViewModelBinding", "viewModelInstance type: ${viewModelInstance::class.java.name}")

    try {
        // Use reflection to call getStringProperty("text")
        val getStringPropertyMethod = viewModelInstance::class.java.getMethod("getStringProperty", String::class.java)
        val stringProperty = getStringPropertyMethod.invoke(viewModelInstance, "text")
        Log.d("ViewModelBinding", "Got stringProperty: $stringProperty")

        if (stringProperty != null) {
            // Log all methods with their signatures to find the correct one
            val methods = stringProperty::class.java.methods
            val setMethods = methods.filter { it.name.contains("set", ignoreCase = true) }
            setMethods.forEach { method ->
                val params = method.parameterTypes.map { it.name }.joinToString()
                Log.d("ViewModelBinding", "Method: ${method.name}($params)")
            }

            // Try setValue with Object parameter
            val setValueMethod = stringProperty::class.java.getMethod("setValue", Any::class.java)
            setValueMethod.invoke(stringProperty, text)
            Log.d("ViewModelBinding", "Successfully set value using setValue(Object) to '$text'")
        } else {
            Log.e("ViewModelBinding", "String property 'text' not found")
        }
    } catch (e: NoSuchMethodException) {
        Log.e("ViewModelBinding", "Method not found", e)
    } catch (e: Exception) {
        Log.e("ViewModelBinding", "Error updating ViewModel text", e)
        e.printStackTrace()
    }
}

/**
 * Example Composable showing how to use AndroidView with RiveAnimationView
 * and a custom asset loader
 */
@Composable
fun RiveAnimationWithCustomLoader(
    @RawRes riveRes: Int,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            RiveAnimationView(context).apply {
                // Set custom asset loader before loading the resource
                setAssetLoader(HandleSystemFont(context))
                setRiveResource(riveRes)
            }
        },
        modifier = modifier
    )
}

/**
 * Custom asset loader that loads system fonts for Rive animations
 * Supports CJK (Chinese, Japanese, Korean) characters by loading appropriate fonts
 */
class HandleSystemFont(context: Context) : ContextAssetLoader(context) {
    override fun loadContents(asset: FileAsset, inBandBytes: ByteArray): Boolean {
        // List of fonts to try, prioritizing CJK-capable fonts
        val fontPaths = listOf(
            "/system/fonts/NotoSansCJK-Regular.ttc",  // Noto Sans CJK (common on newer Android)
            "/system/fonts/NotoSansSC-Regular.otf",    // Noto Sans Simplified Chinese
            "/system/fonts/NotoSansTC-Regular.otf",    // Noto Sans Traditional Chinese
            "/system/fonts/NotoSansJP-Regular.otf",    // Noto Sans Japanese
            "/system/fonts/NotoSansKR-Regular.otf",    // Noto Sans Korean
            "/system/fonts/DroidSansFallback.ttf",     // Fallback font with CJK support (older Android)
            "/system/fonts/Roboto-Regular.ttf"         // Last resort for Latin characters
        )

        for (fontPath in fontPaths) {
            val fontFile = File(fontPath)
            if (fontFile.exists()) {
                try {
                    val success = asset.decode(fontFile.readBytes())
                    if (success) {
                        Log.d("HandleSystemFont", "Successfully loaded font: $fontPath")
                        return true
                    }
                } catch (e: Exception) {
                    Log.w("HandleSystemFont", "Failed to load font $fontPath: ${e.message}")
                }
            }
        }

        Log.w("HandleSystemFont", "No suitable font found for asset")
        // Return false to indicate this loader can't handle the asset
        // The Rive runtime will try other loaders (e.g., CDN, embedded assets)
        return false
    }
}
