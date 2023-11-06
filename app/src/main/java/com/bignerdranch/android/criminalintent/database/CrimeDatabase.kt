package com.bignerdranch.android.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.criminalintent.Crime

@Database(entities = [Crime::class], version = 6)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase : RoomDatabase() {
    abstract fun crimeDao(): CrimeDao
}

val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''"
        )
    }
}

val migration_2_3 = object : Migration(2,3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN photoFileName TEXT"
        )
    }
}

val migration_3_4 = object : Migration(3,4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN photoFileName2 TEXT"
        )
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN photoFileName3 TEXT"
        )
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN photoFileName4 TEXT"
        )
    }
}

val migration_4_5 = object : Migration(4,5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN isFace INTEGER DEFAULT 0 NOT NULL"
        )
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN isMesh INTEGER DEFAULT 0 NOT NULL"
        )
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN isContour INTEGER DEFAULT 0 NOT NULL"
        )
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN isSelfie INTEGER DEFAULT 0 NOT NULL"
        )
    }
}

val migration_5_6 = object : Migration(5,6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Crime ADD COLUMN numFacesDetected TEXT NOT NULL DEFAULT ''"
        )
    }
}