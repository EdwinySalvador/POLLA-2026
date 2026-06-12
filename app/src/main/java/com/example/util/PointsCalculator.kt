package com.example.util

import com.example.data.MatchEntity
import com.example.data.OfficialSpecialsEntity
import com.example.data.PredictionEntity
import com.example.data.UserEntity

object PointsCalculator {

    data class PointBreakdown(
        val matchPoints: Int,
        val specialPoints: Int,
        val exactScoresCount: Int,
        val correctOutcomesCount: Int,
        val championPoints: Int,
        val subchampionPoints: Int,
        val thirdPlacePoints: Int,
        val goleadorPoints: Int,
        val asistentePoints: Int,
        val mvpPoints: Int,
        val porteroPoints: Int,
        val vallaMenosVencidaPoints: Int,
        val jovenPoints: Int
    ) {
        val totalPoints: Int get() = matchPoints + specialPoints
    }

    fun calculatePointsForUser(
        user: UserEntity,
        matches: List<MatchEntity>,
        predictions: List<PredictionEntity>,
        specials: OfficialSpecialsEntity?
    ): PointBreakdown {
        var matchPoints = 0
        var exactScoresCount = 0
        var correctOutcomesCount = 0

        // Filter predictions corresponding to this user
        val userPredictionsMap = predictions
            .filter { it.userId == user.id }
            .associateBy { it.matchId }

        for (match in matches) {
            val goals1 = match.actualGoals1
            val goals2 = match.actualGoals2

            // If match has no official result, skip
            if (goals1 == null || goals2 == null) continue

            val prediction = userPredictionsMap[match.id] ?: continue

            val predGoals1 = prediction.predictedGoals1
            val predGoals2 = prediction.predictedGoals2

            val actualOutcome = getOutcome(goals1, goals2)
            val predictedOutcome = getOutcome(predGoals1, predGoals2)

            if (predGoals1 == goals1 && predGoals2 == goals2) {
                // Exact score
                matchPoints += 6
                exactScoresCount++
                correctOutcomesCount++
            } else {
                // Correct winner or draw, but incorrect score
                if (actualOutcome == predictedOutcome) {
                    matchPoints += 3
                    correctOutcomesCount++
                    // Goal difference
                    if ((goals1 - goals2) == (predGoals1 - predGoals2)) {
                        matchPoints += 2
                    }
                }
                // Guess goals of individual team correctly
                if (predGoals1 == goals1) {
                    matchPoints += 2
                }
                if (predGoals2 == goals2) {
                    matchPoints += 2
                }
            }
        }

        var championPoints = 0
        var subchampionPoints = 0
        var thirdPlacePoints = 0
        var goleadorPoints = 0
        var asistentePoints = 0
        var mvpPoints = 0
        var porteroPoints = 0
        var vallaMenosVencidaPoints = 0
        var jovenPoints = 0

        if (specials != null) {
            val offChamp = specials.champion.trim()
            val userChamp = user.predictedChampion.trim()
            if (offChamp.isNotEmpty() && userChamp.isNotEmpty() && offChamp.equals(userChamp, ignoreCase = true)) {
                championPoints = 15
            }

            val offSub = specials.subchampion.trim()
            val userSub = user.predictedSubchampion.trim()
            if (offSub.isNotEmpty() && userSub.isNotEmpty() && offSub.equals(userSub, ignoreCase = true)) {
                subchampionPoints = 10
            }

            val offThird = specials.thirdPlace.trim()
            val userThird = user.predictedThirdPlace.trim()
            if (offThird.isNotEmpty() && userThird.isNotEmpty() && offThird.equals(userThird, ignoreCase = true)) {
                thirdPlacePoints = 8
            }

            val offGol = specials.goleador.trim()
            val userGol = user.predictedGoleador.trim()
            if (offGol.isNotEmpty() && userGol.isNotEmpty() && offGol.equals(userGol, ignoreCase = true)) {
                goleadorPoints = 10
            }

            val offAsis = specials.asistente.trim()
            val userAsis = user.predictedAsistente.trim()
            if (offAsis.isNotEmpty() && userAsis.isNotEmpty() && offAsis.equals(userAsis, ignoreCase = true)) {
                asistentePoints = 10
            }

            val offMvp = specials.mvp.trim()
            val userMvp = user.predictedMvp.trim()
            if (offMvp.isNotEmpty() && userMvp.isNotEmpty() && offMvp.equals(userMvp, ignoreCase = true)) {
                mvpPoints = 12
            }

            val offPort = specials.portero.trim()
            val userPort = user.predictedPortero.trim()
            if (offPort.isNotEmpty() && userPort.isNotEmpty() && offPort.equals(userPort, ignoreCase = true)) {
                porteroPoints = 10
            }

            val offValla = specials.vallaMenosVencida.trim()
            val userValla = user.predictedVallaMenosVencida.trim()
            if (offValla.isNotEmpty() && userValla.isNotEmpty() && offValla.equals(userValla, ignoreCase = true)) {
                vallaMenosVencidaPoints = 8
            }

            val offJoven = specials.joven.trim()
            val userJoven = user.predictedJoven.trim()
            if (offJoven.isNotEmpty() && userJoven.isNotEmpty() && offJoven.equals(userJoven, ignoreCase = true)) {
                jovenPoints = 8
            }
        }

        val specialPoints = championPoints + subchampionPoints + thirdPlacePoints + goleadorPoints + asistentePoints + mvpPoints + porteroPoints + vallaMenosVencidaPoints + jovenPoints

        return PointBreakdown(
            matchPoints = matchPoints,
            specialPoints = specialPoints,
            exactScoresCount = exactScoresCount,
            correctOutcomesCount = correctOutcomesCount,
            championPoints = championPoints,
            subchampionPoints = subchampionPoints,
            thirdPlacePoints = thirdPlacePoints,
            goleadorPoints = goleadorPoints,
            asistentePoints = asistentePoints,
            mvpPoints = mvpPoints,
            porteroPoints = porteroPoints,
            vallaMenosVencidaPoints = vallaMenosVencidaPoints,
            jovenPoints = jovenPoints
        )
    }

    private fun getOutcome(goals1: Int, goals2: Int): Int {
        return if (goals1 > goals2) 1 else if (goals1 < goals2) 2 else 0
    }
}
