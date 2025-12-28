package ch.hslu.newcmpproject.data.local.database

import app.cash.sqldelight.db.SqlDriver
import ch.hslu.cmpproject.cache.AppDatabase

class DatabaseProvider(driver: SqlDriver) {
    val database = AppDatabase.Companion(driver)
    val queries get() = database.appDatabaseQueries
}