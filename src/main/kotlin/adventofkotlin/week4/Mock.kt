package adventofkotlin.week4

import java.lang.reflect.Method
import java.lang.reflect.Proxy

fun <T> setReturnValue(func: (args: Any?) -> T, ret: T) {

}

fun <T> setBody(func: (args: Any?) -> T, body: () -> T) {

}

inline fun <reified T> mock(): T {
    val clazz = T::class.java
    val proxy: T = Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz)) {
            proxy: Any, method: Method, args: Array<Any>? -> "foo"
    } as T
    return proxy
}
