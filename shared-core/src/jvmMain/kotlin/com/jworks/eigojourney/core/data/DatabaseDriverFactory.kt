package com.jworks.eigojourney.core.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jworks.eigojourney.db.EigoJourneyDatabase

actual class DatabaseDriverFactory(private val dbPath: String = "eigojourney.db") {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")
        EigoJourneyDatabase.Schema.create(driver)
        return driver
    }
}
