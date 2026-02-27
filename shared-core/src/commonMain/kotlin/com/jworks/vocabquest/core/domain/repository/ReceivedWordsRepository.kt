package com.jworks.vocabquest.core.domain.repository

import com.jworks.vocabquest.core.domain.model.ReceivedWord
import com.jworks.vocabquest.core.domain.model.ReceivedWordState

interface ReceivedWordsRepository {
    suspend fun getAll(): List<ReceivedWord>
    suspend fun getPending(): List<ReceivedWord>
    suspend fun getByState(state: ReceivedWordState): List<ReceivedWord>
    suspend fun getById(id: Long): ReceivedWord?
    suspend fun getCount(): Int
    suspend fun getPendingCount(): Int
    suspend fun getMasteredCount(): Int

    suspend fun insert(word: ReceivedWord)
    suspend fun insertBatch(words: List<ReceivedWord>)
    suspend fun updateState(id: Long, state: ReceivedWordState)
    suspend fun markMastered(id: Long, masteredAt: Long)
    suspend fun linkToWord(id: Long, wordId: Int)
}
