package com.zyb.studyflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StudyProgressEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class StudyFlowDatabase : RoomDatabase() {
    abstract fun studyProgressDao(): StudyProgressDao
}
