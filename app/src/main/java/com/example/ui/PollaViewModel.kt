package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.PointsCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UserWithPoints(
    val user: UserEntity,
    val breakdown: PointsCalculator.PointBreakdown
)

class PollaViewModel(private val repository: PollaRepository) : ViewModel() {

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMatches: StateFlow<List<MatchEntity>> = repository.allMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPredictions: StateFlow<List<PredictionEntity>> = repository.allPredictions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val officialSpecials: StateFlow<OfficialSpecialsEntity?> = repository.officialSpecials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current logged in / selected participant
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Flag for Admin Mode
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Leaderboard sorted from highest totalPoints to lowest
    val leaderboard: StateFlow<List<UserWithPoints>> = combine(
        allUsers,
        allMatches,
        allPredictions,
        officialSpecials
    ) { users, matches, predictions, specials ->
        users.map { user ->
            val breakdown = PointsCalculator.calculatePointsForUser(
                user = user,
                matches = matches,
                predictions = predictions,
                specials = specials
            )
            UserWithPoints(user, breakdown)
        }.sortedByDescending { it.breakdown.totalPoints }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Populate DB with initial data if empty
        viewModelScope.launch {
            allUsers.first { true } // wait until flow is active
            repository.allUsers.first().let { list ->
                // Migrate "Papá (Eduardo)" to "Papá (Edwin)" if it already exists
                val eduardoUser = list.find { it.name == "Papá (Eduardo)" }
                if (eduardoUser != null) {
                    repository.updateUser(eduardoUser.copy(name = "Papá (Edwin)"))
                }
                val antoniaUser = list.find { it.name == "Antonia" }
                if (antoniaUser != null) {
                    repository.updateUser(antoniaUser.copy(name = "Anto"))
                }
                val salvadorUser = list.find { it.name == "Salvador" }
                if (salvadorUser != null) {
                    repository.updateUser(salvadorUser.copy(name = "Salva"))
                }
                val gracielaUser = list.find { it.name == "Graciela" }
                if (gracielaUser != null) {
                    repository.updateUser(gracielaUser.copy(name = "Sra. Graciela"))
                }
                val mamaUser = list.find { it.name == "Mamá" }
                if (mamaUser != null) {
                    repository.updateUser(mamaUser.copy(name = "Mamá (Chiquis)"))
                }

                val finalUsers = repository.allUsers.first()
                if (finalUsers.isEmpty()) {
                    DbInitializer.populateDatabase(repository)
                    // Auto-select first user after population
                    val updatedUsers = repository.allUsers.first()
                    if (updatedUsers.isNotEmpty()) {
                        _currentUser.value = updatedUsers.firstOrNull { it.name.contains("Anto") } ?: updatedUsers.first()
                    }
                } else {
                    // Auto-select first user
                    _currentUser.value = finalUsers.firstOrNull { it.name.contains("Anto") } ?: finalUsers.first()
                }
            }
        }
    }

    fun selectUser(user: UserEntity) {
        _currentUser.value = user
    }

    fun setAdminMode(enabled: Boolean) {
        _isAdminMode.value = enabled
    }

    // Actions
    fun createNewUser(name: String, avatar: String) {
        viewModelScope.launch {
            val newUser = UserEntity(name = name, avatar = avatar, pin = "0000")
            val newId = repository.insertUser(newUser)
            // Automatically select the new user
            _currentUser.value = newUser.copy(id = newId.toInt())
        }
    }

    fun updateUserName(userId: Int, name: String) {
        viewModelScope.launch {
            repository.allUsers.first().find { it.id == userId }?.let { user ->
                val updatedUser = user.copy(name = name)
                repository.updateUser(updatedUser)
                if (_currentUser.value?.id == userId) {
                    _currentUser.value = updatedUser
                }
            }
        }
    }

    fun updateUserPin(userId: Int, pin: String) {
        viewModelScope.launch {
            repository.allUsers.first().find { it.id == userId }?.let { user ->
                val updatedUser = user.copy(pin = pin)
                repository.updateUser(updatedUser)
                if (_currentUser.value?.id == userId) {
                    _currentUser.value = updatedUser
                }
            }
        }
    }

    fun savePrediction(matchId: Int, goals1: Int, goals2: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val pred = PredictionEntity(
                userId = user.id,
                matchId = matchId,
                predictedGoals1 = goals1,
                predictedGoals2 = goals2
            )
            repository.insertPrediction(pred)
        }
    }

    fun saveMultiplePredictions(predictionsList: List<PredictionEntity>) {
        viewModelScope.launch {
            repository.insertPredictions(predictionsList)
        }
    }

    fun saveSpecialPredictions(
        champion: String,
        subchampion: String,
        thirdPlace: String,
        goleador: String,
        asistente: String,
        mvp: String,
        portero: String,
        vallaMenosVencida: String,
        joven: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updatedUser = user.copy(
                predictedChampion = champion,
                predictedSubchampion = subchampion,
                predictedThirdPlace = thirdPlace,
                predictedGoleador = goleador,
                predictedAsistente = asistente,
                predictedMvp = mvp,
                predictedPortero = portero,
                predictedVallaMenosVencida = vallaMenosVencida,
                predictedJoven = joven
            )
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    // Admin Actions
    fun saveOfficialMatchResult(matchId: Int, goals1: Int, goals2: Int) {
        viewModelScope.launch {
            allMatches.value.find { it.id == matchId }?.let { match ->
                val updatedMatch = match.copy(
                    actualGoals1 = goals1,
                    actualGoals2 = goals2,
                    isCompleted = true
                )
                repository.updateMatch(updatedMatch)
            }
        }
    }

    fun clearOfficialMatchResult(matchId: Int) {
        viewModelScope.launch {
            allMatches.value.find { it.id == matchId }?.let { match ->
                val updatedMatch = match.copy(
                    actualGoals1 = null,
                    actualGoals2 = null,
                    isCompleted = false
                )
                repository.updateMatch(updatedMatch)
            }
        }
    }

    fun toggleMatchOverrideLock(matchId: Int, isOverrideUnlocked: Boolean) {
        viewModelScope.launch {
            allMatches.value.find { it.id == matchId }?.let { match ->
                val updatedMatch = match.copy(
                    isOverrideUnlocked = isOverrideUnlocked
                )
                repository.updateMatch(updatedMatch)
            }
        }
    }

    fun saveOfficialSpecials(
        champion: String,
        subchampion: String,
        thirdPlace: String,
        goleador: String,
        asistente: String,
        mvp: String,
        portero: String,
        vallaMenosVencida: String,
        joven: String
    ) {
        viewModelScope.launch {
            val specials = OfficialSpecialsEntity(
                id = 1,
                champion = champion,
                subchampion = subchampion,
                thirdPlace = thirdPlace,
                goleador = goleador,
                asistente = asistente,
                mvp = mvp,
                portero = portero,
                vallaMenosVencida = vallaMenosVencida,
                joven = joven
            )
            repository.insertOfficialSpecials(specials)
        }
    }
}

class PollaViewModelFactory(private val repository: PollaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PollaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PollaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
