//
// Created by Muaz KADAN on 5.06.2025.
//

import Foundation
import RiveRuntime

@objcMembers public class RiveAnimationController: NSObject, RiveStateMachineDelegate {
    private(set) public var viewModel: RiveViewModel?
    private var riveView: RiveView?
    private var riveModel: RiveModel?
    private var pendingConfiguration: (url: String, autoPlay: Bool, artboardName: String?, stateMachineName: String?, fit: RiveFit, alignment: RiveAlignment)?

    // Callback closures for Kotlin interop
    public var onStateChanged: ((String?, String?) -> Void)?
    public var onRiveEvent: ((String?, [String: Any]?) -> Void)?
    public var onViewModelReady: ((Any?) -> Void)?

    override init() {
        super.init()
    }

    public func setAnimationItem(
        url: String,
        autoPlay: Bool,
        artboardName: String?,
        stateMachineName: String?,
        fit: RiveFit,
        alignment: RiveAlignment
    ) {
        // Store configuration for deferred creation
        pendingConfiguration = (url, autoPlay, artboardName, stateMachineName, fit, alignment)

        // Clean up previous resources
        releaseAnimation()

        // Create view model with proper configuration
        viewModel = RiveViewModel(
            webURL: url,
            stateMachineName: stateMachineName,
            fit: fit,
            alignment: alignment,
            autoPlay: autoPlay,
            loadCdn: false,
            artboardName: artboardName
        )

        // If view was already requested, create it now
        if riveView == nil {
            createRiveViewIfNeeded()
        }
    }

    public func setAnimationItem(
        data: NSData,
        autoPlay: Bool,
        artboardName: String?,
        stateMachineName: String?,
        fit: RiveFit,
        alignment: RiveAlignment,
        customLoader: ((RiveFileAsset, Data, RiveFactory) -> Bool)? = nil
    ) {
        // Clean up previous resources
        releaseAnimation()

        do {

            // Create RiveFile from NSData with custom loader
            let riveFile: RiveFile
            if let loader = customLoader {
                riveFile = try RiveFile(
                    data: data as Data,
                    loadCdn: true,
                    customAssetLoader: loader
                )
            } else {
                riveFile = try RiveFile(
                    data: data as Data,
                    loadCdn: true
                )
            }

            // Create RiveModel from RiveFile
            let model = RiveModel(riveFile: riveFile)
            self.riveModel = model

            // Set artboard if specified
            if let artboardName = artboardName {
                try model.setArtboard(artboardName)
            }

            // Create RiveViewModel from RiveModel
            if let stateMachineName = stateMachineName {
                viewModel = RiveViewModel(
                    model,
                    stateMachineName: stateMachineName,
                    fit: fit,
                    alignment: alignment,
                    autoPlay: autoPlay,
                    artboardName: artboardName
                )
            } else {
                viewModel = RiveViewModel(
                    model,
                    animationName: nil,
                    fit: fit,
                    alignment: alignment,
                    autoPlay: autoPlay,
                    artboardName: artboardName
                )
            }

            // If view was already requested, create it now
            if riveView == nil {
                createRiveViewIfNeeded()
            }
        } catch {
            print("RiveAnimationController: ERROR - Failed to create RiveFile from data: \(error)")
        }
    }

    private func createRiveViewIfNeeded() {
        guard let vm = viewModel else {
            return
        }

        riveView = vm.createRiveView()
        riveView?.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        
        // Register this controller as the state machine delegate
        riveView?.stateMachineDelegate = self
    }

    public func createAnimationView() -> UIView {
        // If we have a viewModel but no view, create the view
        if viewModel != nil && riveView == nil {
            createRiveViewIfNeeded()
        }

        // If we have a view, return it
        if let view = riveView {
            return view
        }

        // If no viewModel yet, return a placeholder and wait for configuration
        let placeholderView = RiveView()
        placeholderView.backgroundColor = UIColor.clear
        return placeholderView
    }

    public func updateView(_ view: UIView) {
        // Try to create view if we have viewModel but no riveView
        if viewModel != nil && riveView == nil {
            createRiveViewIfNeeded()
        }

        guard let vm = viewModel, let storedView = riveView else {
            return
        }
        vm.update(view: storedView)
    }

    public func releaseAnimation() {
        if riveView != nil {
            riveView?.removeFromSuperview()
        }
        riveView = nil
        viewModel = nil
        riveModel = nil
        pendingConfiguration = nil
    }

    // MARK: - View Model Support

    public func enableAutoBind() {
        viewModel?.riveModel?.enableAutoBind { [weak self] instance in
            self?.onViewModelReady?(instance)
        }
    }
    
    public func setNumberInput(_ name: String, _ value: Float) {
        viewModel?.setInput(name, value: value)
    }
    
    public func setBooleanInput(_ name: String, _ value: Bool) {
        viewModel?.setInput(name, value: value)
    }
    
    public func setTriggerInput(_ name: String) {
        viewModel?.triggerInput(name)
    }

    public func pause() {
        viewModel?.pause()
    }

    public func reset() {
        viewModel?.reset()
    }

    public func stop() {
        viewModel?.stop()
    }
    
    // MARK: - RiveStateMachineDelegate
    
    public func stateMachine(_ stateMachine: RiveStateMachineInstance, didChangeState stateName: String) {
        // Forward state change to Kotlin callback
        onStateChanged?(stateMachine.name(), stateName)
    }
    
