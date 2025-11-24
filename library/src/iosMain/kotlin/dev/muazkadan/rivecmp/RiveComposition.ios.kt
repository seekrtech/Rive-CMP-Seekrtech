package dev.muazkadan.rivecmp

import kotlinx.cinterop.ExperimentalForeignApi
import nativeIosShared.RiveAnimationController
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
actual class RiveComposition internal actual constructor(
    spec: RiveCompositionSpec
) {
    internal actual val spec: RiveCompositionSpec = spec
    private var controllerRef: RiveAnimationController? = null
    
    // Callback listeners
    private var onStateChangedCallback: ((stateMachineName: String, stateName: String) -> Unit)? = null
    private var onRiveEventCallback: ((eventName: String, properties: Map<String, Any>) -> Unit)? = null
    private var onViewModelReadyCallback: ((Any?) -> Unit)? = null

    // ViewModel instance
    private var viewModelInitialized = false

    actual fun setNumberInput(stateMachineName: String, name: String, value: Float) {
        controllerRef?.setNumberInput(name, value)
    }

    actual fun setBooleanInput(stateMachineName: String, name: String, value: Boolean) {
        controllerRef?.setBooleanInput(name, value)
    }

    actual fun setTriggerInput(stateMachineName: String, name: String) {
        controllerRef?.setTriggerInput(name)
    }

    actual fun pause() {
        controllerRef?.pause()
    }

    actual fun reset() {
        controllerRef?.reset()
    }

    actual fun stop() {
        controllerRef?.stop()
    }
    
    /**
     * Set a listener for state machine state changes
     * @param listener Callback invoked when state changes: (stateMachineName, stateName) -> Unit
     */
    fun setOnStateChangedListener(listener: ((String, String) -> Unit)?) {
        onStateChangedCallback = listener
        
        // Update the controller's callback if already connected
        controllerRef?.setOnStateChanged(listener?.let { callback ->
            { stateMachineName: String?, stateName: String? ->
                callback(stateMachineName ?: "", stateName ?: "")
            }
        })
    }
    
    /**
     * Set a listener for Rive events
     * @param listener Callback invoked when Rive event occurs: (eventName, properties) -> Unit
     */
    fun setOnRiveEventListener(listener: ((String, Map<String, Any>) -> Unit)?) {
        onRiveEventCallback = listener

        // Update the controller's callback if already connected
        controllerRef?.setOnRiveEvent(listener?.let { callback ->
            { eventName: String?, properties: Map<*, *>? ->
                @Suppress("UNCHECKED_CAST")
                val kotlinMap = properties as? Map<String, Any> ?: emptyMap()
                callback(eventName ?: "", kotlinMap)
            }
        })
    }

    /**
     * Set a listener for ViewModel ready callback
     * @param listener Callback invoked when ViewModel instance is ready
     */
    fun setOnViewModelReadyListener(listener: ((Any?) -> Unit)?) {
        onViewModelReadyCallback = listener

        // Update the controller's callback if already connected
        if (!viewModelInitialized) {
            controllerRef?.let { controller ->
                listener?.let { callback ->
                    controller.setOnViewModelReady { instance ->
                        callback(instance)
                        viewModelInitialized = true
                    }
                    controller.enableAutoBind()
                }
            }
        }
    }

    internal actual fun connectToAnimationView(animationView: Any?) {
        controllerRef = animationView as? RiveAnimationController

        // Set up callbacks when controller is connected
        controllerRef?.let { controller ->
            // Set state change callback
            onStateChangedCallback?.let { callback ->
                controller.setOnStateChanged { stateMachineName: String?, stateName: String? ->
                    callback(stateMachineName ?: "", stateName ?: "")
                }
            }

            // Set Rive event callback
            onRiveEventCallback?.let { callback ->
                controller.setOnRiveEvent { eventName: String?, properties: Map<*, *>? ->
                    @Suppress("UNCHECKED_CAST")
                    val kotlinMap = properties as? Map<String, Any> ?: emptyMap()
                    callback(eventName ?: "", kotlinMap)
                }
            }

            // Set ViewModel ready callback
            if (!viewModelInitialized) {
                onViewModelReadyCallback?.let { callback ->
                    controller.setOnViewModelReady { instance ->
                        callback(instance)
                        viewModelInitialized = true
                    }
                    controller.enableAutoBind()
                }
            }
        }
    }
} 