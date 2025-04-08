package models

import kotlinx.serialization.Serializable


@Serializable
data class TestResponse(
    val word: String,
    val options: List<String>,
    val correctAnswer: String
)
