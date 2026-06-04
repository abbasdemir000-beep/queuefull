package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database definition. Entity set, version (1), and exportSchema flag are
 * unchanged from the previous definition; only the entity class names carry the
 * `Entity` suffix now. The on-disk schema is unchanged.
 */
@Database(
    entities = [
        CityEntity::class,
        StationEntity::class,
        QueueUpdateEntity::class,
        AdBannerEntity::class,
        AppUserEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QueueFuelDatabase : RoomDatabase() {
    abstract fun dao(): QueueFuelDao
}
