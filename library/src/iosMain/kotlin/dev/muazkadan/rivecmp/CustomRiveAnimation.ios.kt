package dev.muazkadan.rivecmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import dev.muazkadan.rivecmp.core.RiveAlignment
import dev.muazkadan.rivecmp.core.RiveFit
import dev.muazkadan.rivecmp.core.toIosAlignment
import dev.muazkadan.rivecmp.core.toIosFit
import dev.muazkadan.rivecmp.utils.ExperimentalRiveCmpApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import nativeIosShared.RiveAnimationController
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
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
    LaunchedEffect(composition, onStateChanged, onRiveEvent) {
        composition?.setOnStateChangedListener(onStateChanged)
        composition?.setOnRiveEventListener(onRiveEvent)
    }
    
    if (composition != null) {
        when (val spec = composition.spec) {
            is RiveUrlCompositionSpec -> {
                val animationController = remember(spec.url, autoPlay, artboardName, fit, stateMachineName, alignment) {
                    val controller = RiveAnimationController()
                    controller.setAnimationItemWithUrl(
                        url = spec.url,
                        autoPlay = autoPlay,
                        artboardName = artboardName,
                        stateMachineName = stateMachineName,
                        fit = fit.toIosFit(),
                        alignment = alignment.toIosAlignment()
                    )
                    composition.connectToAnimationView(controller)
                    controller
                }

                DisposableEffect(Unit) {
                    onDispose {
                        animationController.releaseAnimation()
                    }
                }

                UIKitView(
                    factory = {
                        animationController.createAnimationView()
                    },
                    modifier = modifier,
                    update = { view ->
                        animationController.updateView(view)
                    }
                )
            }
            is RiveByteArrayCompositionSpec -> {
                val animationController = remember(spec.byteArray, autoPlay, artboardName, fit, stateMachineName, alignment) {
                    val controller = RiveAnimationController()

                    // Convert ByteArray to NSData
                    val nsData = spec.byteArray.usePinned { pinned ->
                        NSData.create(
                            bytes = pinned.addressOf(0),
                            length = spec.byteArray.size.toULong()
                        )
                    }

                    controller.setAnimationItemWithData(
                        data = nsData,
                        autoPlay = autoPlay,
                        artboardName = artboardName,
                        stateMachineName = stateMachineName,
                        fit = fit.toIosFit(),
                        alignment = alignment.toIosAlignment(),
                        customLoader = null
                    )
                    composition.connectToAnimationView(controller)
                    controller
                }

                DisposableEffect(Unit) {
                    onDispose {
                        animationController.releaseAnimation()
                    }
                }

                UIKitView(
                    factory = {
                        animationController.createAnimationView()
                    },
                    modifier = modifier,
                    update = { view ->
                        animationController.updateView(view)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
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
    val animationController = remember(url, autoPlay, artboardName, fit, stateMachineName, alignment) {
        val controller = RiveAnimationController()
        controller.setAnimationItemWithUrl(
            url = url,
            autoPlay = autoPlay,
            artboardName = artboardName,
            stateMachineName = stateMachineName,
            fit = fit.toIosFit(),
            alignment = alignment.toIosAlignment()
        )
        controller
    }

    // Set up View Model if callback is provided
    // The callback receives the actual ViewModel instance from enableAutoBind
    LaunchedEffect(animationController, onViewModelReady) {
        onViewModelReady?.let { callback ->
            animationController.setOnViewModelReady { instance ->
                callback(instance)
            }
            animationController.enableAutoBind()
        }
    }

    // Set up callbacks when controller or callbacks change
    LaunchedEffect(animationController, onStateChanged, onRiveEvent) {
        animationController.setOnStateChanged(onStateChanged?.let { callback ->
            { stateMachineName: String?, stateName: String? ->
                callback(stateMachineName ?: "", stateName ?: "")
            }
        })
        animationController.setOnRiveEvent(onRiveEvent?.let { callback ->
            { eventName: String?, properties: Map<*, *>? ->
                @Suppress("UNCHECKED_CAST")
                val kotlinMap = properties as? Map<String, Any> ?: emptyMap()
                callback(eventName ?: "", kotlinMap)
            }
        })
    }

    DisposableEffect(Unit) {
        onDispose {
            animationController.releaseAnimation()
        }
    }

    UIKitView(
        factory = {
            animationController.createAnimationView()
        },
        modifier = modifier,
        update = { view ->
            animationController.updateView(view)
        }
    )
}


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
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
    val animationController = remember(byteArray, autoPlay, artboardName, fit, stateMachineName, alignment, assetLoader) {
        val controller = RiveAnimationController()

        // Convert ByteArray to NSData
        val nsData = byteArray.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = byteArray.size.toULong()
            )
        }

        // Use custom loader if provided, otherwise use default system font loader
        val customLoader = (assetLoader as? Function3<*, *, *, *>)?.let { loader ->
            { asset: Any?, data: Any?, factory: Any? ->
                @Suppress("UNCHECKED_CAST")
                (loader as Function3<Any?, Any?, Any?, Boolean>)(asset, data, factory)
            }
        } ?: nativeIosShared.RiveAnimationController.createSystemFontLoader()

        controller.setAnimationItemWithData(
            data = nsData,
            autoPlay = autoPlay,
            artboardName = artboardName,
            stateMachineName = stateMachineName,
            fit = fit.toIosFit(),
            alignment = alignment.toIosAlignment(),
            customLoader = customLoader
        )
        controller
    }

    // Set up View Model if callback is provided
    // The callback receives the actual ViewModel instance from enableAutoBind
    LaunchedEffect(animationController, onViewModelReady) {
        onViewModelReady?.let { callback ->
            animationController.setOnViewModelReady { instance ->
                callback(instance)
            }
            animationController.enableAutoBind()
        }
    }

    // Set up callbacks when controller or callbacks change
    LaunchedEffect(animationController, onStateChanged, onRiveEvent) {
        animationController.setOnStateChanged(onStateChanged?.let { callback ->
            { stateMachineName: String?, stateName: String? ->
                callback(stateMachineName ?: "", stateName ?: "")
            }
        })
        animationController.setOnRiveEvent(onRiveEvent?.let { callback ->
            { eventName: String?, properties: Map<*, *>? ->
                @Suppress("UNCHECKED_CAST")
                val kotlinMap = properties as? Map<String, Any> ?: emptyMap()
                callback(eventName ?: "", kotlinMap)
            }
        })
    }

    DisposableEffect(Unit) {
        onDispose {
            animationController.releaseAnimation()
        }
    }

    UIKitView(
        factory = {
            animationController.createAnimationView()
        },
        modifier = modifier,
        update = { view ->
            animationController.updateView(view)
        }
    )
}