    public func onRiveEventReceived(onRiveEvent riveEvent: RiveEvent) {
        // Extract event properties
        var properties: [String: Any] = [:]

        // Add event type
        if let generalEvent = riveEvent as? RiveGeneralEvent {
            properties["type"] = "general"

            // Add general event properties if available
            let eventProperties = generalEvent.properties()
            properties.merge(eventProperties) { (_, new) in new }
        } else if let openUrlEvent = riveEvent as? RiveOpenUrlEvent {
            properties["type"] = "openUrl"
            properties["url"] = openUrlEvent.url()
            properties["target"] = openUrlEvent.target()
        } else {
            properties["type"] = "unknown"
        }

        // Add delay if available
        properties["delay"] = riveEvent.delay()

        // Forward to Kotlin callback
        self.onRiveEvent?(riveEvent.name(), properties)
    }

    // MARK: - System Font Loader

    /// Creates a custom asset loader that loads all available system fonts
    public static func createSystemFontLoader() -> (RiveFileAsset, Data, RiveFactory) -> Bool {
        return { (asset: RiveFileAsset, data: Data, factory: RiveFactory) -> Bool in
            if let fontAsset = asset as? RiveFontAsset {
                // Try loading the default system font
                guard let font = factory.decodeFont(UIFont.systemFont(ofSize: 12)) else {
                    return false
                }
                fontAsset.font(font)
                return true
            }
            return false
        }
    }

    // MARK: - ViewModel Property Updates

    /// Updates a string property on a RiveDataBindingViewModelInstance
    /// - Parameters:
    ///   - viewModelInstance: The ViewModel instance (should be RiveDataBindingViewModelInstance)
    ///   - propertyName: The name of the string property to update
    ///   - value: The string value to set
    public static func updateStringProperty(viewModelInstance: Any?, propertyName: String, value: String) {
        guard let instance = viewModelInstance as? RiveDataBindingViewModel.Instance else {
            print("RiveAnimationController: viewModelInstance is not RiveDataBindingViewModel.Instance")
            return
        }

        // Get the string property by name
        guard let stringProperty = instance.stringProperty(fromPath: propertyName) else {
            print("RiveAnimationController: String property '\(propertyName)' not found")
            return
        }

        // Set the value
        stringProperty.value = value
        print("RiveAnimationController: Successfully set property '\(propertyName)' to '\(value)'")
    }

    /// Updates a number property on a RiveDataBindingViewModelInstance
    /// - Parameters:
    ///   - viewModelInstance: The ViewModel instance (should be RiveDataBindingViewModelInstance)
    ///   - propertyName: The name of the number property to update
    ///   - value: The number value to set
    public static func updateNumberProperty(viewModelInstance: Any?, propertyName: String, value: Double) {
        guard let instance = viewModelInstance as? RiveDataBindingViewModel.Instance else {
            print("RiveAnimationController: viewModelInstance is not RiveDataBindingViewModel.Instance")
            return
        }

        // Get the number property by name
        guard let numberProperty = instance.numberProperty(fromPath: propertyName) else {
            print("RiveAnimationController: Number property '\(propertyName)' not found")
            return
        }

        // Set the value
        numberProperty.value = Float(value)
        print("RiveAnimationController: Successfully set property '\(propertyName)' to '\(value)'")
    }

    /// Updates a boolean property on a RiveDataBindingViewModelInstance
    /// - Parameters:
    ///   - viewModelInstance: The ViewModel instance (should be RiveDataBindingViewModelInstance)
    ///   - propertyName: The name of the boolean property to update
    ///   - value: The boolean value to set
    public static func updateBooleanProperty(viewModelInstance: Any?, propertyName: String, value: Bool) {
        guard let instance = viewModelInstance as? RiveDataBindingViewModel.Instance else {
            print("RiveAnimationController: viewModelInstance is not RiveDataBindingViewModel.Instance")
            return
        }

        // Get the boolean property by name
        guard let booleanProperty = instance.booleanProperty(fromPath: propertyName) else {
            print("RiveAnimationController: Boolean property '\(propertyName)' not found")
            return
        }

        // Set the value
        booleanProperty.value = value
        print("RiveAnimationController: Successfully set property '\(propertyName)' to '\(value)'")
    }

    /// Updates a color property on a RiveDataBindingViewModelInstance
    /// - Parameters:
    ///   - viewModelInstance: The ViewModel instance (should be RiveDataBindingViewModelInstance)
    ///   - propertyName: The name of the color property to update
    ///   - value: The color value in ARGB format (0xAARRGGBB)
    public static func updateColorProperty(viewModelInstance: Any?, propertyName: String, value: UInt32) {
        guard let instance = viewModelInstance as? RiveDataBindingViewModel.Instance else {
            print("RiveAnimationController: viewModelInstance is not RiveDataBindingViewModel.Instance")
            return
        }

        // Get the color property by name
        guard let colorProperty = instance.colorProperty(fromPath: propertyName) else {
            print("RiveAnimationController: Color property '\(propertyName)' not found")
            return
        }

        // Convert ARGB UInt32 to UIColor
        let alpha = CGFloat((value >> 24) & 0xFF) / 255.0
        let red = CGFloat((value >> 16) & 0xFF) / 255.0
        let green = CGFloat((value >> 8) & 0xFF) / 255.0
        let blue = CGFloat(value & 0xFF) / 255.0
        let uiColor = UIColor(red: red, green: green, blue: blue, alpha: alpha)

        // Set the value
        colorProperty.value = uiColor
        print("RiveAnimationController: Successfully set property '\(propertyName)' to '0x\(String(value, radix: 16))'")
    }
}
