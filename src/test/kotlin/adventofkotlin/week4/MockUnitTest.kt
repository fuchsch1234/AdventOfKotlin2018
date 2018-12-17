package adventofkotlin.week4

import jdk.internal.vm.compiler.word.LocationIdentity.any
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

interface Example {
    fun getInt(): Int
}

interface ExampleWithArguments {
    fun a(i: Int, str: String): Int
}

class MockUnitTest {
    @Test
    fun `setReturnValue returns correct value`() {
        val a = mock<Example>()
        setReturnValue( { a.getInt() }, 2)
        assertEquals(2, a.getInt())
    }

    @Test
    fun `setBody is executed on call`() {
        var i = 1
        val b = mock<Example>()
        setBody(
            { b.getInt() },
            { i++ })
        assertEquals(1, b.getInt())
        assertEquals(2, b.getInt())
        assertEquals(3, b.getInt())
    }

    @Test
    fun `mock works with arguments`() {
//        var i = 1
//        val b = mock<ExampleWithArguments>()
//        setBody({ b.a(any(), any()) }, { (a, str) -> print("$a, $str") })
//        setBody({ b.a(1, any()) }, { print("This is one!") })
//        b.a(10, "AAA") // Prints: 10, AAA
//        b.a(5, "AAA") // Prints: 5, AAA
//        b.a(1, "AAA") // Prints: This is one!
    }
}