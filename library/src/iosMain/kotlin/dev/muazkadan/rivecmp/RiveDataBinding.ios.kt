package dev.muazkadan.rivecmp

import kotlinx.cinterop.ExperimentalForeignApi
import nativeIosShared.RiveAnimationController

/**
 * Updates a string property on a Rive ViewModel instance (iOS implementation).
 *
 * The viewModelInstance should be of type RiveDataBindingViewModelInstance.
 *
 * @param viewModelInstance The ViewModel instance obtained from onViewModelReady callback
 * @param propertyName The name of the string property to update
 * @param value The string value to set
 */
@OptIn(ExperimentalForeignApi::class)
actual fun updateViewModelStringProperty(
    viewModelInstance: Any?,
    propertyName: String,
    value: String
) {
    RiveAnimationController.updateStringPropertyWithViewModelInstance(
        viewModelInstance = viewModelInstance,
        propertyName = propertyName,
        value = value
    )
}

/**
 * Updates a number property on a Rive ViewModel instance (iOS implementation).
 *
 * The viewModelInstance should be of type RiveDataBindingViewModelInstance.
 *
 * @param viewModelInstance The ViewModel instance obtained from onViewModelReady callback
 * @param propertyName The name of the number property to update
 * @param value The number value to set
 */
@OptIn(ExperimentalForeignApi::class)
actual fun updateViewModelNumberProperty(
    viewModelInstance: Any?,
    propertyName: String,
    value: Double
) {
    RiveAnimationController.updateNumberPropertyWithViewModelInstance(
        viewModelInstance = viewModelInstance,
        propertyName = propertyName,
        value = value
    )
}

/**
 * Updates a boolean property on a Rive ViewModel instance (iOS implementation).
 *
 * The viewModelInstance should be of type RiveDataBindingViewModelInstance.
 *
 * @param viewModelInstance The ViewModel instance obtained from onViewModelReady callback
 * @param propertyName The name of the boolean property to update
 * @param value The boolean value to set
 */
@OptIn(ExperimentalForeignApi::class)
actual fun updateViewModelBooleanProperty(
    viewModelInstance: Any?,
    propertyName: String,
    value: Boolean
) {
    RiveAnimationController.updateBooleanPropertyWithViewModelInstance(
        viewModelInstance = viewModelInstance,
        propertyName = propertyName,
        value = value
    )
}

/**
 * Updates a color property on a Rive ViewModel instance (iOS implementation).
 *
 * The viewModelInstance should be of type RiveDataBindingViewModelInstance.
 *
 * @param viewModelInstance The ViewModel instance obtained from onViewModelReady callback
 * @param propertyName The name of the color property to update
 * @param value The color value in ARGB format (0xAARRGGBB)
 */
@OptIn(ExperimentalForeignApi::class)
actual fun updateViewModelColorProperty(
    viewModelInstance: Any?,
    propertyName: String,
    value: UInt
) {
    RiveAnimationController.updateColorPropertyWithViewModelInstance(
        viewModelInstance = viewModelInstance,
        propertyName = propertyName,
        value = value
    )
}
