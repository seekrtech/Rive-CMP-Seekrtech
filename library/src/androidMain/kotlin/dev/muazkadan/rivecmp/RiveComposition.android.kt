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
    private var onViewModelReadyCallback: ((Any?) -> Unit)? = null

    // Current registered listeners
    private var currentStateListener: RiveFileController.Listener? = null
    private var currentEventListener: RiveFileController.RiveEventListener? = null

    // ViewModel instance
    private var viewModelInstance: Any? = null
    private var viewModelInitialized = false

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

    internal actual fun connectToAnimationView(animationView: Any?) {
        android.util.Log.d("RiveComposition", "connectToAnimationView called with: $animationView")
        animationViewRef = animationView as? RiveAnimationView
        android.util.Log.d("RiveComposition", "animationViewRef = $animationViewRef")

        // Set up callbacks when view is connected
        animationViewRef?.let { view ->
            android.util.Log.d("RiveComposition", "Setting up callbacks, controller = ${view.controller}")
            android.util.Log.d("RiveComposition", "controller.file = ${view.controller.file}")
            android.util.Log.d("RiveComposition", "controller.activeArtboard = ${view.controller.activeArtboard}")
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

            // Initialize ViewModel if not already done
            if (!viewModelInitialized && view.controller.file != null && view.controller.activeArtboard != null) {
                android.util.Log.d("RiveComposition", "Initializing ViewModel from connectToAnimationView")
                android.util.Log.d("RiveComposition", "controller.file = ${view.controller.file}")
                android.util.Log.d("RiveComposition", "activeArtboard = ${view.controller.activeArtboard}")

                val defaultViewModel = view.controller.file
                    ?.defaultViewModelForArtboard(view.controller.activeArtboard!!)
                android.util.Log.d("RiveComposition", "defaultViewModel = $defaultViewModel")

                viewModelInstance = defaultViewModel?.createDefaultInstance()
                android.util.Log.d("RiveComposition", "Created viewModelInstance = $viewModelInstance")

                viewModelInitialized = true
                onViewModelReadyCallback?.invoke(viewModelInstance)
                android.util.Log.d("RiveComposition", "Invoked onViewModelReadyCallback")
            }
        }
    }
} 