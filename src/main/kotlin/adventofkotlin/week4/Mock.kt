package adventofkotlin.week4

import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

typealias MockBody = (List<Any>) -> Any

/**
 * Convenience function to set a singleton return value for a mocked method.
 *
 * This function expects its first argument to be a lambda
 * calling a single method of a mock object created with mock().
 *
 * @param func A lambda calling a method of a mock object.
 * @param ret The return value all calls to this method should return.
 */
fun <T> setReturnValue(func: () -> T, ret: T) = setBody(func, { ret })

/**
 * Sets a body for a mocked method.
 *
 * This function expects its first argument to be a lambda
 * calling a single method of a mock object created with mock().
 *
 * The function uses the first lambdas class name to uniquely identify this particular invocation
 * of setBody and associates the body lambda with it through the Mock class bindingHelper. Then
 * it executes func, which in turn will call a method on the mock method. The mock object
 * will search its stack trace to find the body lambda and associates it with the method called
 * in func.
 *
 * @param func A lambda calling a method of a mock object.
 * @param body A lambda which is executed every time the mocked method is called.
 */
@Suppress("UNCHECKED_CAST")
fun <T> setBody(func: () -> T, body: (List<Any>) -> T) {
    val key = func.javaClass.name
    Mock.bindingHelper[key] = body as MockBody
    // Call lambda to associate body with mocked method.
    try {
        func()
    } catch (t: Throwable) {
    } finally {
        // Remove binding to avoid checking it again and again in mock.invoke().
        Mock.bindingHelper.remove(key)
    }
}

/**
 * Creates an interface proxy as a mock object for this interface.
 *
 * @return A mock object for interface T.
 */
inline fun <reified T> mock(): T {
    val clazz = T::class.java
    // Register a new mock object as the proxies invocation handler
    return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), Mock()) as T
}

/**
 * Handles invocation of all mocked method calls.
 *
 */
class Mock: InvocationHandler {

    var bindings: MutableMap<Method, MockBody> = mutableMapOf()

    /**
     * Handles a method calls for a proxy object.
     *
     * Whenever an interface method is called on a mock object returned by mock(). This
     * class invoke method is called. If a method is invoked when a new binding is available,
     * i.e. bindingHelper contains some values, invoke searches its stackTrace to check if
     * it has been called from within setBody and identifies a new binding for the currently called
     * method.
     *
     * @param proxy Proxy object on which method is called.
     * @param method Method called on the proxy object.
     * @param args Arguments to method call.
     */
    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any {
        val method: Method = method ?: throw IllegalArgumentException("invoke called without method")
        if (!bindingHelper.isEmpty()) {
            // Check if a new binding is available for this method, i.e. if method is called from setBody.
            val key = Throwable().stackTrace.find { bindingHelper.containsKey(it.className) }?.className
            if (key != null) {
                // Should never fail, because key was checked before using containsKey()
                bindings[method] = bindingHelper[key] ?: throw IllegalArgumentException("Missing body for $key")
                // Bail out early to avoid calling the function body.
                throw Throwable()
            }
        }

        val argsList: MutableList<Any> = mutableListOf()
        args?.map { argsList.add(it) }
        return bindings[method]?.invoke(argsList) ?: throw IllegalStateException("No body for mocked method $method set")
    }


    companion object {

        /**
         * Stores all bodies which are currently in process of binding to a mocked method.
         */
        var bindingHelper: MutableMap<String, MockBody> = mutableMapOf()
    }

}

fun <T: Any> any(value: T): T {
    return value
}
