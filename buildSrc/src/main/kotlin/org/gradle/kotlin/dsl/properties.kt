package org.gradle.kotlin.dsl

import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtraPropertiesExtension
import kotlin.reflect.KProperty

@PublishedApi
internal val Any.ext: ExtraPropertiesExtension
    get() = (this as ExtensionAware).extensions.getByName("ext") as ExtraPropertiesExtension

object Extensions {
    operator inline fun <reified T> getValue(thisRef: Any, property: KProperty<*>): T? =
        if (!thisRef.ext.has(property.name)) null else thisRef.ext.get(property.name) as T

    operator inline fun <reified T> setValue(thisRef: Any, property: KProperty<*>, value: T) {
        thisRef.ext.set(property.name, value)
    }
}