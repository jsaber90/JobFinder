package com.ai.jobfinder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_searches")
data class SavedSearchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    val email: String = "",
    val location: String = "Egypt",
    val enabled: Boolean = true
)

