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
fun <T> setReturnValue(func: ArgumentMatcher.() -> T, ret: T) = setBody(func, { ret })

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
fun <T> setBody(func: ArgumentMatcher.() -> T, body: (List<Any>) -> T) {
    val key = func.javaClass.name
    Mock.bindingHelper[key] = body as MockBody
    val argumentMatcher = ArgumentMatcher()
    Mock.argumentMatchers[key] = argumentMatcher
    // Call lambda to associate body with mocked method.
    try {
        argumentMatcher.func()
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

    private val mockFunctions: MutableList<MockFunction> = mutableListOf()

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
                val body = bindingHelper[key] ?: throw IllegalArgumentException("Missing body for $key")
                val argumentMatcher = argumentMatchers[key] ?: throw IllegalArgumentException("Missing argument matchers for $key")
                val arguments = args?.map { argumentMatcher.getArgumentMatcher(it)}?.toList() ?: emptyList()
                mockFunctions.add(MockFunction(method, arguments, body)
                    )
                // Bail out early to avoid calling the function body.
                throw Throwable()
            }
        }

        val argsList: MutableList<Any> = mutableListOf()
        args?.map { argsList.add(it) }
        val mock = mockFunctions.find { it.matches(method, argsList) }
        return mock?.invoke(argsList) ?: throw IllegalStateException("No body for mocked method $method set")
    }


    companion object {

        /**
         * Stores all bodies which are currently in process of binding to a mocked method.
         */
        val bindingHelper: MutableMap<String, MockBody> = mutableMapOf()

        val argumentMatchers: MutableMap<String, ArgumentMatcher> = mutableMapOf()
    }

}

/**
 * Stores a mock function body, as well as the method and argument matchers the body is applicable to.
 *
 * @param method The method this mock function is valid for.
 * @param argumentMatchers List of matchers that must be valid for method arguments.
 * @param body The mock function implementation.
 */
class MockFunction(private val method: Method,
                   private val argumentMatchers: List<Matcher>,
                   private val body: MockBody
) {

    /**
     * Checks if this mock function is valid for a method and argument combination.
     *
     * For the mock method to be valid, the method must be correct and every argument must match
     * its associated argument matcher.
     *
     * @param method Method to check against.
     * @param arguments Args the method was called with.
     */
    fun matches(method: Method, arguments: List<Any>): Boolean {
        return method == this.method &&
                argumentMatchers.size == arguments.size &&
                argumentMatchers.zip(arguments).all { it.first.matches(it.second) }
    }

    fun invoke(args: List<Any>): Any = body(args)

}
