package com.jworks.vocabquest.core.domain.model

const val LOCAL_USER_ID = "local_user"

/**
 * J Coin earn trigger source_types for EigoQuest (source_business = "eigoquests").
 * 16 triggers registered in the J Coin DB.
 */
object EarnTriggers {
    // Session-based (awarded at session end)
    const val STUDY_SESSION = "study_session"             // 5 coins: completed a study session (10+ cards)
    const val LONG_SESSION = "long_study_session"         // 10 coins: completed 20+ cards in one session
    const val PERFECT_QUIZ = "perfect_quiz"               // 25 coins: 100% accuracy with 10+ cards
    const val FIRST_SESSION = "first_session_of_day"      // 3 coins: first session each day

    // Streak milestones (awarded when streak increases to threshold)
    const val STREAK_3 = "streak_3_days"                  // 15 coins
    const val STREAK_7 = "streak_7_days"                  // 50 coins
    const val STREAK_14 = "streak_14_days"                // 100 coins
    const val STREAK_30 = "streak_30_days"                // 300 coins

    // Word milestones (awarded when total reviewed/mastered hits threshold)
    const val WORDS_100 = "milestone_100_words"           // 50 coins: reviewed 100 unique words
    const val WORDS_500 = "milestone_500_words"           // 150 coins: reviewed 500 unique words
    const val WORDS_1000 = "milestone_1000_words"         // 500 coins: reviewed 1000 unique words

    // Collection triggers
    const val WORD_COLLECTED = "word_collected"           // 2 coins: discovered a new word in collection
    const val RARE_COLLECTED = "rare_word_collected"      // 10 coins: discovered Rare+ word
    const val COLLECTION_50 = "collection_milestone_50"   // 75 coins: collected 50 words

    // Cross-app integration (EigoLens → EigoQuest)
    const val WORD_MASTERED = "eigoquest_word_mastered"   // 5 coins: mastered a received word (srs graduated)

    // Level-up
    const val LEVEL_UP = "level_up"                       // 20 coins: user leveled up
}
