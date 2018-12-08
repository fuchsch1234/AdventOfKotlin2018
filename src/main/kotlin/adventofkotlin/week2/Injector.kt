package adventofkotlin.week2

import java.lang.Exception
import kotlin.reflect.KProperty

inline fun <reified T> get(): T {
    val typeName = T::class.qualifiedName ?: T::class.toString()
    val constructor = Injector.getConstructor<T>(Injector.Key(typeName))
    return constructor?.invoke()
        ?: throw UnsatisfiableDependency("Cannot create object of type $typeName")
}

inline fun <reified T> inject(): DelegateProvider<T> {
    val typeName = T::class.qualifiedName ?: T::class.toString()
    return DelegateProvider(typeName)
}

class UnsatisfiableDependency(what: String) : Exception(what)

class DelegateProvider<T>(private val typeName: String) {

    operator fun provideDelegate(thisref: Any?, property: KProperty<*>): Delegate<T> {
        // Try to find constructor for type and property name, or general constructor for type
        val constructor = Injector.getConstructor<T>(Injector.Key(typeName, property.name))
            ?: Injector.getConstructor<T>(Injector.Key(typeName))
        return constructor?.let { Delegate(it) }
            ?: throw UnsatisfiableDependency("Cannot create object of type $typeName")
    }

}

class Delegate<T>(private val constructor: ()->T) {

    private val value: T by lazy { constructor() }

    operator fun getValue(thisref: Any?, property: KProperty<*>): T {
        return value
    }

}

object Injector {

    data class Key(val typeName: String, val varName: String? = null)

    private val typeRegistry = emptyMap<Key, ()->Any?>().toMutableMap()

    inline fun <reified T> bind(varName: String? = null): Binder<T> {
        val typeName = T::class.qualifiedName ?: T::class.toString()
        return Binder(Key(typeName, varName))
    }

    fun providing(f: Injector.()->Unit) {
        this.f()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getConstructor(key: Key): (() -> T)? = typeRegistry[key] as (() -> T)?

    fun reset() {
        typeRegistry.clear()
    }

    class Binder<T>(private val key: Key) {
        infix fun <R: T> with(f: ()->R) {
            typeRegistry[key] = f
        }
    }

}

