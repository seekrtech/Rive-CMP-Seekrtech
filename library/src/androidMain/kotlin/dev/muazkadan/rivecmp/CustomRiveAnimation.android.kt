package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.controllers.RiveFileController
import app.rive.runtime.kotlin.core.PlayableInstance
import app.rive.runtime.kotlin.core.RiveEvent
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.core.toAndroidFit
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.core.toAndroidAlignment
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi

@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    composition: RiveComposition?,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?,
    onStateChanged: ((String, String) -> Unit)?,
    onRiveEvent: ((String, Map<String, Any>) -> Unit)?
) {
    // Set up callbacks when composition or callbacks change
    LaunchedEffect(composition, onStateChanged, onRiveEvent) {
        composition?.setOnStateChangedListener(onStateChanged)
        composition?.setOnRiveEventListener(onRiveEvent)
    }
    
    if (composition != null) {
        when (val spec = composition.spec) {
            is RiveUrlCompositionSpec -> {
                AndroidView(
                    modifier = modifier,
                    factory = { context ->
                        val builder = RiveAnimationView.Builder(context)
                            .setResource(spec.url)
                            .setAlignment(alignment.toAndroidAlignment())
                            .setFit(fit.toAndroidFit())
                            .setAutoplay(autoPlay)

                        // Set artboard name if provided
                        artboardName?.let {
                            builder.setArtboardName(it)
                        }

                        // Set state machine name if provided
                        stateMachineName?.let {
                            builder.setStateMachineName(it)
                        }

                        builder.build()
                    },
                    update = { view ->
                        composition.connectToAnimationView(view)
                    }
                )
            }
            is RiveByteArrayCompositionSpec -> {
                AndroidView(
                    modifier = modifier,
                    factory = { context ->
                        val builder = RiveAnimationView.Builder(context)
                            .setResource(spec.byteArray)
                            .setAlignment(alignment.toAndroidAlignment())
                            .setFit(fit.toAndroidFit())
                            .setAutoplay(autoPlay)

                        // Set artboard name if provided
                        artboardName?.let {
                            builder.setArtboardName(it)
                        }

                        // Set state machine name if provided
                        stateMachineName?.let {
                            builder.setStateMachineName(it)
                        }

                        builder.build()
                    },
                    update = { view ->
                        composition.connectToAnimationView(view)
                    }
                )
            }
        }
    }
}

@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    url: String,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?,
    onStateChanged: ((String, String) -> Unit)?,
    onRiveEvent: ((String, Map<String, Any>) -> Unit)?
) {
    var riveView by remember { mutableStateOf<RiveAnimationView?>(null) }
    var currentStateListener by remember { mutableStateOf<RiveFileController.Listener?>(null) }
    var currentEventListener by remember { mutableStateOf<RiveFileController.RiveEventListener?>(null) }
    
    // Create state change listener
    val stateListener = remember(onStateChanged) {
        onStateChanged?.let { callback ->
            object : RiveFileController.Listener {
                override fun notifyStateChanged(stateMachineName: String, stateName: String) {
                    callback(stateMachineName, stateName)
                }
                override fun notifyLoop(animation: PlayableInstance) {}
                override fun notifyPause(animation: PlayableInstance) {}
                override fun notifyPlay(animation: PlayableInstance) {}
                override fun notifyStop(animation: PlayableInstance) {}
            }
        }
    }
    
    // Create event listener
    val eventListener = remember(onRiveEvent) {
        onRiveEvent?.let { callback ->
            object : RiveFileController.RiveEventListener {
                override fun notifyEvent(event: RiveEvent) {
                    val properties = buildMap<String, Any> {
                        put("type", event.type.toString())
                        put("delay", event.delay)
                        putAll(event.properties)
                    }
                    callback(event.name, properties)
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            currentStateListener?.let { riveView?.unregisterListener(it) }
            currentEventListener?.let { riveView?.removeEventListener(it) }
        }
    }
    
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val builder = RiveAnimationView.Builder(context)
                .setResource(url)
                .setAlignment(alignment.toAndroidAlignment())
                .setFit(fit.toAndroidFit())
                .setAutoplay(autoPlay)

            // Set artboard name if provided
            artboardName?.let {
                builder.setArtboardName(it)
            }

            // Set state machine name if provided
            stateMachineName?.let {
                builder.setStateMachineName(it)
            }

            builder.build().also { view ->
                riveView = view
            }
        },
        update = { view ->
            // Update state listener
            if (currentStateListener != stateListener) {
                currentStateListener?.let { view.unregisterListener(it) }
                stateListener?.let { view.registerListener(it) }
                currentStateListener = stateListener
            }
            
            // Update event listener
            if (currentEventListener != eventListener) {
                currentEventListener?.let { view.removeEventListener(it) }
                eventListener?.let { view.addEventListener(it) }
                currentEventListener = eventListener
            }
        }
    )
}

@ExperimentalRiveCmpApi
@Composable
actual fun CustomRiveAnimation(
    modifier: Modifier,
    byteArray: ByteArray,
    alignment: RiveAlignment,
    autoPlay: Boolean,
    artboardName: String?,
    fit: RiveFit,
    stateMachineName: String?,
    onStateChanged: ((String, String) -> Unit)?,
    onRiveEvent: ((String, Map<String, Any>) -> Unit)?
) {
    var riveView by remember { mutableStateOf<RiveAnimationView?>(null) }
    var currentStateListener by remember { mutableStateOf<RiveFileController.Listener?>(null) }
    var currentEventListener by remember { mutableStateOf<RiveFileController.RiveEventListener?>(null) }
    
    // Create state change listener
    val stateListener = remember(onStateChanged) {
        onStateChanged?.let { callback ->
            object : RiveFileController.Listener {
                override fun notifyStateChanged(stateMachineName: String, stateName: String) {
                    callback(stateMachineName, stateName)
                }
                override fun notifyLoop(animation: PlayableInstance) {}
                override fun notifyPause(animation: PlayableInstance) {}
                override fun notifyPlay(animation: PlayableInstance) {}
                override fun notifyStop(animation: PlayableInstance) {}
            }
        }
    }
    
    // Create event listener
    val eventListener = remember(onRiveEvent) {
        onRiveEvent?.let { callback ->
            object : RiveFileController.RiveEventListener {
                override fun notifyEvent(event: RiveEvent) {
                    val properties = buildMap<String, Any> {
                        put("type", event.type.toString())
                        put("delay", event.delay)
                        putAll(event.properties)
                    }
                    callback(event.name, properties)
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            currentStateListener?.let { riveView?.unregisterListener(it) }
            currentEventListener?.let { riveView?.removeEventListener(it) }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val builder = RiveAnimationView.Builder(context)
                .setResource(byteArray)
                .setAlignment(alignment.toAndroidAlignment())
                .setFit(fit.toAndroidFit())
                .setAutoplay(autoPlay)

            // Set artboard name if provided
            artboardName?.let {
                builder.setArtboardName(it)
            }

            // Set state machine name if provided
            stateMachineName?.let {
                builder.setStateMachineName(it)
            }

            builder.build().also { view ->
                riveView = view
            }
        },
        update = { view ->
            // Update state listener
            if (currentStateListener != stateListener) {
                currentStateListener?.let { view.unregisterListener(it) }
                stateListener?.let { view.registerListener(it) }
                currentStateListener = stateListener
            }
            
            // Update event listener
            if (currentEventListener != eventListener) {
                currentEventListener?.let { view.removeEventListener(it) }
                eventListener?.let { view.addEventListener(it) }
                currentEventListener = eventListener
            }
        }
    )
}