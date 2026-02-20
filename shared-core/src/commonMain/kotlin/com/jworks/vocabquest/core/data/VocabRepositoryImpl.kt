package com.jworks.vocabquest.core.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import com.jworks.vocabquest.core.domain.model.Word
import com.jworks.vocabquest.core.domain.model.WordExample
import com.jworks.vocabquest.core.domain.repository.VocabRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VocabRepositoryImpl(
    private val driver: SqlDriver
) : VocabRepository {

    override suspend fun getWordById(id: Int): Word? = withContext(Dispatchers.Default) {
        driver.executeQuery(
            identifier = null,
            sql = "SELECT id, word, definition, pos, cefr_level, frequency_rank, phonetic, audio_url FROM word WHERE id = ?",
            mapper = { cursor ->
                val result = if (cursor.next().value) {
                    cursorToWord(cursor)
                } else null
                QueryResult.Value(result)
            },
            parameters = 1
        ) {
            bindLong(0, id.toLong())
        }.value
    }

    override suspend fun getWordsByLevel(cefrLevel: String, limit: Int): List<Word> = withContext(Dispatchers.Default) {
        driver.executeQuery(
            identifier = null,
            sql = "SELECT id, word, definition, pos, cefr_level, frequency_rank, phonetic, audio_url FROM word WHERE cefr_level = ? ORDER BY frequency_rank LIMIT ?",
            mapper = { cursor ->
                val words = mutableListOf<Word>()
                while (cursor.next().value) {
                    words.add(cursorToWord(cursor))
                }
                QueryResult.Value(words.toList())
            },
            parameters = 2
        ) {
            bindString(0, cefrLevel)
            bindLong(1, limit.toLong())
        }.value
    }

    override suspend fun getWordsByIds(ids: List<Int>): List<Word> = withContext(Dispatchers.Default) {
        if (ids.isEmpty()) return@withContext emptyList()
        val placeholders = ids.joinToString(",") { "?" }
        driver.executeQuery(
            identifier = null,
            sql = "SELECT id, word, definition, pos, cefr_level, frequency_rank, phonetic, audio_url FROM word WHERE id IN ($placeholders)",
            mapper = { cursor ->
                val words = mutableListOf<Word>()
                while (cursor.next().value) {
                    words.add(cursorToWord(cursor))
                }
                QueryResult.Value(words.toList())
            },
            parameters = ids.size
        ) {
            ids.forEachIndexed { index, id -> bindLong(index, id.toLong()) }
        }.value
    }

    override suspend fun searchWords(query: String, limit: Int): List<Word> = withContext(Dispatchers.Default) {
        driver.executeQuery(
            identifier = null,
            sql = "SELECT id, word, definition, pos, cefr_level, frequency_rank, phonetic, audio_url FROM word WHERE word LIKE ? ORDER BY frequency_rank LIMIT ?",
            mapper = { cursor ->
                val words = mutableListOf<Word>()
                while (cursor.next().value) {
                    words.add(cursorToWord(cursor))
                }
                QueryResult.Value(words.toList())
            },
            parameters = 2
        ) {
            bindString(0, "$query%")
            bindLong(1, limit.toLong())
        }.value
    }

    override suspend fun getRandomWords(count: Int, cefrLevel: String?): List<Word> = withContext(Dispatchers.Default) {
        val sql = if (cefrLevel != null) {
            "SELECT id, word, definition, pos, cefr_level, frequency_rank, phonetic, audio_url FROM word WHERE cefr_level = ? ORDER BY RANDOM() LIMIT ?"
        } else {
            "SELECT id, word, definition, pos, cefr_level, frequency_rank, phonetic, audio_url FROM word ORDER BY RANDOM() LIMIT ?"
        }
        val paramCount = if (cefrLevel != null) 2 else 1

        driver.executeQuery(
            identifier = null,
            sql = sql,
            mapper = { cursor ->
                val words = mutableListOf<Word>()
                while (cursor.next().value) {
                    words.add(cursorToWord(cursor))
                }
                QueryResult.Value(words.toList())
            },
            parameters = paramCount
        ) {
            if (cefrLevel != null) {
                bindString(0, cefrLevel)
                bindLong(1, count.toLong())
            } else {
                bindLong(0, count.toLong())
            }
        }.value
    }

    override suspend fun getTotalWordCount(): Int = withContext(Dispatchers.Default) {
        driver.executeQuery(
            identifier = null,
            sql = "SELECT COUNT(*) FROM word",
            mapper = { cursor ->
                if (cursor.next().value) {
                    QueryResult.Value(cursor.getLong(0)?.toInt() ?: 0)
                } else {
                    QueryResult.Value(0)
                }
            },
            parameters = 0
        ).value
    }

    override suspend fun getWordCountByLevel(cefrLevel: String): Int = withContext(Dispatchers.Default) {
        driver.executeQuery(
            identifier = null,
            sql = "SELECT COUNT(*) FROM word WHERE cefr_level = ?",
            mapper = { cursor ->
                if (cursor.next().value) {
                    QueryResult.Value(cursor.getLong(0)?.toInt() ?: 0)
                } else {
                    QueryResult.Value(0)
                }
            },
            parameters = 1
        ) {
            bindString(0, cefrLevel)
        }.value
    }

    private fun cursorToWord(cursor: app.cash.sqldelight.db.SqlCursor) = Word(
        id = (cursor.getLong(0) ?: 0).toInt(),
        word = cursor.getString(1) ?: "",
        definition = cursor.getString(2) ?: "",
        pos = cursor.getString(3) ?: "noun",
        cefrLevel = cursor.getString(4) ?: "B1",
        frequencyRank = (cursor.getLong(5) ?: 0).toInt(),
        phonetic = cursor.getString(6),
        audioUrl = cursor.getString(7)
    )
}
