package com.duduapps.mybanks.features.feedback

import app.cash.turbine.test
import com.duduapps.mybanks.data.repository.FeedbackRepository
import com.duduapps.mybanks.data.repository.PreferencesRepository
import com.duduapps.mybanks.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FeedbackViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val feedbackRepository: FeedbackRepository = mockk(relaxed = true)
    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { preferencesRepository.name } returns "Tester"
        every { preferencesRepository.email } returns "tester@example.com"
        every { preferencesRepository.comments } returns ""
    }

    @Test
    fun `given empty comments, when onSubmit called, then shows error`() {
        val viewModel = FeedbackViewModel(
            feedbackRepository = feedbackRepository,
            preferencesRepository = preferencesRepository,
        )

        viewModel.onCommentsChanged("")
        viewModel.onSubmit()

        assertNotNull(viewModel.uiState.value.commentsError)
    }

    @Test
    fun `given valid input, when onSubmit called successfully, then sends message and emits ShowSuccessDialog`() = runTest {
        coEvery { feedbackRepository.sendFeedback(any(), any(), any()) } returns Result.success("Mensagem enviada!")

        val viewModel = FeedbackViewModel(
            feedbackRepository = feedbackRepository,
            preferencesRepository = preferencesRepository,
        )
        viewModel.onNameChanged("Tester")
        viewModel.onEmailChanged("tester@example.com")
        viewModel.onCommentsChanged("Excelente app!")

        viewModel.events.test {
            viewModel.onSubmit()
            val event = awaitItem()
            assertTrue(event is FeedbackEvent.ShowSuccessDialog)
            assertEquals("Mensagem enviada!", (event as FeedbackEvent.ShowSuccessDialog).message)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { feedbackRepository.sendFeedback("Tester", "tester@example.com", "Excelente app!") }
    }
}
