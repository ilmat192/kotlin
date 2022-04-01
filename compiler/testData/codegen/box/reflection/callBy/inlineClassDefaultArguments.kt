// IGNORE_BACKEND: JS_IR, JS, NATIVE, WASM
// IGNORE_BACKEND: JS_IR_ES6
// WITH_REFLECT

import kotlin.test.assertEquals

inline class A(val x: Int)

fun test1(x: A = A(0)) = "OK"
fun test2(
    arg00: Long = 0L, arg01: Long = 0L, arg02: Long = 0L, arg03: Long = 0L, arg04: Long = 0L,
    arg05: Long = 0L, arg06: Long = 0L, arg07: Long = 0L, arg08: Long = 0L, arg09: Long = 0L,
    arg10: Long = 0L, arg11: Long = 0L, arg12: Long = 0L, arg13: Long = 0L, arg14: Long = 0L,
    arg15: Long = 0L, arg16: Long = 0L, arg17: Long = 0L, arg18: Long = 0L, arg19: Long = 0L,
    arg20: Long = 0L, arg21: Long = 0L, arg22: Long = 0L, arg23: Long = 0L, arg24: Long = 0L,
    arg25: Long = 0L, arg26: Long = 0L, arg27: Long = 0L, arg28: Long = 0L, arg29: Long = 0L,
    arg30: Long = 0L, arg31: Long = 0L, x: A = A(0)
) = "OK"

fun box(): String {
    assertEquals("OK", ::test1.callBy(mapOf()))
    assertEquals("OK", ::test2.callBy(mapOf()))

    return "OK"
}
