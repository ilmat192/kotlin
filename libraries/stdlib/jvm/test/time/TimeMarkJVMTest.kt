/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.time

import kotlin.test.*
import kotlin.time.*

@OptIn(ExperimentalTime::class)
class TimeMarkJVMTest {

    @Test
    fun longDurationElapsed() {
        TimeMarkTest().testLongDisplacement(TimeSource.Monotonic, { waitDuration -> Thread.sleep(waitDuration.inWholeMilliseconds) })
    }
}