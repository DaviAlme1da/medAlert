package com.davialmeida.medalert.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE medications
            ADD COLUMN seriesId INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        db.execSQL(
            """
            UPDATE medications
            SET seriesId = id
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX index_medications_seriesId
            ON medications(seriesId)
            """.trimIndent()
        )
    }
}