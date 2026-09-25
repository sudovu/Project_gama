package com.example

import com.example.core.taste.EmailValidationStatus
import com.example.core.taste.EmailValidator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailValidatorTest {

    @Test
    fun testValidEmailSyntax() {
        val validEmails = listOf(
            "vhuwonmathers@gmail.com",
            "info@gautambhuwan.com.np",
            "user@spotify.com",
            "support@youtube.com",
            "john.doe@outlook.com",
            "music_fan+rock@proton.me",
            "dev@sub.domain.org"
        )

        for (email in validEmails) {
            assertTrue("Expected '$email' to be valid", EmailValidator.isSyntaxValid(email))
            assertNull("Expected no error message for '$email'", EmailValidator.getSyntaxErrorMessage(email))
        }
    }

    @Test
    fun testInvalidEmailSyntax() {
        val invalidEmails = listOf(
            "",
            "   ",
            "@gmail.com",
            "user@",
            "user@domain",
            "user@domain.c",
            "user@@domain.com",
            "user with spaces@gmail.com",
            "plainaddress"
        )

        for (email in invalidEmails) {
            assertFalse("Expected '$email' to be invalid", EmailValidator.isSyntaxValid(email))
            assertNotNull("Expected error message for '$email'", EmailValidator.getSyntaxErrorMessage(email))
        }
    }

    @Test
    fun testBackgroundValidationForKnownDomains() = runBlocking {
        val result = EmailValidator.validateEmailInBackground("vhuwonmathers@gmail.com")
        assertTrue(result.isValid)
        assertEquals(EmailValidationStatus.VERIFIED, result.status)
        assertTrue(result.message.contains("gmail.com"))
    }

    @Test
    fun testBackgroundValidationForInvalidEmail() = runBlocking {
        val result = EmailValidator.validateEmailInBackground("@gmail.com")
        assertFalse(result.isValid)
        assertEquals(EmailValidationStatus.INVALID, result.status)
        assertEquals("Username before '@' cannot be empty.", result.message)
    }

    @Test
    fun testBackgroundValidationForDeveloperEmail() = runBlocking {
        val result = EmailValidator.validateEmailInBackground("info@gautambhuwan.com.np")
        assertTrue(result.isValid)
        assertEquals(EmailValidationStatus.VERIFIED, result.status)
    }
}
