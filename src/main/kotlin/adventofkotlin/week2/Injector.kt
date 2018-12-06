package adventofkotlin.week2

import java.lang.Exception
import kotlin.reflect.KProperty

inline fun <reified T> get(): T {
    val typeName = T::class.qualifiedName ?: T::class.toString()
    val constructor = Injector.typeRegistry[typeName]
    if (constructor != null) {
        val ctor = constructor as ()->T
        return ctor()
    }
    throw UnsatisfiableDependency("Cannot create object of type $typeName")
}

inline fun <reified T> inject(): Delegate<T> {
    val typeName = T::class.qualifiedName ?: T::class.toString()
    val constructor = Injector.typeRegistry[typeName]
    if (constructor != null) {
        val ctor = constructor as ()->T
        return Delegate(ctor)
    }
    throw UnsatisfiableDependency("Cannot create object of type $typeName")
}

class UnsatisfiableDependency(what: String) : Exception(what)

class Delegate<T>(private val constructor: ()->T) {

    private val value: T by lazy { constructor() }

    operator fun getValue(thisref: Any?, property: KProperty<*>): T {
        return value
    }
}

object Injector {

    val typeRegistry = emptyMap<String, ()->Any?>().toMutableMap()

    inline fun <reified T> bind(): Binder<T> {
        val typeName = T::class.qualifiedName ?: T::class.toString()
        return Binder(typeName)
    }

    fun providing(f: Injector.()->Unit) {
        this.f()
    }

    class Binder<T>(private val typeName: String) {
        infix fun <R: T> with(f: ()->R) {
            typeRegistry[typeName] = f
        }
    }


}

