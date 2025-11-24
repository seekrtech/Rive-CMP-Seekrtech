package dev.muazkadan.rivecmp

import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.controllers.RiveFileController
import app.rive.runtime.kotlin.core.PlayableInstance
import app.rive.runtime.kotlin.core.RiveEvent
import java.lang.ref.WeakReference

actual class RiveComposition internal actual constructor(
    spec: RiveCompositionSpec
) {
    internal actual val spec: RiveCompositionSpec = spec
    private var animationViewRef: RiveAnimationView? = null

    // Callback listeners
    private var onStateChangedCallback: ((stateMachineName: String, stateName: String) -> Unit)? = null
    private var onRiveEventCallback: ((eventName: String, properties: Map<String, Any>) -> Unit)? = null
    internal var onViewModelReadyCallback: ((Any?) -> Unit)? = null

    // Current registered listeners
    private var currentStateListener: RiveFileController.Listener? = null
    private var currentEventListener: RiveFileController.RiveEventListener? = null

    // ViewModel instance
    internal var viewModelInstance: Any? = null
    internal var viewModelInitialized = false

    actual fun setNumberInput(stateMachineName: String, name: String, value: Float) {
        animationViewRef?.setNumberState(
            stateMachineName = stateMachineName,
            inputName = name,
            value = value
        )
    }

    actual fun setBooleanInput(stateMachineName: String, name: String, value: Boolean) {
        animationViewRef?.setBooleanState(
            stateMachineName = stateMachineName,
            inputName = name,
            value = value
        )
    }

    actual fun setTriggerInput(stateMachineName: String, name: String) {
        animationViewRef?.fireState(stateMachineName = stateMachineName, inputName = name)
    }

    actual fun pause() {
        animationViewRef?.pause()
    }

    actual fun reset() {
        animationViewRef?.reset()
    }

    actual fun stop() {
        animationViewRef?.stop()
    }
    
    /**
     * Set a listener for state machine state changes
     * @param listener Callback invoked when state changes: (stateMachineName, stateName) -> Unit
     */
    fun setOnStateChangedListener(listener: ((String, String) -> Unit)?) {
        onStateChangedCallback = listener
        
        // Update the view's listener if already connected
        animationViewRef?.let { view ->
            // Unregister old listener
            currentStateListener?.let { view.unregisterListener(it) }
            
            // Register new listener if callback is provided
            currentStateListener = listener?.let { callback ->
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
            
            currentStateListener?.let { view.registerListener(it) }
        }
    }
    
    /**
     * Set a listener for Rive events
     * @param listener Callback invoked when Rive event occurs: (eventName, properties) -> Unit
     */
    fun setOnRiveEventListener(listener: ((String, Map<String, Any>) -> Unit)?) {
        onRiveEventCallback = listener
        
        // Update the view's listener if already connected
        animationViewRef?.let { view ->
            // Unregister old listener
            currentEventListener?.let { view.removeEventListener(it) }
            
            // Register new listener if callback is provided
            currentEventListener = listener?.let { callback ->
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
            
            currentEventListener?.let { view.addEventListener(it) }
        }
    }

    /**
     * Set a listener for ViewModel ready callback
     * @param listener Callback invoked when ViewModel instance is ready
     */
    fun setOnViewModelReadyListener(listener: ((Any?) -> Unit)?) {
        onViewModelReadyCallback = listener

        // If ViewModel is already initialized, invoke callback immediately
        if (viewModelInitialized) {
            listener?.invoke(viewModelInstance)
        }
    }

    internal fun connectToAnimationView(animationView: Any?, skipViewModelInit: Boolean = false) {
        animationViewRef = animationView as? RiveAnimationView

        // Set up callbacks when view is connected
        animationViewRef?.let { view ->
            updateListeners(view)

            // Initialize ViewModel if not already done and not skipped
            // Note: For ByteArray-based compositions with autoBind enabled, the ViewModel
            // is already initialized in the factory. We should skip initialization here.
            if (!skipViewModelInit && !viewModelInitialized && view.controller.file != null) {
                // First, check if there's an auto-bound ViewModel instance
                val autoBoundInstance = view.controller.stateMachines.firstOrNull()?.viewModelInstance

                if (autoBoundInstance != null) {
                    // Use the auto-bound instance
                    viewModelInstance = autoBoundInstance
                } else if (view.controller.activeArtboard != null) {
                    // Fall back to manual initialization if no auto-bound instance exists
                    val defaultViewModel = view.controller.file
                        ?.defaultViewModelForArtboard(view.controller.activeArtboard!!)

                    viewModelInstance = defaultViewModel?.createDefaultInstance()

                    // Manually bind to the state machine
                    view.controller.stateMachines.firstOrNull()?.let { stateMachine ->
                        stateMachine.viewModelInstance = viewModelInstance as? app.rive.runtime.kotlin.core.ViewModelInstance
                    }
                }

                viewModelInitialized = true
                onViewModelReadyCallback?.invoke(viewModelInstance)
            }
        }
    }

    internal actual fun connectToAnimationView(animationView: Any?) {
        connectToAnimationView(animationView, skipViewModelInit = false)
    }

    internal fun updateListeners(view: RiveAnimationView) {
        // Set state change listener
        onStateChangedCallback?.let { callback ->
            currentStateListener = object : RiveFileController.Listener {
                override fun notifyStateChanged(stateMachineName: String, stateName: String) {
                    callback(stateMachineName, stateName)
                }
                override fun notifyLoop(animation: PlayableInstance) {}
                override fun notifyPause(animation: PlayableInstance) {}
                override fun notifyPlay(animation: PlayableInstance) {}
                override fun notifyStop(animation: PlayableInstance) {}
            }
            currentStateListener?.let { view.registerListener(it) }
        }

        // Set Rive event listener
        onRiveEventCallback?.let { callback ->
            currentEventListener = object : RiveFileController.RiveEventListener {
                override fun notifyEvent(event: RiveEvent) {
                    val properties = buildMap<String, Any> {
                        put("type", event.type.toString())
                        put("delay", event.delay)
                        putAll(event.properties)
                    }
                    callback(event.name, properties)
                }
            }
            currentEventListener?.let { view.addEventListener(it) }
        }
    }
} 