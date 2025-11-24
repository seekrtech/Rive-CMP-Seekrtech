package dev.muazkadan.rivecmpdemo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.muazkadan.rivecmp.CustomRiveAnimation
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.createSystemFontLoader
import dev.muazkadan.rivecmp.updateViewModelStringProperty
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import rivecmp.sample.generated.resources.Res

@OptIn(ExperimentalRiveCmpApi::class)
@Composable
fun ViewModelBindingScreen(onBack: () -> Unit) {
    var viewModelInstance by remember { mutableStateOf<Any?>(null) }
    var riveByteArray by remember { mutableStateOf<ByteArray?>(null) }

    // Get current system locale
    // TODO: This could be passed as a parameter to ViewModelBindingScreen if you want explicit control
//    val currentLocale = remember {
//        val locale = java.util.Locale.getDefault()
//        val language = locale.language
//        val country = locale.country
//        if (country.isNotEmpty()) "${language}_${country}" else language
//    }

    val assetLoader = createSystemFontLoader("en")

    // Load the Rive file
    LaunchedEffect(Unit) {
        riveByteArray = Res.readBytes("files/relax_onboarding_autolayout_test.riv")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        riveByteArray?.let { bytes ->
            CustomRiveAnimation(
                modifier = Modifier.fillMaxSize(),
                byteArray = bytes,
                stateMachineName = "State Machine 1",
                artboardName = "1194x1194",
                fit = RiveFit.NONE,
                assetLoader = assetLoader,
                onViewModelReady = { instance ->
                    viewModelInstance = instance
                    println("ViewModelBinding: onViewModelReady called, instance = $instance")
                },
                onStateChanged = { stateMachineName, stateName ->
                    // Update the "text" property on the ViewModel with the current state name
                    println("ViewModelBinding: onStateChanged called - stateMachine: $stateMachineName, state: $stateName")
                    when(stateName) {
                        "cut 1" -> updateViewModelStringProperty(viewModelInstance, "text", "我們來試試中文如何")
                        "cut 2-1" -> updateViewModelStringProperty(viewModelInstance, "text", "很長很長很長很長很長很長的中文內容看看會不會出事呢")
                        else -> updateViewModelStringProperty(viewModelInstance, "text", stateName)
                    }
                }
            )
        }

        // Back button
        FloatingActionButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text("Back")
        }
    }
}
