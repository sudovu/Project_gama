package com.example

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.CuePointEntity
import com.example.data.local.GammaDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class GammaDatabaseMigrationTest {

    @Test
    fun migration5To6CreatesCuePointsTableAndIndicesMatchingEntity() {
        val entityColumns = CuePointEntity::class.java.declaredFields
            .filterNot { it.isSynthetic || it.name.startsWith("$") || it.name == "Companion" }
            .associate { it.name to it.type }
        assertEquals(4, entityColumns.size)

        val executedSql = mutableListOf<String>()
        val dbProxy = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            when (method.name) {
                "execSQL" -> {
                    val sql = args?.firstOrNull() as? String
                    if (sql != null) executedSql.add(sql)
                    null
                }
                "isOpen" -> true
                "getVersion" -> 5
                else -> null
            }
        } as SupportSQLiteDatabase

        GammaDatabase.MIGRATION_5_6.migrate(dbProxy)

        val createTableSql = executedSql.firstOrNull { it.contains("CREATE TABLE", ignoreCase = true) } ?: ""
        assertTrue("Expected cue_points table creation", createTableSql.contains("`cue_points`"))

        for ((colName, colType) in entityColumns) {
            val sqlType = when (colType) {
                Long::class.java, java.lang.Long::class.java -> "INTEGER"
                else -> "TEXT"
            }
            assertTrue(
                "Expected column `$colName` $sqlType NOT NULL in migration SQL",
                createTableSql.contains("`$colName` $sqlType NOT NULL")
            )
        }
        assertTrue(createTableSql.contains("PRIMARY KEY(`id`)"))
        assertTrue(executedSql.any { it.contains("`index_cue_points_trackId` ON `cue_points` (`trackId`)") })
        assertTrue(executedSql.any { it.contains("`index_cue_points_positionMs` ON `cue_points` (`positionMs`)") })
    }
}
