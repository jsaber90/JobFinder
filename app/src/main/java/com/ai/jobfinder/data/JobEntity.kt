package com.ai.jobfinder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val title: String,
    val company: String,
    val location: String,
    val description: String,
    val url: String,
    val source: String,
    val publishedAt: String? = null,
    val notified: Boolean = false
)

