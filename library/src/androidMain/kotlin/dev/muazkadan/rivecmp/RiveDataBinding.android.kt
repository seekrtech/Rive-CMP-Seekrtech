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

actual fun updateViewModelNumberProperty(
    viewModelInstance: Any?,
    propertyName: String,
    value: Double
) {
    Log.d(TAG, "updateViewModelNumberProperty called - property: $propertyName, value: $value")

    if (viewModelInstance == null) {
        Log.e(TAG, "viewModelInstance is null!")
        return
    }

    Log.d(TAG, "viewModelInstance type: ${viewModelInstance::class.java.name}")

    try {
        // Use reflection to call getNumberProperty(propertyName)
        val getNumberPropertyMethod = viewModelInstance::class.java.getMethod(
            "getNumberProperty",
            String::class.java
        )
        val numberProperty = getNumberPropertyMethod.invoke(viewModelInstance, propertyName)
        Log.d(TAG, "Got numberProperty for '$propertyName': $numberProperty")

        if (numberProperty != null) {
            // Call setValue with the new number value
            val setValueMethod = numberProperty::class.java.getMethod("setValue", Any::class.java)
            setValueMethod.invoke(numberProperty, value.toFloat())
            Log.d(TAG, "Successfully set property '$propertyName' to '$value'")
        } else {
            Log.e(TAG, "Number property '$propertyName' not found")
        }
    } catch (e: NoSuchMethodException) {
        Log.e(TAG, "Method not found for property '$propertyName'", e)
    } catch (e: Exception) {
        Log.e(TAG, "Error updating ViewModel number property '$propertyName'", e)
        e.printStackTrace()
    }
}

actual fun updateViewModelBooleanProperty(
    viewModelInstance: Any?,
    propertyName: String,
    value: Boolean
) {
    Log.d(TAG, "updateViewModelBooleanProperty called - property: $propertyName, value: $value")

    if (viewModelInstance == null) {
        Log.e(TAG, "viewModelInstance is null!")
        return
    }

    Log.d(TAG, "viewModelInstance type: ${viewModelInstance::class.java.name}")

    try {
        // Use reflection to call getBooleanProperty(propertyName)
        val getBooleanPropertyMethod = viewModelInstance::class.java.getMethod(
            "getBooleanProperty",
            String::class.java
        )
        val booleanProperty = getBooleanPropertyMethod.invoke(viewModelInstance, propertyName)
        Log.d(TAG, "Got booleanProperty for '$propertyName': $booleanProperty")

        if (booleanProperty != null) {
            // Call setValue with the new boolean value
            val setValueMethod = booleanProperty::class.java.getMethod("setValue", Any::class.java)
            setValueMethod.invoke(booleanProperty, value)
            Log.d(TAG, "Successfully set property '$propertyName' to '$value'")
        } else {
            Log.e(TAG, "Boolean property '$propertyName' not found")
        }
    } catch (e: NoSuchMethodException) {
        Log.e(TAG, "Method not found for property '$propertyName'", e)
    } catch (e: Exception) {
        Log.e(TAG, "Error updating ViewModel boolean property '$propertyName'", e)
        e.printStackTrace()
    }
}

actual fun updateViewModelColorProperty(
    viewModelInstance: Any?,
    propertyName: String,
    value: UInt
) {
    Log.d(TAG, "updateViewModelColorProperty called - property: $propertyName, value: ${value.toString(16)}")

    if (viewModelInstance == null) {
        Log.e(TAG, "viewModelInstance is null!")
        return
    }

    Log.d(TAG, "viewModelInstance type: ${viewModelInstance::class.java.name}")

    try {
        // Use reflection to call getColorProperty(propertyName)
        val getColorPropertyMethod = viewModelInstance::class.java.getMethod(
            "getColorProperty",
            String::class.java
        )
        val colorProperty = getColorPropertyMethod.invoke(viewModelInstance, propertyName)
        Log.d(TAG, "Got colorProperty for '$propertyName': $colorProperty")

        if (colorProperty != null) {
            // Call setValue with the new color value (as Int)
            val setValueMethod = colorProperty::class.java.getMethod("setValue", Any::class.java)
            setValueMethod.invoke(colorProperty, value.toInt())
            Log.d(TAG, "Successfully set property '$propertyName' to '${value.toString(16)}'")
        } else {
            Log.e(TAG, "Color property '$propertyName' not found")
        }
    } catch (e: NoSuchMethodException) {
        Log.e(TAG, "Method not found for property '$propertyName'", e)
    } catch (e: Exception) {
        Log.e(TAG, "Error updating ViewModel color property '$propertyName'", e)
        e.printStackTrace()
    }
}

private const val TAG = "RiveDataBinding"
