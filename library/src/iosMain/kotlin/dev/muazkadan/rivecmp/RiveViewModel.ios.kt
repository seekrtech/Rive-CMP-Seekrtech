package dev.muazkadan.rivecmp

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
actual fun updateViewModelStringProperty(
    viewModelInstance: Any?,
    propertyName: String,
    value: String
) {
    RiveAnimationController.updateStringProperty(
        viewModelInstance = viewModelInstance,
        propertyName = propertyName,
        value = value
    )
}
