package adventofkotlin.week2

import java.lang.Exception
import kotlin.reflect.KProperty

inline fun <reified T> get(): T {
    val typeName = T::class.qualifiedName ?: T::class.toString()
    val constructor = Injector.typeRegistry[Injector.Key(typeName)]
    return constructor?.let { (it as ()->T).invoke() }
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
        val constructor = Injector.typeRegistry[Injector.Key(typeName, property.name)]
            ?: Injector.typeRegistry[Injector.Key(typeName)]
        return constructor?.let { Delegate(it as ()->T) }
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

    val typeRegistry = emptyMap<Key, ()->Any?>().toMutableMap()

    inline fun <reified T> bind(varName: String? = null): Binder<T> {
        val typeName = T::class.qualifiedName ?: T::class.toString()
        return Binder(Key(typeName, varName))
    }

    fun providing(f: Injector.()->Unit) {
        this.f()
    }

    class Binder<T>(private val key: Key) {
        infix fun <R: T> with(f: ()->R) {
            typeRegistry[key] = f
        }
    }

}

