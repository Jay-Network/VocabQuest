package com.jworks.vocabquest.core.domain.usecase

import com.jworks.vocabquest.core.domain.model.EarnTriggers
import com.jworks.vocabquest.core.domain.model.LOCAL_USER_ID
import com.jworks.vocabquest.core.domain.model.ReceivedWordState
import com.jworks.vocabquest.core.domain.model.SrsState
import com.jworks.vocabquest.core.domain.repository.JCoinRepository
import com.jworks.vocabquest.core.domain.repository.ReceivedWordsRepository
import kotlinx.datetime.Clock

/**
 * Checks if an SRS card graduation means a received word was mastered.
 * If so, fires the eigoquest_word_mastered J Coin earn trigger (+5 coins).
 *
 * Called after every SRS upsert where the card reaches GRADUATED state.
 */
class CheckWordMasteryUseCase(
    private val receivedWordsRepository: ReceivedWordsRepository,
    private val jCoinRepository: JCoinRepository
) {
    /**
     * @param wordId The word ID that was just reviewed
     * @param previousState The SRS state before this review
     * @param newState The SRS state after this review
     */
    suspend fun execute(wordId: Int, previousState: SrsState, newState: SrsState) {
        // Only trigger on transition TO graduated
        if (newState != SrsState.GRADUATED || previousState == SrsState.GRADUATED) return

        // Check if this word is linked to a received word
        val allReceived = receivedWordsRepository.getAll()
        val receivedWord = allReceived.find {
            it.linkedWordId == wordId && it.srsState != ReceivedWordState.GRADUATED
        } ?: return

        // Mark as mastered
        val now = Clock.System.now().epochSeconds
        receivedWordsRepository.markMastered(receivedWord.id, now)

        // Fire J Coin earn trigger
        jCoinRepository.earnCoins(
            userId = LOCAL_USER_ID,
            sourceType = EarnTriggers.WORD_MASTERED,
            baseAmount = 5,
            description = "Mastered received word: ${receivedWord.word}",
            metadata = """{"word":"${receivedWord.word}","source_app":"${receivedWord.sourceApp}"}"""
        )
    }
}
