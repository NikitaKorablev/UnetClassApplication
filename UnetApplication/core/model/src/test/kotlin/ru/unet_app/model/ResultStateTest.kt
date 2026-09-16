package ru.unet_app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultStateTest {

    @Test
    fun testSuccess() {
        val result = ResultState.Success<String, String>("success data")

        assertTrue(result is ResultState.Success)
        val success = result as ResultState.Success<String, String>
        assertEquals("success data", success.data)
    }

    @Test
    fun testError() {
        val result = ResultState.Error<String, String>("error message")

        assertTrue(result is ResultState.Error)
        val error = result as ResultState.Error<String, String>
        assertEquals("error message", error.error)
    }

    @Test
    fun testSuccessEquality() {
        val s1 = ResultState.Success<String, String>("data")
        val s2 = ResultState.Success<String, String>("data")
        val s3 = ResultState.Success<String, String>("other")

        assertEquals(s1, s2)
        assertNotEquals(s1, s3)
    }

    @Test
    fun testErrorEquality() {
        val e1 = ResultState.Error<String, String>("error")
        val e2 = ResultState.Error<String, String>("error")
        val e3 = ResultState.Error<String, String>("other error")

        assertEquals(e1, e2)
        assertNotEquals(e1, e3)
    }

    @Test
    fun testSuccessNotEqualsError() {
        val success = ResultState.Success<String, String>("data")
        val error = ResultState.Error<String, String>("error")

        assertNotEquals(success, error)
    }
}