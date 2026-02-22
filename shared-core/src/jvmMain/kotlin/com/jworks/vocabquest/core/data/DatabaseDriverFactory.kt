package com.jworks.vocabquest.core.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jworks.vocabquest.db.EigoQuestDatabase

actual class DatabaseDriverFactory(private val dbPath: String = "vocabquest.db") {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")
        EigoQuestDatabase.Schema.create(driver)
        return driver
    }
}
