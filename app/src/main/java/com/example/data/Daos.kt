package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY matchTimeMillis ASC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Update
    suspend fun updateMatch(match: MatchEntity)
}

@Dao
interface PredictionDao {
    @Query("SELECT * FROM predictions")
    fun getAllPredictions(): Flow<List<PredictionEntity>>

    @Query("SELECT * FROM predictions WHERE userId = :userId")
    fun getPredictionsForUser(userId: Int): Flow<List<PredictionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPredictions(predictions: List<PredictionEntity>)
}

@Dao
interface OfficialSpecialsDao {
    @Query("SELECT * FROM official_specials WHERE id = 1 LIMIT 1")
    fun getOfficialSpecials(): Flow<OfficialSpecialsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfficialSpecials(specials: OfficialSpecialsEntity)

    @Update
    suspend fun updateOfficialSpecials(specials: OfficialSpecialsEntity)
}
