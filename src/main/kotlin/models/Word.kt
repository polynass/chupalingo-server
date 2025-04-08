package models

import kotlinx.serialization.Serializable


@Serializable
data class Word(
    val id: Int? = null,
    val word: String,
    val translation: String
)
