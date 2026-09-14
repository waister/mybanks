package com.duduapps.mybanks

import org.junit.After
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
abstract class BaseRobolectricTest {

    @After
    open fun tearDown() {
        stopKoin()
    }
}
