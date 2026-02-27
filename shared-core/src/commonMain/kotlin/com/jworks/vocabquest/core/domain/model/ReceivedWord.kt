package com.jworks.vocabquest.core.domain.model

/**
 * A word received from a cross-app transfer (e.g. EigoLens → EigoQuest).
 * Feeds into the SRS system and awards J Coins on mastery.
 */
data class ReceivedWord(
    val id: Long = 0,
    val word: String,
    val ipa: String? = null,
    val cefrLevel: String = "B1",
    val sourceApp: String = "eigolens",
    val senderUserId: String,
    val receivedAt: Long,
    val srsState: ReceivedWordState = ReceivedWordState.NEW,
    val linkedWordId: Int? = null,
    val masteredAt: Long? = null
)

enum class ReceivedWordState(val value: String) {
    NEW("new"),
    REVIEWING("reviewing"),
    GRADUATED("graduated");

    companion object {
        fun fromString(value: String): ReceivedWordState =
            entries.find { it.value == value } ?: NEW
    }
}
