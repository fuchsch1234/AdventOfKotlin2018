package adventofkotlin.week4

/**
 * Stores mapping from actual values to argument matchers.
 */
class ArgumentMatcher {

    private val argumentMatchers = mutableMapOf<Any, Matcher>()

    fun <T: Any> any(value: T): T {
        argumentMatchers[value] = AnyMatcher()
        return value
    }

    fun <T: Any> equals(value: T): T {
        argumentMatchers[value] = EqualsMatcher(value)
        return value
    }

    fun <T: Any> same(value: T): T {
        argumentMatchers[value] = SameMatcher(value)
        return value
    }

    fun <T: Any> getArgumentMatcher(value: T): Matcher = argumentMatchers[value] ?: EqualsMatcher(value)

}

interface Matcher {

    fun <T> matches(other: T): Boolean

}

/**
 * Matches every possible argument.
 */
class AnyMatcher: Matcher {

    override fun <T> matches(other: T): Boolean = true

}

/**
 * Matches if argument compares equal to value.
 *
 * @param value Value to compare against.
 */
class EqualsMatcher<T>(private val value: T): Matcher {

    override fun <T> matches(other: T): Boolean = value == other

}

/**
 * Matches if argument is same as value.
 *
 * @param value Value to compare against.
 */
class SameMatcher<T>(private val value: T): Matcher {

    override fun <T> matches(other: T): Boolean = value === other

}
