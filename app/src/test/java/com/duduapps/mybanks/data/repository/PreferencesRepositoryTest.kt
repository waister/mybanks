package com.duduapps.mybanks.data.repository

import androidx.test.core.app.ApplicationProvider
import com.duduapps.mybanks.BaseRobolectricTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PreferencesRepositoryTest : BaseRobolectricTest() {

    private lateinit var repository: PreferencesRepository

    @Before
    fun setUp() {
        repository = PreferencesRepositoryImpl(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `given new preferences, when ensureDeviceId called, then generates and persists valid id`() {
        repository.deviceId = ""
        val id = repository.ensureDeviceId()

        assertTrue(id.isNotEmpty())
        assertEquals(id, repository.deviceId)
    }

    @Test
    fun `given user logged in, when logout called, then isLogged becomes false`() {
        repository.isLogged = true
        assertTrue(repository.isLogged)

        repository.logout()
        assertFalse(repository.isLogged)
    }

    @Test
    fun `given active plan time, when havePlan called, then returns true`() {
        repository.planVideoMillis = System.currentTimeMillis()
        repository.planVideoDuration = 86400000L

        assertTrue(repository.havePlan())
    }

    @Test
    fun `given expired plan time, when havePlan called, then returns false`() {
        repository.planVideoMillis = System.currentTimeMillis() - 100000000L
        repository.planVideoDuration = 86400000L

        assertFalse(repository.havePlan())
    }

    @Test
    fun `given cached feedback data, when clearFeedbackCache called, then fields are emptied`() {
        repository.name = "Test Name"
        repository.email = "test@example.com"
        repository.comments = "Some comments"

        repository.clearFeedbackCache()

        assertTrue(repository.name.isEmpty())
        assertTrue(repository.email.isEmpty())
        assertTrue(repository.comments.isEmpty())
    }
}
