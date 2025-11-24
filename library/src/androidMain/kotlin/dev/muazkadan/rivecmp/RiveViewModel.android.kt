package dev.muazkadan.rivecmp

import android.util.Log

/**
 * Updates a string property on a Rive ViewModel instance (Android implementation).
 *
 * Uses reflection to call getStringProperty(propertyName) and then setValue(value)
 * on the returned property object.
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
    Log.d(TAG, "updateViewModelStringProperty called - property: $propertyName, value: $value")

    if (viewModelInstance == null) {
        Log.e(TAG, "viewModelInstance is null!")
        return
    }

    Log.d(TAG, "viewModelInstance type: ${viewModelInstance::class.java.name}")

    try {
        // Use reflection to call getStringProperty(propertyName)
        val getStringPropertyMethod = viewModelInstance::class.java.getMethod(
            "getStringProperty",
            String::class.java
        )
        val stringProperty = getStringPropertyMethod.invoke(viewModelInstance, propertyName)
        Log.d(TAG, "Got stringProperty for '$propertyName': $stringProperty")

        if (stringProperty != null) {
            // Call setValue with the new string value
            val setValueMethod = stringProperty::class.java.getMethod("setValue", Any::class.java)
            setValueMethod.invoke(stringProperty, value)
            Log.d(TAG, "Successfully set property '$propertyName' to '$value'")
        } else {
            Log.e(TAG, "String property '$propertyName' not found")
        }
    } catch (e: NoSuchMethodException) {
        Log.e(TAG, "Method not found for property '$propertyName'", e)
    } catch (e: Exception) {
        Log.e(TAG, "Error updating ViewModel string property '$propertyName'", e)
        e.printStackTrace()
    }
}

private const val TAG = "RiveViewModel"
