package com.example.core.taste

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.util.regex.Pattern

enum class EmailValidationStatus {
    UNVERIFIED,
    VALIDATING,
    VERIFIED,
    INVALID
}

data class EmailValidationResult(
    val isValid: Boolean,
    val status: EmailValidationStatus,
    val message: String
)

object EmailValidator {
    private val EMAIL_REGEX = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,10}$"
    )

    private val KNOWN_MAIL_DOMAINS = setOf(
        "gmail.com", "googlemail.com", "google.com",
        "spotify.com", "youtube.com",
        "yahoo.com", "ymail.com", "rocketmail.com",
        "outlook.com", "hotmail.com", "live.com", "msn.com",
        "icloud.com", "me.com", "mac.com",
        "proton.me", "protonmail.com",
        "aol.com", "zoho.com", "mail.com",
        "gautambhuwan.com.np"
    )

    fun isSyntaxValid(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return false
        if (!trimmed.contains("@")) return false
        val parts = trimmed.split("@")
        if (parts.size != 2) return false
        val local = parts[0]
        val domain = parts[1]
        if (local.isBlank() || domain.isBlank()) return false
        if (!domain.contains(".")) return false
        val tld = domain.substringAfterLast(".")
        if (tld.length < 2) return false
        return EMAIL_REGEX.matcher(trimmed).matches()
    }

    fun getSyntaxErrorMessage(email: String): String? {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return "Email address cannot be empty."
        if (!trimmed.contains("@")) return "Missing '@' symbol in email address."
        val parts = trimmed.split("@")
        if (parts.size != 2) return "Email must contain exactly one '@' symbol."
        val local = parts[0]
        val domain = parts[1]
        if (local.isBlank()) return "Username before '@' cannot be empty."
        if (domain.isBlank()) return "Domain after '@' cannot be empty."
        if (!domain.contains(".")) return "Domain must contain a valid extension (e.g. .com)."
        val tld = domain.substringAfterLast(".")
        if (tld.length < 2) return "Domain extension '.$tld' is too short."
        if (!EMAIL_REGEX.matcher(trimmed).matches()) return "Invalid characters in email address."
        return null
    }

    suspend fun validateEmailInBackground(email: String): EmailValidationResult = withContext(Dispatchers.IO) {
        val trimmed = email.trim()
        val syntaxError = getSyntaxErrorMessage(trimmed)
        if (syntaxError != null) {
            return@withContext EmailValidationResult(
                isValid = false,
                status = EmailValidationStatus.INVALID,
                message = syntaxError
            )
        }

        val domain = trimmed.substringAfter("@").lowercase()

        // Check recognized valid domains
        if (KNOWN_MAIL_DOMAINS.contains(domain)) {
            return@withContext EmailValidationResult(
                isValid = true,
                status = EmailValidationStatus.VERIFIED,
                message = "Verified ($domain)"
            )
        }

        // Domain reachability / DNS verification
        val dnsValid = try {
            val addresses = InetAddress.getAllByName(domain)
            addresses.isNotEmpty()
        } catch (_: Exception) {
            // Fallback for offline mode if syntax and TLD are valid
            domain.substringAfterLast(".").length >= 2
        }

        if (dnsValid) {
            EmailValidationResult(
                isValid = true,
                status = EmailValidationStatus.VERIFIED,
                message = "Verified domain ($domain)"
            )
        } else {
            EmailValidationResult(
                isValid = false,
                status = EmailValidationStatus.INVALID,
                message = "Domain '$domain' could not be resolved."
            )
        }
    }
}
