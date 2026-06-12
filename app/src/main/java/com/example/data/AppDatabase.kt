package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        MatchEntity::class,
        PredictionEntity::class,
        OfficialSpecialsEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun matchDao(): MatchDao
    abstract fun predictionDao(): PredictionDao
    abstract fun officialSpecialsDao(): OfficialSpecialsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the new isOverrideUnlocked column with standard boolean DEFAULT 0 (false)
                db.execSQL("ALTER TABLE matches ADD COLUMN isOverrideUnlocked INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add predictedJoven to users, and joven to official_specials
                db.execSQL("ALTER TABLE users ADD COLUMN predictedJoven TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE official_specials ADD COLUMN joven TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "polla_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
