package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val avatar: String = "⚽", // emoji character
    val pin: String = "0000",
    val predictedChampion: String = "",
    val predictedSubchampion: String = "",
    val predictedThirdPlace: String = "",
    val predictedGoleador: String = "",
    val predictedAsistente: String = "",
    val predictedMvp: String = "",
    val predictedPortero: String = "",
    val predictedVallaMenosVencida: String = "",
    val predictedJoven: String = ""
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: Int,
    val team1: String,
    val team2: String,
    val matchTimeMillis: Long, // Epoch millis of kickoff
    val groupCode: String,     // e.g. "Grupo A"
    val stadium: String,
    val actualGoals1: Int? = null, // null means not played yet
    val actualGoals2: Int? = null,
    val isCompleted: Boolean = false,
    val isOverrideUnlocked: Boolean = false
)

@Entity(tableName = "predictions", primaryKeys = ["userId", "matchId"])
data class PredictionEntity(
    val userId: Int,
    val matchId: Int,
    val predictedGoals1: Int = 0,
    val predictedGoals2: Int = 0
)

@Entity(tableName = "official_specials")
data class OfficialSpecialsEntity(
    @PrimaryKey val id: Int = 1,
    val champion: String = "",
    val subchampion: String = "",
    val thirdPlace: String = "",
    val goleador: String = "",
    val asistente: String = "",
    val mvp: String = "",
    val portero: String = "",
    val vallaMenosVencida: String = "",
    val joven: String = ""
)
