package adventofkotlin.week4

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

interface Example {
    fun getInt(): Int
}

interface ExampleWithArguments {
    fun a(i: Int, str: String): String
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
        setBody( { b.getInt() }, { i++ })
        assertEquals(1, b.getInt())
        assertEquals(2, b.getInt())
        assertEquals(3, b.getInt())
    }

    @Test
    fun `mock works with arguments`() {
        val b = mock<ExampleWithArguments>()
        setBody({ b.a(1, any("test")) }, { "This is one!" })
        setBody({ b.a(any(1), any("test")) }, { (a, str) -> "$a, $str" })
        assertEquals("10, AAA", b.a(10, "AAA"))
        assertEquals("5, AAA", b.a(5, "AAA"))
        assertEquals("This is one!", b.a(1, "AAA"))
    }
}