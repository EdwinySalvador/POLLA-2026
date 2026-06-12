package com.example.data

import kotlinx.coroutines.flow.Flow

class PollaRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val matchDao = db.matchDao()
    private val predictionDao = db.predictionDao()
    private val officialSpecialsDao = db.officialSpecialsDao()

    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allMatches: Flow<List<MatchEntity>> = matchDao.getAllMatches()
    val allPredictions: Flow<List<PredictionEntity>> = predictionDao.getAllPredictions()
    val officialSpecials: Flow<OfficialSpecialsEntity?> = officialSpecialsDao.getOfficialSpecials()

    suspend fun insertUser(user: UserEntity): Long {
        return userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }

    suspend fun insertMatches(matches: List<MatchEntity>) {
        matchDao.insertMatches(matches)
    }

    suspend fun updateMatch(match: MatchEntity) {
        matchDao.updateMatch(match)
    }

    suspend fun insertPrediction(prediction: PredictionEntity) {
        predictionDao.insertPrediction(prediction)
    }

    suspend fun insertPredictions(predictions: List<PredictionEntity>) {
        predictionDao.insertPredictions(predictions)
    }

    suspend fun insertOfficialSpecials(specials: OfficialSpecialsEntity) {
        officialSpecialsDao.insertOfficialSpecials(specials)
    }

    suspend fun updateOfficialSpecials(specials: OfficialSpecialsEntity) {
        officialSpecialsDao.updateOfficialSpecials(specials)
    }
}
