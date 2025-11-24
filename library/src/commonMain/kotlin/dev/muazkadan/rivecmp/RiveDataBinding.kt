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

/**
 * Updates a number property on a Rive ViewModel instance.
 *
 * @param viewModelInstance The ViewModel instance obtained from onViewModelReady callback
 * @param propertyName The name of the number property to update
 * @param value The number value to set
 */
expect fun updateViewModelNumberProperty(
    viewModelInstance: Any?,
    propertyName: String,
    value: Double
)

/**
 * Updates a boolean property on a Rive ViewModel instance.
 *
 * @param viewModelInstance The ViewModel instance obtained from onViewModelReady callback
 * @param propertyName The name of the boolean property to update
 * @param value The boolean value to set
 */
expect fun updateViewModelBooleanProperty(
    viewModelInstance: Any?,
    propertyName: String,
    value: Boolean
)

/**
 * Updates a color property on a Rive ViewModel instance.
 *
 * @param viewModelInstance The ViewModel instance obtained from onViewModelReady callback
 * @param propertyName The name of the color property to update
 * @param value The color value in ARGB format (0xAARRGGBB)
 */
expect fun updateViewModelColorProperty(
    viewModelInstance: Any?,
    propertyName: String,
    value: UInt
)
