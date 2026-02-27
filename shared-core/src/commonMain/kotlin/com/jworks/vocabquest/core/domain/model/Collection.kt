package com.jworks.vocabquest.core.domain.model

enum class Rarity(val label: String, val colorValue: Long) {
    COMMON("Common", 0xFF9E9E9E),
    UNCOMMON("Uncommon", 0xFF4CAF50),
    RARE("Rare", 0xFF2196F3),
    EPIC("Epic", 0xFF9C27B0),
    LEGENDARY("Legendary", 0xFFFFD700);

    companion object {
        fun fromString(value: String): Rarity =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: COMMON
    }
}

data class CollectedWord(
    val wordId: Int,
    val rarity: Rarity,
    val itemLevel: Int,
    val itemXp: Int,
    val discoveredAt: Long,
    val source: String
) {
    val xpToNextLevel: Int get() = itemLevel * itemLevel * 25
    val levelProgress: Float get() = if (isMaxLevel) 1f else itemXp.toFloat() / xpToNextLevel
    val isMaxLevel: Boolean get() = itemLevel >= MAX_LEVEL

    companion object {
        const val MAX_LEVEL = 10
    }
}

data class CollectionStats(
    val totalCollected: Int,
    val commonCount: Int,
    val uncommonCount: Int,
    val rareCount: Int,
    val epicCount: Int,
    val legendaryCount: Int,
    val a1Count: Int = 0,
    val a2Count: Int = 0,
    val b1Count: Int = 0,
    val b2Count: Int = 0,
    val c1Count: Int = 0,
    val c2Count: Int = 0
) {
    companion object {
        val EMPTY = CollectionStats(0, 0, 0, 0, 0, 0)
    }
}
