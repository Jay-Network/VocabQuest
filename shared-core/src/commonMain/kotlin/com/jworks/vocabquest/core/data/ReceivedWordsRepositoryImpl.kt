package com.jworks.vocabquest.core.data

import com.jworks.vocabquest.core.domain.model.ReceivedWord
import com.jworks.vocabquest.core.domain.model.ReceivedWordState
import com.jworks.vocabquest.core.domain.repository.ReceivedWordsRepository
import com.jworks.vocabquest.db.EigoQuestDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReceivedWordsRepositoryImpl(
    private val database: EigoQuestDatabase
) : ReceivedWordsRepository {

    private val queries get() = database.receivedWordsQueries

    override suspend fun getAll(): List<ReceivedWord> = withContext(Dispatchers.Default) {
        queries.getAll().executeAsList().map { it.toDomain() }
    }

    override suspend fun getPending(): List<ReceivedWord> = withContext(Dispatchers.Default) {
        queries.getPending().executeAsList().map { it.toDomain() }
    }

    override suspend fun getByState(state: ReceivedWordState): List<ReceivedWord> =
        withContext(Dispatchers.Default) {
            queries.getByState(state.value).executeAsList().map { it.toDomain() }
        }

    override suspend fun getById(id: Long): ReceivedWord? = withContext(Dispatchers.Default) {
        queries.getById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getCount(): Int = withContext(Dispatchers.Default) {
        queries.getCount().executeAsOne().toInt()
    }

    override suspend fun getPendingCount(): Int = withContext(Dispatchers.Default) {
        queries.getPendingCount().executeAsOne().toInt()
    }

    override suspend fun getMasteredCount(): Int = withContext(Dispatchers.Default) {
        queries.getMasteredCount().executeAsOne().toInt()
    }

    override suspend fun insert(word: ReceivedWord) = withContext(Dispatchers.Default) {
        queries.insert(
            word = word.word,
            ipa = word.ipa,
            cefr_level = word.cefrLevel,
            source_app = word.sourceApp,
            sender_user_id = word.senderUserId,
            received_at = word.receivedAt
        )
    }

    override suspend fun insertBatch(words: List<ReceivedWord>) = withContext(Dispatchers.Default) {
        queries.transaction {
            for (word in words) {
                queries.insert(
                    word = word.word,
                    ipa = word.ipa,
                    cefr_level = word.cefrLevel,
                    source_app = word.sourceApp,
                    sender_user_id = word.senderUserId,
                    received_at = word.receivedAt
                )
            }
        }
    }

    override suspend fun updateState(id: Long, state: ReceivedWordState) =
        withContext(Dispatchers.Default) {
            queries.updateState(srs_state = state.value, id = id)
        }

    override suspend fun markMastered(id: Long, masteredAt: Long) =
        withContext(Dispatchers.Default) {
            queries.markMastered(mastered_at = masteredAt, id = id)
        }

    override suspend fun linkToWord(id: Long, wordId: Int) = withContext(Dispatchers.Default) {
        queries.linkToWord(linked_word_id = wordId.toLong(), id = id)
    }
}

private fun com.jworks.vocabquest.db.Received_words.toDomain() = ReceivedWord(
    id = id,
    word = word,
    ipa = ipa,
    cefrLevel = cefr_level,
    sourceApp = source_app,
    senderUserId = sender_user_id,
    receivedAt = received_at,
    srsState = ReceivedWordState.fromString(srs_state),
    linkedWordId = linked_word_id?.toInt(),
    masteredAt = mastered_at
)
