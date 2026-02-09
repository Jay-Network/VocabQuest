package com.jworks.vocabquest.core.engine

import com.jworks.vocabquest.core.domain.model.GameMode

data class SessionStats(
    val gameMode: GameMode,
    val cardsStudied: Int,
    val correctCount: Int,
    val comboMax: Int,
    val xpEarned: Int,
    val durationSec: Int,
    val touchedKanjiIds: List<Int> = emptyList(),
    val touchedVocabIds: List<Long> = emptyList()
)
