package com.jworks.vocabquest.core.data

import com.jworks.vocabquest.core.domain.model.CollectedWord
import com.jworks.vocabquest.core.domain.model.CollectionStats
import com.jworks.vocabquest.core.domain.model.Rarity
import com.jworks.vocabquest.core.domain.repository.CollectionRepository
import com.jworks.vocabquest.db.EigoQuestDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CollectionRepositoryImpl(
    private val database: EigoQuestDatabase
) : CollectionRepository {

    private val queries get() = database.collectionQueries

    override suspend fun getAll(): List<CollectedWord> = withContext(Dispatchers.Default) {
        queries.getAll().executeAsList().map { it.toDomain() }
    }

    override suspend fun getByRarity(rarity: Rarity): List<CollectedWord> =
        withContext(Dispatchers.Default) {
            queries.getByRarity(rarity.name.lowercase()).executeAsList().map { it.toDomain() }
        }

    override suspend fun getItem(wordId: Int): CollectedWord? = withContext(Dispatchers.Default) {
        queries.getItem(wordId.toLong()).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun isCollected(wordId: Int): Boolean = withContext(Dispatchers.Default) {
        queries.isCollected(wordId.toLong()).executeAsOne() > 0
    }

    override suspend fun collect(item: CollectedWord) = withContext(Dispatchers.Default) {
        queries.insert(
            word_id = item.wordId.toLong(),
            rarity = item.rarity.name.lowercase(),
            item_level = item.itemLevel.toLong(),
            item_xp = item.itemXp.toLong(),
            discovered_at = item.discoveredAt,
            source = item.source
        )
    }

    override suspend fun addItemXp(wordId: Int, xp: Int): CollectedWord? =
        withContext(Dispatchers.Default) {
            queries.addXp(xp.toLong(), wordId.toLong())
            queries.getItem(wordId.toLong()).executeAsOneOrNull()?.toDomain()
        }

    override suspend fun updateLevel(wordId: Int, newLevel: Int, overflowXp: Int) =
        withContext(Dispatchers.Default) {
            queries.updateLevel(newLevel.toLong(), overflowXp.toLong(), wordId.toLong())
        }

    override suspend fun getCollectedWordIds(): List<Int> = withContext(Dispatchers.Default) {
        queries.getCollectedWordIds().executeAsList().map { it.toInt() }
    }

    override suspend fun getTotalCount(): Long = withContext(Dispatchers.Default) {
        queries.getTotalCount().executeAsOne()
    }

    override suspend fun getCountByRarity(rarity: Rarity): Long =
        withContext(Dispatchers.Default) {
            queries.getCountByRarity(rarity.name.lowercase()).executeAsOne()
        }

    override suspend fun getStats(): CollectionStats = withContext(Dispatchers.Default) {
        CollectionStats(
            totalCollected = queries.getTotalCount().executeAsOne().toInt(),
            commonCount = queries.getCountByRarity("common").executeAsOne().toInt(),
            uncommonCount = queries.getCountByRarity("uncommon").executeAsOne().toInt(),
            rareCount = queries.getCountByRarity("rare").executeAsOne().toInt(),
            epicCount = queries.getCountByRarity("epic").executeAsOne().toInt(),
            legendaryCount = queries.getCountByRarity("legendary").executeAsOne().toInt()
        )
    }

    override suspend fun getRecent(limit: Int): List<CollectedWord> =
        withContext(Dispatchers.Default) {
            queries.getRecent(limit.toLong()).executeAsList().map { it.toDomain() }
        }
}

private fun com.jworks.vocabquest.db.Word_collection.toDomain() = CollectedWord(
    wordId = word_id.toInt(),
    rarity = Rarity.fromString(rarity),
    itemLevel = item_level.toInt(),
    itemXp = item_xp.toInt(),
    discoveredAt = discovered_at,
    source = source
)
