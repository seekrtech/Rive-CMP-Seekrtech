package dev.muazkadan.rivecmp

/**
 * Updates a string property on a Rive ViewModel instance.
 *
 * @param viewModelInstance The ViewModel instance obtained from onViewModelReady callback
 * @param propertyName The name of the string property to update
 * @param value The string value to set
 */
expect fun updateViewModelStringProperty(
    viewModelInstance: Any?,
    propertyName: String,
    value: String
)
