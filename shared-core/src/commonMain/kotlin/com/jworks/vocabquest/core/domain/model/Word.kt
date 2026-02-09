package com.jworks.vocabquest.core.domain.model

data class Word(
    val id: Int,
    val word: String,
    val definition: String,
    val pos: String,
    val cefrLevel: String,
    val frequencyRank: Int,
    val phonetic: String?,
    val audioUrl: String?,
    val examples: List<WordExample> = emptyList()
)

data class WordExample(
    val id: Int,
    val wordId: Int,
    val sentence: String,
    val context: String?,
    val difficulty: String?
)
