package models

import kotlinx.serialization.Serializable

@Serializable
data class PasswordUpdateRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class UsernameUpdateRequest(
    val newUsername: String,
    val password: String
)

@Serializable
data class UpdateResponse(
    val success: Boolean,
    val message: String
)