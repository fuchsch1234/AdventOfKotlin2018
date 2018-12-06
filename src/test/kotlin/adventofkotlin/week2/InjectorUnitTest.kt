package adventofkotlin.week2

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

interface I1 {
    fun foo(): String
}

class Injectee1 {
    val test = 5
}

class Injectee2 {
    val test = 6
}

class Injectee3 : I1 {
    override fun foo(): String = "Injectee3"
}

class InjectorUnitTests {

    @Nested
    inner class GetUnitTests {
        @Test
        fun `Simple injection works`() {
            Injector.providing {
                bind<Injectee1>() with { Injectee1() }
            }

            class TestClass {
                val injected: Injectee1 = get()
            }

            val test = TestClass()
            assertEquals(5, test.injected.test)
        }

        @Test
        fun `Multiple injection works`() {
            Injector.providing {
                bind<Injectee1>() with { Injectee1() }
                bind<Injectee2>() with { Injectee2() }
            }

            class TestClass {
                val injected1: Injectee1 = get()
                val injected2: Injectee2 = get()
            }

            val test = TestClass()
            assertEquals(5, test.injected1.test)
            assertEquals(6, test.injected2.test)
        }

        @Test
        fun `Interface injection works`() {
            Injector.providing {
                bind<I1>() with { Injectee3() }
                // Doesn't work because Injectee1 is not a subtype of I1
//                bind<I1>() with { Injectee1() }
            }
            class TestClass {
                val injected: I1 = get()
            }

            val test = TestClass()
            assertEquals("Injectee3", test.injected.foo())
        }
    }

    @Nested
    inner class InjectUnitTests {
        @Test
        fun `Simple lazy injection works`() {
            Injector.providing {
                bind<Injectee1>() with { Injectee1() }
            }

            class TestClass {
                val injected: Injectee1 by inject()
            }

            val test = TestClass()
            assertEquals(5, test.injected.test)
        }

        @Test
        fun `Lazy interface injection works`() {
            Injector.providing {
                bind<I1>() with { Injectee3() }
            }

            class TestClass {
                val injected: I1 by inject()
            }

            val test = TestClass()
            assertEquals("Injectee3", test.injected.foo())
        }

        @Test
        fun `Injection works with named variables`() {
            Injector.providing {
                bind<String>() with { "Default" }
                bind<String>("otherStr") with { "Other" }
            }

            class TestClass {
                val str: String by inject()
                val anotherStr: String by inject()
                val otherStr: String by inject()
            }

            val test = TestClass()
            assertEquals("Default", test.str)
            assertEquals("Default", test.anotherStr)
            assertEquals("Other", test.otherStr)
        }

        @Test
        fun `Rebinding values works`() {
            Injector.providing {
                bind<String>() with { "Default" }
                bind<String>("otherStr") with { "Other" }
            }

            class TestClass {
                val str: String by inject()
                val anotherStr: String by inject()
                val otherStr: String by inject()
            }

            val test = TestClass()
            assertEquals("Default", test.str)
            assertEquals("Default", test.anotherStr)
            assertEquals("Other", test.otherStr)

            Injector.providing {
                bind<String>() with { "Changed" }
            }
            val test2 = TestClass()
            assertEquals("Changed", test2.str)
            assertEquals("Changed", test2.anotherStr)
            assertEquals("Other", test2.otherStr)
        }
    }
}