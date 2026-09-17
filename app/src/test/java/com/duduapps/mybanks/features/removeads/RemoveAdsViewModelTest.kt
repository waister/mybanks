package com.duduapps.mybanks.features.removeads

import app.cash.turbine.test
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.duduapps.mybanks.utils.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RemoveAdsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { preferencesRepository.havePlan() } returns false
        every { preferencesRepository.planVideoDuration } returns 86400000L
        every { preferencesRepository.planVideoMillis } returns 0L
    }

    @Test
    fun `given active reward, when onUserEarnedReward called, then updates preferences and emits success`() = runTest {
        val viewModel = RemoveAdsViewModel(preferencesRepository = preferencesRepository)

        viewModel.events.test {
            viewModel.onUserEarnedReward()
            val event = awaitItem()
            assertEquals(RemoveAdsEvent.AdRewardSuccess, event)
            cancelAndIgnoreRemainingEvents()
        }

        verify { preferencesRepository.planVideoMillis = any() }
    }

    @Test
    fun `when onWatchAdClicked called, then emits ShowRewardedAd event`() = runTest {
        val viewModel = RemoveAdsViewModel(preferencesRepository = preferencesRepository)

        viewModel.events.test {
            viewModel.onWatchAdClicked()
            val event = awaitItem()
            assertEquals(RemoveAdsEvent.ShowRewardedAd, event)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
