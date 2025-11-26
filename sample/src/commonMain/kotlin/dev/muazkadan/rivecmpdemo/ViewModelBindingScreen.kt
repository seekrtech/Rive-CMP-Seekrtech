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
import dev.muazkadan.rivecmp.RiveCompositionSpec
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.createSystemFontLoader
import dev.muazkadan.rivecmp.rememberRiveComposition
import dev.muazkadan.rivecmp.updateViewModelStringProperty
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import rivecmp.sample.generated.resources.Res

@OptIn(ExperimentalRiveCmpApi::class)
@Composable
fun ViewModelBindingScreen(onBack: () -> Unit) {
    var viewModelInstance by remember { mutableStateOf<Any?>(null) }

    // Get current system locale
    // TODO: This could be passed as a parameter to ViewModelBindingScreen if you want explicit control
//    val currentLocale = remember {
//        val locale = java.util.Locale.getDefault()
//        val language = locale.language
//        val country = locale.country
//        if (country.isNotEmpty()) "${language}_${country}" else language
//    }

    val assetLoader = createSystemFontLoader("zh_tw")

    // Load the Rive file using composition
    val composition by rememberRiveComposition {
        RiveCompositionSpec.byteArray(Res.readBytes("files/time_guard_intro.riv"))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CustomRiveAnimation(
            modifier = Modifier.fillMaxSize(),
            composition = composition,
            stateMachineName = "State Machine 1",
            artboardName = "autolayout",
            fit = RiveFit.LAYOUT,
            assetLoader = assetLoader,
            onViewModelReady = { instance ->
                viewModelInstance = instance
                println("ViewModelBinding: onViewModelReady called, instance = $instance")
            },
            onStateChanged = { stateMachineName, stateName ->
                // Update the "text" property on the ViewModel with the current state name
                println("ViewModelBinding: onStateChanged called - stateMachine: $stateMachineName, state: $stateName")
                when (stateName) {
                    "cut 1 intro" -> {
                        updateViewModelStringProperty(viewModelInstance, "Title", "Title 標題三行\nTitle 標題三行\nTitle 標題三行")
                        updateViewModelStringProperty(viewModelInstance, "Btn", "中文按鈕")
                    }
                    "cut 2 intro" -> {
                        updateViewModelStringProperty(viewModelInstance, "Title", "screen_time_intro_title_2")
                        updateViewModelStringProperty(viewModelInstance, "Btn", "screen_time_intro_btn_2")
                    }
                    "cut3 intro" -> {
                        updateViewModelStringProperty(viewModelInstance, "Title", "screen_time_intro_title_3")
                        updateViewModelStringProperty(viewModelInstance, "Btn", "screen_time_intro_btn_3")
                    }
                    "cut4 intro" -> {
                        updateViewModelStringProperty(viewModelInstance, "Title", "screen_time_intro_title_4")
                        updateViewModelStringProperty(viewModelInstance, "Btn", "screen_time_intro_btn_4")
                    }
                    "cut5 intro" -> {
                        updateViewModelStringProperty(viewModelInstance, "Title", "screen_time_intro_title_5")
                        updateViewModelStringProperty(viewModelInstance, "Btn", "screen_time_intro_btn_5")
                    }
                    "Exit" -> {
                        updateViewModelStringProperty(viewModelInstance, "Title", "screen_time_intro_title_5")
                        updateViewModelStringProperty(viewModelInstance, "Btn", "screen_time_intro_btn_5")
                    }
                    else -> {
                       // do nothing
                    }
                }
            }
        )

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
