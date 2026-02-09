package com.jworks.vocabquest.core.domain.repository

import com.jworks.vocabquest.core.domain.model.Word

interface VocabRepository {
    suspend fun getWordById(id: Int): Word?
    suspend fun getWordsByLevel(cefrLevel: String, limit: Int = 50): List<Word>
    suspend fun getWordsByIds(ids: List<Int>): List<Word>
    suspend fun searchWords(query: String, limit: Int = 20): List<Word>
    suspend fun getRandomWords(count: Int, cefrLevel: String? = null): List<Word>
    suspend fun getTotalWordCount(): Int
    suspend fun getWordCountByLevel(cefrLevel: String): Int
}
