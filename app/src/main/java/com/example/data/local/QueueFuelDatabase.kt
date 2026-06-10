package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CityEntity::class,
        StationEntity::class,
        QueueUpdateEntity::class,
        AdBannerEntity::class,
        AppUserEntity::class,
        AdvertisementEntity::class,
        UserBadgeEntity::class,
        RewardEntryEntity::class,
        ReportConfidenceEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class QueueFuelDatabase : RoomDatabase() {
    abstract fun dao(): QueueFuelDao

    companion object {
        @Volatile private var INSTANCE: QueueFuelDatabase? = null

        fun getInstance(context: Context): QueueFuelDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    QueueFuelDatabase::class.java,
                    "queue_fuel.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Extend app_users with new columns (all have defaults so existing rows are valid)
                db.execSQL("ALTER TABLE app_users ADD COLUMN lifetimePoints INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN monthlyPoints INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE app_users ADD COLUMN accountType TEXT NOT NULL DEFAULT 'FREE'")
                db.execSQL("ALTER TABLE app_users ADD COLUMN premiumUntil INTEGER")

                // New tables
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS advertisements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        imageUrl TEXT,
                        businessName TEXT NOT NULL,
                        category TEXT NOT NULL,
                        targetCity TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER NOT NULL,
                        clickCount INTEGER NOT NULL DEFAULT 0,
                        viewCount INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_badges (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userPhone TEXT NOT NULL,
                        badge TEXT NOT NULL,
                        awardedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reward_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userPhone TEXT NOT NULL,
                        stationId INTEGER NOT NULL,
                        reportId INTEGER NOT NULL,
                        monthYear TEXT NOT NULL,
                        isVerified INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS report_confidence (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        stationId INTEGER NOT NULL,
                        originalReporterId TEXT NOT NULL,
                        confirmationCount INTEGER NOT NULL DEFAULT 0,
                        confidenceScore REAL NOT NULL DEFAULT 1.0,
                        timestamp INTEGER NOT NULL,
                        expiresAt INTEGER NOT NULL,
                        isExpired INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Simple profile registration (no OTP): city + phoneVerified flag
                db.execSQL("ALTER TABLE app_users ADD COLUMN city TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE app_users ADD COLUMN phoneVerified INTEGER NOT NULL DEFAULT 0")

                // Photo + AI verification fields on reports. Legacy reports already
                // affected stations/points, so they are grandfathered as VERIFIED.
                db.execSQL("ALTER TABLE queue_updates ADD COLUMN photoPath TEXT")
                db.execSQL("ALTER TABLE queue_updates ADD COLUMN verification TEXT NOT NULL DEFAULT 'VERIFIED'")
                db.execSQL("ALTER TABLE queue_updates ADD COLUMN verificationNote TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
