// !LANGUAGE: +ContextReceivers

class A
class B
class C

context(A)
fun B.f() {}

fun main() {
    val b = B()

    <!NO_CONTEXT_RECEIVER!>b.f()<!>
    with(A()) {
        b.f()
    }
    with(C()) {
        <!NO_CONTEXT_RECEIVER!>b.f()<!>
    }
}
