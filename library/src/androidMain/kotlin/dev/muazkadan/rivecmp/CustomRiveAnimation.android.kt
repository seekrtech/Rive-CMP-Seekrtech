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
import app.rive.runtime.kotlin.core.FileAssetLoader
import app.rive.runtime.kotlin.core.PlayableInstance
import app.rive.runtime.kotlin.core.RiveEvent
import app.rive.runtime.kotlin.core.ViewModelInstance
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
    onRiveEvent: ((String, Map<String, Any>) -> Unit)?,
    onViewModelReady: ((Any?) -> Unit)?,
    assetLoader: Any?
) {
    // Set up callbacks when composition or callbacks change
    LaunchedEffect(composition, onStateChanged, onRiveEvent, onViewModelReady) {
        composition?.setOnStateChangedListener(onStateChanged)
        composition?.setOnRiveEventListener(onRiveEvent)
        composition?.setOnViewModelReadyListener(onViewModelReady)
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

                        builder.build().also { view ->
                            android.util.Log.d("CustomRiveAnimation", "Factory: Built view")
                            android.util.Log.d("CustomRiveAnimation", "Factory: controller.file = ${view.controller.file}")
                            android.util.Log.d("CustomRiveAnimation", "Factory: activeArtboard = ${view.controller.activeArtboard}")
                        }
                    },
                    update = { view ->
                        android.util.Log.d("CustomRiveAnimation", "Update: Before connect")
                        android.util.Log.d("CustomRiveAnimation", "Update: controller.file = ${view.controller.file}")
                        android.util.Log.d("CustomRiveAnimation", "Update: activeArtboard = ${view.controller.activeArtboard}")

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
    onRiveEvent: ((String, Map<String, Any>) -> Unit)?,
    onViewModelReady: ((Any?) -> Unit)?,
    assetLoader: Any?
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
            riveView?.controller?.file?.release()
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

                // Initialize View Model if specified
                val instance = view.controller.file
                    ?.defaultViewModelForArtboard(view.controller.activeArtboard!!)
                    ?.createDefaultInstance()
                view.controller.stateMachines.first().viewModelInstance = instance
                onViewModelReady?.invoke(instance)
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
    onRiveEvent: ((String, Map<String, Any>) -> Unit)?,
    onViewModelReady: ((Any?) -> Unit)?,
    assetLoader: Any?
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
            riveView?.controller?.file?.release()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            // If we need ViewModel, disable autoplay and manually control it
            val needsViewModel = onViewModelReady != null

            val builder = RiveAnimationView.Builder(context)
                .setResource(byteArray)
                .setAlignment(alignment.toAndroidAlignment())
                .setFit(fit.toAndroidFit())
                .setAutoplay(if (needsViewModel) false else autoPlay)
                .setAutoBind(needsViewModel)

            // Set asset loader if provided
            (assetLoader as? FileAssetLoader)?.let {
                android.util.Log.d("CustomRiveAnimation", "Setting asset loader: $it")
                builder.setAssetLoader(it)
            }

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

                android.util.Log.d("CustomRiveAnimation", "Factory: View built, needsViewModel=$needsViewModel")

                if (needsViewModel) {
                    // Start playing to initialize state machines and trigger autoBind
                    view.play()
                    android.util.Log.d("CustomRiveAnimation", "Factory: Called play() to initialize")

                    // Immediately pause to prevent state changes before we get the ViewModel
                    view.pause()
                    android.util.Log.d("CustomRiveAnimation", "Factory: Called pause() to wait for ViewModel")

                    // Now get the ViewModel and invoke callback
                    view.post {
                        val instance = view.controller.stateMachines.firstOrNull()?.viewModelInstance
                        android.util.Log.d("CustomRiveAnimation", "Post: Auto-bound ViewModel = $instance")

                        onViewModelReady?.invoke(instance)

                        // Resume playback if autoPlay was requested
                        if (autoPlay) {
                            view.play()
                            android.util.Log.d("CustomRiveAnimation", "Post: Resumed playback")
                        }
                    }
                }
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