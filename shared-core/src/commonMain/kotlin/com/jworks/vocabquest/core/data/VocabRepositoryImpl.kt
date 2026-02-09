package com.jworks.vocabquest.core.data

import com.jworks.vocabquest.core.domain.model.Word
import com.jworks.vocabquest.core.domain.model.WordExample
import com.jworks.vocabquest.core.domain.repository.VocabRepository
import com.jworks.vocabquest.db.VocabQuestDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VocabRepositoryImpl(
    private val database: VocabQuestDatabase
) : VocabRepository {

    // The vocabulary data is in the pre-built SQLite DB.
    // We access it through raw SQL since the word/word_example tables
    // aren't in the SQLDelight schema (they're pre-populated, not generated).
    // For now, use the database driver directly.

    override suspend fun getWordById(id: Int): Word? = withContext(Dispatchers.Default) {
        val driver = database.driver
        val cursor = driver.executeQuery(
            identifier = null,
            sql = "SELECT id, word, definition, pos, cefr_level, frequency_rank, phonetic, audio_url FROM word WHERE id = ?",
            mapper = { cursor ->
                if (cursor.next().value) {
                    Word(
                        id = cursor.getLong(0)!!.toInt(),
                        word = cursor.getString(1)!!,
                        definition = cursor.getString(2)!!,
                        pos = cursor.getString(3) ?: "noun",
                        cefrLevel = cursor.getString(4) ?: "B1",
                        frequencyRank = cursor.getLong(5)!!.toInt(),
                        phonetic = cursor.getString(6),
                        audioUrl = cursor.getString(7)
                    )
                } else null
            },
            parameters = 1
        ) {
            bindLong(0, id.toLong())
        }
        cursor.value
    }

    override suspend fun getWordsByLevel(cefrLevel: String, limit: Int): List<Word> = withContext(Dispatchers.Default) {
        val driver = database.driver
        val cursor = driver.executeQuery(
            identifier = null,
            sql = "SELECT id, word, definition, pos, cefr_level, frequency_rank, phonetic, audio_url FROM word WHERE cefr_level = ? ORDER BY frequency_rank LIMIT ?",
            mapper = { cursor ->
                val words = mutableListOf<Word>()
                while (cursor.next().value) {
                    words.add(
                        Word(
                            id = cursor.getLong(0)!!.toInt(),
                            word = cursor.getString(1)!!,
                            definition = cursor.getString(2)!!,
                            pos = cursor.getString(3) ?: "noun",
                            cefrLevel = cursor.getString(4) ?: "B1",
                            frequencyRank = cursor.getLong(5)!!.toInt(),
                            phonetic = cursor.getString(6),
                            audioUrl = cursor.getString(7)
                        )
                    )
                }
                words
            },
            parameters = 2
        ) {
            bindString(0, cefrLevel)
            bindLong(1, limit.toLong())
        }
        cursor.value
    }

    override suspend fun getWordsByIds(ids: List<Int>): List<Word> = withContext(Dispatchers.Default) {
        if (ids.isEmpty()) return@withContext emptyList()
        val placeholders = ids.joinToString(",") { "?" }
        val driver = database.driver
        val cursor = driver.executeQuery(
            identifier = null,
            sql = "SELECT id, word, definition, pos, cefr_level, frequency_rank, phonetic, audio_url FROM word WHERE id IN ($placeholders)",
            mapper = { cursor ->
                val words = mutableListOf<Word>()
                while (cursor.next().value) {
                    words.add(
                        Word(
                            id = cursor.getLong(0)!!.toInt(),
                            word = cursor.getString(1)!!,
                            definition = cursor.getString(2)!!,
                            pos = cursor.getString(3) ?: "noun",
                            cefrLevel = cursor.getString(4) ?: "B1",
                            frequencyRank = cursor.getLong(5)!!.toInt(),
                            phonetic = cursor.getString(6),
                            audioUrl = cursor.getString(7)
                        )
                    )
                }
                words
            },
            parameters = ids.size
        ) {
            ids.forEachIndexed { index, id -> bindLong(index, id.toLong()) }
        }
        cursor.value
    }

    override suspend fun searchWords(query: String, limit: Int): List<Word> = withContext(Dispatchers.Default) {
        val driver = database.driver
        val cursor = driver.executeQuery(
            identifier = null,
            sql = "SELECT id, word, definition, pos, cefr_level, frequency_rank, phonetic, audio_url FROM word WHERE word LIKE ? ORDER BY frequency_rank LIMIT ?",
            mapper = { cursor ->
                val words = mutableListOf<Word>()
                while (cursor.next().value) {
                    words.add(
                        Word(
                            id = cursor.getLong(0)!!.toInt(),
                            word = cursor.getString(1)!!,
                            definition = cursor.getString(2)!!,
                            pos = cursor.getString(3) ?: "noun",
                            cefrLevel = cursor.getString(4) ?: "B1",
                            frequencyRank = cursor.getLong(5)!!.toInt(),
                            phonetic = cursor.getString(6),
                            audioUrl = cursor.getString(7)
                        )
                    )
                }
                words
            },
            parameters = 2
        ) {
            bindString(0, "$query%")
            bindLong(1, limit.toLong())
        }
        cursor.value
    }

    override suspend fun getRandomWords(count: Int, cefrLevel: String?): List<Word> = withContext(Dispatchers.Default) {
        val driver = database.driver
        val sql = if (cefrLevel != null) {
            "SELECT id, word, definition, pos, cefr_level, frequency_rank, phonetic, audio_url FROM word WHERE cefr_level = ? ORDER BY RANDOM() LIMIT ?"
        } else {
            "SELECT id, word, definition, pos, cefr_level, frequency_rank, phonetic, audio_url FROM word ORDER BY RANDOM() LIMIT ?"
        }
        val paramCount = if (cefrLevel != null) 2 else 1

        val cursor = driver.executeQuery(
            identifier = null,
            sql = sql,
            mapper = { cursor ->
                val words = mutableListOf<Word>()
                while (cursor.next().value) {
                    words.add(
                        Word(
                            id = cursor.getLong(0)!!.toInt(),
                            word = cursor.getString(1)!!,
                            definition = cursor.getString(2)!!,
                            pos = cursor.getString(3) ?: "noun",
                            cefrLevel = cursor.getString(4) ?: "B1",
                            frequencyRank = cursor.getLong(5)!!.toInt(),
                            phonetic = cursor.getString(6),
                            audioUrl = cursor.getString(7)
                        )
                    )
                }
                words
            },
            parameters = paramCount
        ) {
            if (cefrLevel != null) {
                bindString(0, cefrLevel)
                bindLong(1, count.toLong())
            } else {
                bindLong(0, count.toLong())
            }
        }
        cursor.value
    }

    override suspend fun getTotalWordCount(): Int = withContext(Dispatchers.Default) {
        val driver = database.driver
        val cursor = driver.executeQuery(
            identifier = null,
            sql = "SELECT COUNT(*) FROM word",
            mapper = { cursor ->
                cursor.next()
                cursor.getLong(0)!!.toInt()
            },
            parameters = 0
        )
        cursor.value
    }

    override suspend fun getWordCountByLevel(cefrLevel: String): Int = withContext(Dispatchers.Default) {
        val driver = database.driver
        val cursor = driver.executeQuery(
            identifier = null,
            sql = "SELECT COUNT(*) FROM word WHERE cefr_level = ?",
            mapper = { cursor ->
                cursor.next()
                cursor.getLong(0)!!.toInt()
            },
            parameters = 1
        ) {
            bindString(0, cefrLevel)
        }
        cursor.value
    }
}
