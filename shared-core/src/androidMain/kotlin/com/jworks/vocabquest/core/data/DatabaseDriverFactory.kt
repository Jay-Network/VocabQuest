package com.jworks.vocabquest.core.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jworks.vocabquest.db.EigoQuestDatabase
import java.io.FileOutputStream

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        val isFreshCopy = copyDatabaseFromAssets()
        if (isFreshCopy) {
            migratePreBuiltDatabase()
        }
        ensureNewTables()
        return AndroidSqliteDriver(
            schema = EigoQuestDatabase.Schema,
            context = context,
            name = DB_NAME
        )
    }

    /** Returns true if a fresh copy was made (first install). */
    private fun copyDatabaseFromAssets(): Boolean {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (dbFile.exists()) return false

        dbFile.parentFile?.mkdirs()
        context.assets.open(DB_NAME).use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }
        return true
    }

    /**
     * The pre-built DB from the data pipeline has word/example data but:
     * 1. No schema version set (PRAGMA user_version = 0)
     * 2. No JCoin tables, SRS tables, user profile, etc.
     *
     * We set user_version = 1 so AndroidSqliteDriver won't try to re-create
     * all tables, and add runtime tables via CREATE TABLE IF NOT EXISTS.
     */
    private fun migratePreBuiltDatabase() {
        val dbFile = context.getDatabasePath(DB_NAME)
        val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
        db.use {
            it.execSQL("PRAGMA user_version = 1")

            // J Coin tables
            it.execSQL("""
                CREATE TABLE IF NOT EXISTS coin_balance (
                    user_id TEXT PRIMARY KEY NOT NULL,
                    local_balance INTEGER NOT NULL DEFAULT 0,
                    synced_balance INTEGER NOT NULL DEFAULT 0,
                    lifetime_earned INTEGER NOT NULL DEFAULT 0,
                    lifetime_spent INTEGER NOT NULL DEFAULT 0,
                    tier TEXT NOT NULL DEFAULT 'bronze',
                    last_synced_at INTEGER NOT NULL DEFAULT 0,
                    needs_sync INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())

            it.execSQL("""
                CREATE TABLE IF NOT EXISTS coin_sync_queue (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    user_id TEXT NOT NULL,
                    event_type TEXT NOT NULL,
                    source_business TEXT NOT NULL DEFAULT 'eigoquests',
                    source_type TEXT NOT NULL,
                    base_amount INTEGER NOT NULL,
                    description TEXT NOT NULL,
                    metadata TEXT NOT NULL DEFAULT '{}',
                    created_at INTEGER NOT NULL,
                    sync_status TEXT NOT NULL DEFAULT 'pending',
                    retry_count INTEGER NOT NULL DEFAULT 0,
                    last_attempt_at INTEGER,
                    error_message TEXT
                )
            """.trimIndent())

            it.execSQL("""
                CREATE TABLE IF NOT EXISTS premium_content_unlocks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    user_id TEXT NOT NULL,
                    content_type TEXT NOT NULL,
                    content_id TEXT NOT NULL,
                    unlocked_at INTEGER NOT NULL,
                    cost_coins INTEGER NOT NULL,
                    UNIQUE(user_id, content_type, content_id)
                )
            """.trimIndent())

            it.execSQL("""
                CREATE TABLE IF NOT EXISTS active_boosters (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    user_id TEXT NOT NULL,
                    booster_type TEXT NOT NULL,
                    multiplier REAL NOT NULL DEFAULT 1.0,
                    activated_at INTEGER NOT NULL,
                    expires_at INTEGER NOT NULL
                )
            """.trimIndent())

            it.execSQL("CREATE INDEX IF NOT EXISTS idx_sync_queue_status ON coin_sync_queue(sync_status, created_at)")
            it.execSQL("CREATE INDEX IF NOT EXISTS idx_unlocks_user ON premium_content_unlocks(user_id, content_type)")
            it.execSQL("CREATE INDEX IF NOT EXISTS idx_boosters_user ON active_boosters(user_id, expires_at)")
        }
    }

    /** Creates tables added after the initial schema - runs on every app start. */
    private fun ensureNewTables() {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) return
        val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
        db.use {
            // SRS card tracking
            it.execSQL("""
                CREATE TABLE IF NOT EXISTS srs_card (
                    word_id INTEGER PRIMARY KEY NOT NULL,
                    ease_factor REAL NOT NULL DEFAULT 2.5,
                    interval INTEGER NOT NULL DEFAULT 0,
                    repetitions INTEGER NOT NULL DEFAULT 0,
                    next_review INTEGER NOT NULL DEFAULT 0,
                    state TEXT NOT NULL DEFAULT 'new',
                    total_reviews INTEGER NOT NULL DEFAULT 0,
                    correct_count INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())

            // User profile
            it.execSQL("""
                CREATE TABLE IF NOT EXISTS user_profile (
                    id INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
                    total_xp INTEGER NOT NULL DEFAULT 0,
                    level INTEGER NOT NULL DEFAULT 1,
                    current_streak INTEGER NOT NULL DEFAULT 0,
                    longest_streak INTEGER NOT NULL DEFAULT 0,
                    last_study_date TEXT,
                    daily_goal INTEGER NOT NULL DEFAULT 20,
                    words_mastered INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())

            // Study sessions
            it.execSQL("""
                CREATE TABLE IF NOT EXISTS study_session (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    game_mode TEXT NOT NULL,
                    started_at INTEGER NOT NULL,
                    cards_studied INTEGER NOT NULL DEFAULT 0,
                    correct_count INTEGER NOT NULL DEFAULT 0,
                    xp_earned INTEGER NOT NULL DEFAULT 0,
                    duration_sec INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())

            // Daily stats
            it.execSQL("""
                CREATE TABLE IF NOT EXISTS daily_stats (
                    date TEXT PRIMARY KEY NOT NULL,
                    cards_reviewed INTEGER NOT NULL DEFAULT 0,
                    xp_earned INTEGER NOT NULL DEFAULT 0,
                    study_time_sec INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())

            // Word collection (gacha)
            it.execSQL("""
                CREATE TABLE IF NOT EXISTS word_collection (
                    word_id INTEGER PRIMARY KEY NOT NULL,
                    rarity TEXT NOT NULL DEFAULT 'COMMON',
                    item_level INTEGER NOT NULL DEFAULT 1,
                    item_xp INTEGER NOT NULL DEFAULT 0,
                    discovered_at INTEGER NOT NULL DEFAULT 0,
                    source TEXT NOT NULL DEFAULT 'gameplay'
                )
            """.trimIndent())

            // Received words (cross-app: EigoLens → EigoQuest)
            it.execSQL("""
                CREATE TABLE IF NOT EXISTS received_words (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    word TEXT NOT NULL,
                    ipa TEXT,
                    cefr_level TEXT NOT NULL DEFAULT 'B1',
                    source_app TEXT NOT NULL DEFAULT 'eigolens',
                    sender_user_id TEXT NOT NULL,
                    received_at INTEGER NOT NULL,
                    srs_state TEXT NOT NULL DEFAULT 'new',
                    linked_word_id INTEGER,
                    mastered_at INTEGER
                )
            """.trimIndent())
            it.execSQL("CREATE INDEX IF NOT EXISTS idx_received_srs_state ON received_words(srs_state)")
            it.execSQL("CREATE INDEX IF NOT EXISTS idx_received_source ON received_words(source_app, sender_user_id)")
            it.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_received_unique_word ON received_words(word, sender_user_id)")
        }
    }

    companion object {
        private const val DB_NAME = "vocabquest.db"
    }
}
