package com.kepler88d.diffplugin.common

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey
import java.text.MessageFormat
import java.util.MissingResourceException
import java.util.ResourceBundle

private const val BUNDLE = "messages.MyMessageBundle"

internal object MyMessageBundle {
    private val classLoader = MyMessageBundle::class.java.classLoader

    @JvmStatic
    fun message(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any?): @Nls String {
        return messageOrNull(key, *params) ?: key
    }

    @JvmStatic
    fun messageOrNull(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any?): @Nls String? {
        val bundle = loadBundle() ?: return null
        if (!bundle.containsKey(key)) return null
        val pattern = bundle.getString(key)
        return if (params.isEmpty()) pattern else MessageFormat(pattern, DynamicBundle.getLocale()).format(params)
    }

    private fun loadBundle(): ResourceBundle? {
        return loadBundleOrNull() ?: run {
            ResourceBundle.clearCache(classLoader)
            loadBundleOrNull()
        }
    }

    private fun loadBundleOrNull(): ResourceBundle? {
        return try {
            ResourceBundle.getBundle(BUNDLE, DynamicBundle.getLocale(), classLoader)
        } catch (_: MissingResourceException) {
            null
        }
    }
}
