package models

import kotlinx.serialization.Serializable

@Serializable
data class UserStatisticsData(
    val userId: String,
    val learnedWords: Int,
    val solvedTests: Int,
    val mistakes: Int
)