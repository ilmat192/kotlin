/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.time

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

@SinceKotlin("1.3")
@ExperimentalTime
internal actual object MonotonicTimeSource : TimeSource {
    private val zero: Long = System.nanoTime()
    private fun read(): Long = System.nanoTime() - zero
    override fun toString(): String = "TimeSource(System.nanoTime())"

    actual override fun markNow(): DefaultTimeMark = DefaultTimeMark(read())
    actual fun elapsedFrom(timeMark: DefaultTimeMark): Duration =
        saturatingDiff(read(), timeMark.reading)

    // may have questionable contract
    actual fun adjustReading(timeMark: DefaultTimeMark, duration: Duration): DefaultTimeMark =
        DefaultTimeMark(saturatingAdd(timeMark.reading, duration))
}

private fun saturatingAdd(longNs: Long, duration: Duration): Long {
    val durationNs = duration.inWholeNanoseconds
    if ((longNs - 1) or 1 == Long.MAX_VALUE) { // MIN_VALUE or MAX_VALUE
        if (duration.isInfinite() && (longNs xor durationNs < 0)) throw IllegalArgumentException("Summing infinities of different signs")
        return longNs
    }
    if ((durationNs - 1) or 1 == Long.MAX_VALUE) {
        // TODO: check precision losses
        return (longNs + duration.toDouble(DurationUnit.NANOSECONDS)).toLong()
    }

    val result = longNs + durationNs
    if (((longNs xor result) and (durationNs xor result)) < 0) {
        return if (longNs < 0) Long.MIN_VALUE else Long.MAX_VALUE
    }
    return result
}

private fun saturatingDiff(valueNs: Long, originNs: Long): Duration {
    if ((originNs - 1) or 1 == Long.MAX_VALUE) { // MIN_VALUE or MAX_VALUE
        return -(originNs.toDuration(DurationUnit.DAYS)) // saturate to infinity
    }
    val result = valueNs - originNs
    if ((result xor valueNs) and (result xor originNs).inv() < 0) {
        val resultMs = valueNs / NANOS_IN_MILLIS - originNs / NANOS_IN_MILLIS
        val resultNs = valueNs % NANOS_IN_MILLIS - originNs % NANOS_IN_MILLIS
        return resultMs.milliseconds + resultNs.nanoseconds
    }
    return result.nanoseconds
}


@Suppress("ACTUAL_WITHOUT_EXPECT") // visibility
internal actual typealias DefaultTimeMarkReading = Long


//@SinceKotlin("1.3")
//@ExperimentalTime
//public inline fun measureTimeSpec(block: () -> Unit): Duration {
//    contract {
//        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
//    }
//
//    val mark = MonotonicTimeSource.markNow()
//    block()
//    return mark.elapsedNow()
//}
//
//@OptIn(ExperimentalTime::class)
//fun testMono() {
//    MonotonicTimeSource.measureTime {
//        println("test")
//    }
//    measureTimeSpec {
//        println("test2")
//    }
//}
//
