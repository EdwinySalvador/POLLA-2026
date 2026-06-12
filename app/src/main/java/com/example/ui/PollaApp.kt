package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.util.GeminiParserService
import kotlinx.coroutines.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MatchEntity
import com.example.data.OfficialSpecialsEntity
import com.example.data.PredictionEntity
import com.example.data.UserEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Helper function to format kickoff times in Colombian Spanish
fun formatMatchTime(timeMillis: Long): String {
    return try {
        val sdf = SimpleDateFormat("EEEE d 'de' MMMM, HH:mm", Locale("es", "CO"))
        sdf.timeZone = TimeZone.getTimeZone("GMT-5")
        val formatted = sdf.format(Date(timeMillis))
        formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "CO")) else it.toString() }
    } catch (e: Exception) {
        "Fecha por confirmar"
    }
}

val worldCupCountries = listOf(
    "Alemania", "Arabia Saudita", "Argelia", "Argentina", "Australia", "Austria",
    "Bélgica", "Bosnia y Herzegovina", "Brasil", "Cabo Verde", "Canadá", "Catar",
    "Colombia", "Corea del Sur", "Costa de Marfil", "Croacia", "Curazao", "Ecuador",
    "Egipto", "Escocia", "España", "Estados Unidos", "Francia", "Ghana", "Haití",
    "Inglaterra", "Irak", "Irán", "Japón", "Jordania", "Marruecos", "México",
    "Noruega", "Nueva Zelanda", "Países Bajos", "Panamá", "Paraguay", "Portugal",
    "RD Congo", "República Checa", "Senegal", "Sudáfrica", "Suecia", "Suiza",
    "Túnez", "Turquía", "Uruguay", "Uzbekistán"
).sorted()

val starPlayers = listOf(
    "Lionel Messi",
    "Kylian Mbappé",
    "Luis Díaz",
    "James Rodríguez",
    "Jhon Durán",
    "Vinícius Júnior",
    "Jude Bellingham",
    "Harry Kane",
    "Cristiano Ronaldo",
    "Lamine Yamal",
    "Nico Williams",
    "Robert Lewandowski",
    "Mohamed Salah",
    "Kevin De Bruyne",
    "Lautaro Martínez",
    "Julián Álvarez",
    "Rodrygo",
    "Neymar Jr",
    "Phil Foden",
    "Bukayo Saka",
    "Dani Olmo",
    "Bruno Fernandes",
    "Rafael Leão",
    "Jamal Musiala",
    "Florian Wirtz",
    "Kai Havertz",
    "Romelu Lukaku",
    "Achraf Hakimi",
    "Brahim Díaz",
    "Darwin Núñez",
    "Federico Valverde",
    "Moises Caicedo",
    "Enner Valencia",
    "Santiago Giménez",
    "Christian Pulisic",
    "Alphonso Davies",
    "Jonathan David",
    "Federico Chiesa",
    "Cody Gakpo",
    "Xavi Simons",
    "Kaoru Mitoma",
    "Sadio Mané",
    "Nicolas Jackson",
    "Heung-min Son",
    "Luka Modrić",
    "Salomón Rondón"
).sorted()

@Composable
fun PollaDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    showCustomInputOption: Boolean = false,
    customInputPlaceholder: String = "¿Cuál?",
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    // Detect if current value is custom (not in default list, not "Otro" but not empty)
    val isCustomActive = showCustomInputOption && (selectedValue == "Otro" || (selectedValue.isNotEmpty() && !options.contains(selectedValue)))
    val displayValue = if (isCustomActive) "Otro" else selectedValue
    
    var localCustomText by remember(selectedValue) { 
        mutableStateOf(if (isCustomActive) selectedValue else "") 
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = displayValue,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                placeholder = { Text("Selecciona una opción") },
                trailingIcon = {
                    IconButton(onClick = { if (enabled) expanded = !expanded }) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Desplegar")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )
            // Invisible overlays to capture click on the entire textfield
            if (enabled) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = !expanded }
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .heightIn(max = 280.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
            if (showCustomInputOption) {
                DropdownMenuItem(
                    text = { Text("Otro (Escribir...) ✍️") },
                    onClick = {
                        onValueChange("Otro")
                        expanded = false
                    }
                )
            }
        }

        if (isCustomActive || selectedValue == "Otro") {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = localCustomText,
                onValueChange = {
                    localCustomText = it
                    onValueChange(it)
                },
                label = { Text(customInputPlaceholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollaApp(viewModel: PollaViewModel) {
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    val predictions by viewModel.allPredictions.collectAsStateWithLifecycle()
    val specials by viewModel.officialSpecials.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf("leaderboard") }
    var showUserPickerDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var selectedUserForComparison by remember { mutableStateOf<UserWithPoints?>(null) }
    var userPendingSelection by remember { mutableStateOf<UserEntity?>(null) }
    
    val currentTimeMillis = remember { System.currentTimeMillis() }
    val firstMatchTime = matches.firstOrNull { it.id == 1 }?.matchTimeMillis ?: 1780514400000L // Default June 11, 2026 17:00
    val isTourneyStarted = currentTimeMillis >= firstMatchTime

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Polla Mundial 2026",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Predicciones en Familia",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    currentUser?.let { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { showUserPickerDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = user.avatar, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Cambiar Usuario",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == "leaderboard",
                    onClick = { currentTab = "leaderboard" },
                    icon = { Icon(Icons.Default.List, contentDescription = "Tabla") },
                    label = { Text("Tabla 🏆") }
                )
                NavigationBarItem(
                    selected = currentTab == "predictions",
                    onClick = { currentTab = "predictions" },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Partidos") },
                    label = { Text("Partidos ⚽") }
                )
                NavigationBarItem(
                    selected = currentTab == "groups",
                    onClick = { currentTab = "groups" },
                    icon = { Icon(Icons.Default.Menu, contentDescription = "Grupos") },
                    label = { Text("Grupos 📊") }
                )
                NavigationBarItem(
                    selected = currentTab == "specials",
                    onClick = { currentTab = "specials" },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Especiales") },
                    label = { Text("Especiales ⭐") }
                )
                NavigationBarItem(
                    selected = currentTab == "admin",
                    onClick = { currentTab = "admin" },
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Oficial") },
                    label = { Text("Oficial ⚙️") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "leaderboard" -> LeaderboardScreen(
                    leaderboard = leaderboard,
                    currentUser = currentUser,
                    onUserClick = { selectedUserForComparison = it }
                )
                "predictions" -> PredictionsScreen(
                    matches = matches,
                    predictions = predictions,
                    currentUser = currentUser,
                    currentTimeMillis = currentTimeMillis,
                    onSavePrediction = { matchId, goals1, goals2 ->
                        viewModel.savePrediction(matchId, goals1, goals2)
                    },
                    onSaveMultiplePredictions = { list ->
                        viewModel.saveMultiplePredictions(list)
                    }
                )
                "groups" -> GroupsScreen(
                    matches = matches,
                    predictions = predictions,
                    currentUser = currentUser
                )
                "specials" -> SpecialsScreen(
                    currentUser = currentUser,
                    isTourneyStarted = isTourneyStarted,
                    onSaveSpecials = { champ, subc, third, gol, asis, mvp, port, valla, joven ->
                        viewModel.saveSpecialPredictions(champ, subc, third, gol, asis, mvp, port, valla, joven)
                    }
                )
                "admin" -> AdminScreen(
                    matches = matches,
                    specials = specials,
                    isAdminMode = isAdminMode,
                    users = users,
                    onToggleAdmin = { viewModel.setAdminMode(it) },
                    onSaveResult = { matchId, goals1, goals2 ->
                        viewModel.saveOfficialMatchResult(matchId, goals1, goals2)
                    },
                    onClearResult = { matchId ->
                        viewModel.clearOfficialMatchResult(matchId)
                    },
                    onSaveSpecials = { champ, subc, third, gol, asis, mvp, port, valla, joven ->
                        viewModel.saveOfficialSpecials(champ, subc, third, gol, asis, mvp, port, valla, joven)
                    },
                    onUpdateUserPin = { userId, pin ->
                        viewModel.updateUserPin(userId, pin)
                    },
                    onUpdateUserName = { userId, name ->
                        viewModel.updateUserName(userId, name)
                    },
                    onToggleMatchOverrideLock = { matchId, isUnlocked ->
                        viewModel.toggleMatchOverrideLock(matchId, isUnlocked)
                    }
                )
            }
        }
    }

    if (showUserPickerDialog) {
        UserPickerDialog(
            users = users,
            currentUser = currentUser,
            onSelectUser = {
                userPendingSelection = it
                showUserPickerDialog = false
            },
            onAddUserClick = {
                showUserPickerDialog = false
                showAddUserDialog = true
            },
            onDismiss = { showUserPickerDialog = false }
        )
    }

    userPendingSelection?.let { user ->
        UserPinVerificationDialog(
            user = user,
            onPinCorrect = {
                viewModel.selectUser(user)
                userPendingSelection = null
            },
            onDismiss = {
                userPendingSelection = null
            }
        )
    }

    if (showAddUserDialog) {
        AddUserDialog(
            onAddUser = { name, avatar ->
                viewModel.createNewUser(name, avatar)
                showAddUserDialog = false
            },
            onDismiss = { showAddUserDialog = false }
        )
    }

    selectedUserForComparison?.let { userWithPoints ->
        ComparisonDialog(
            userWithPoints = userWithPoints,
            matches = matches,
            predictions = predictions,
            specials = specials,
            onDismiss = { selectedUserForComparison = null }
        )
    }
}

// ---------------------- LEADERBOARD SCREEN ----------------------
@Composable
fun LeaderboardScreen(
    leaderboard: List<UserWithPoints>,
    currentUser: UserEntity?,
    onUserClick: (UserWithPoints) -> Unit
) {
    var activeSubTab by remember { mutableStateOf("standings") } // "standings" or "rules"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Pill tabs switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeSubTab == "standings") TurfGreenPrimary else Color.Transparent)
                    .clickable { activeSubTab = "standings" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏆 Tabla de Posiciones",
                    fontWeight = FontWeight.Bold,
                    color = if (activeSubTab == "standings") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeSubTab == "rules") TurfGreenPrimary else Color.Transparent)
                    .clickable { activeSubTab = "rules" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📋 Reglamento de Puntos",
                    fontWeight = FontWeight.Bold,
                    color = if (activeSubTab == "rules") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (activeSubTab == "rules") {
            ScoresRulesTable()
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = TurfGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sistema de Puntos",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Marcador Exacto: 6 pts • Selección Ganador: 3 pts • Goles Equipo: 2 pts • Diferencia: 2 pts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Text(
                text = "Posiciones de la Familia",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

        if (leaderboard.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(leaderboard) { item ->
                    val isCurrentUser = item.user.id == currentUser?.id
                    val index = leaderboard.indexOf(item)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUserClick(item) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentUser) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isCurrentUser) {
                            CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                        } else null
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (index) {
                                            0 -> Color(0xFFFFD700)
                                            1 -> Color(0xFFC0C0C0)
                                            2 -> Color(0xFFCD7F32)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (index) {
                                        0 -> "🥇"
                                        1 -> "🥈"
                                        2 -> "🥉"
                                        else -> "${index + 1}"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (index > 2) 13.sp else 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(text = item.user.avatar, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.user.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Exacto: ${item.breakdown.exactScoresCount} • Ganados: ${item.breakdown.correctOutcomesCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${item.breakdown.totalPoints}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "pts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Text(
            text = "💡 Toca sobre un jugador para ver y comparar su cartón de pronósticos.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}
}

// ---------------------- PREDICTIONS SCREEN ----------------------
@Composable
fun PredictionsScreen(
    matches: List<MatchEntity>,
    predictions: List<PredictionEntity>,
    currentUser: UserEntity?,
    currentTimeMillis: Long,
    onSavePrediction: (Int, Int, Int) -> Unit,
    onSaveMultiplePredictions: (List<PredictionEntity>) -> Unit
) {
    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Por favor selecciona un perfil", fontWeight = FontWeight.Bold)
        }
        return
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }
    var parsedResultsToReview by remember { mutableStateOf<List<GeminiParserService.ParsedPrediction>?>(null) }
    var showExampleFormatDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isScanning = true
                scanError = null
                try {
                    val results = GeminiParserService.parsePredictionsFromImage(context, uri, matches)
                    if (results.isEmpty()) {
                        scanError = "No se detectaron predicciones legibles en la imagen."
                    } else {
                        parsedResultsToReview = results
                    }
                } catch (e: Exception) {
                    scanError = e.message ?: "Error desconocido al escanear la imagen."
                } finally {
                    isScanning = false
                }
            }
        }
    }

    val userPredictionsMap = remember(predictions, currentUser) {
        predictions.filter { it.userId == currentUser.id }.associateBy { it.matchId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "${currentUser.avatar} Pronósticos de ${currentUser.name}",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // --- AI TEMPLATE SCANNER CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📸", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Escáner Inteligente con IA",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "¿La señora Graciela o Salvador llenaron una plantilla física? Sube una foto de su papel y Gemini cargará los resultados al instante.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isScanning) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Gemini está descifrando la plantilla escrita...",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("📸 Elegir o Tomar Foto", style = MaterialTheme.typography.labelSmall)
                        }

                        FilledTonalButton(
                            onClick = {
                                coroutineScope.launch {
                                    isScanning = true
                                    delay(1200)
                                    parsedResultsToReview = listOf(
                                        GeminiParserService.ParsedPrediction(matchId = 1, goals1 = 2, goals2 = 1),
                                        GeminiParserService.ParsedPrediction(matchId = 2, goals1 = 0, goals2 = 0),
                                        GeminiParserService.ParsedPrediction(matchId = 3, goals1 = 1, goals2 = 2),
                                        GeminiParserService.ParsedPrediction(matchId = 4, goals1 = 3, goals2 = 1),
                                        GeminiParserService.ParsedPrediction(matchId = 5, goals1 = 1, goals2 = 1),
                                        GeminiParserService.ParsedPrediction(matchId = 6, goals1 = 2, goals2 = 0)
                                    )
                                    isScanning = false
                                }
                            },
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🧪 Probar Demo", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                scanError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📝 Ver cómo deben anotar en el papel",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { showExampleFormatDialog = true }
                        .padding(vertical = 4.dp)
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(matches) { match ->
                val prediction = userPredictionsMap[match.id]
                val predG1 = prediction?.predictedGoals1 ?: 0
                val predG2 = prediction?.predictedGoals2 ?: 0
                val isSaved = prediction != null
                val isLocked = currentTimeMillis >= match.matchTimeMillis && !match.isOverrideUnlocked

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLocked) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${match.groupCode} • ${match.stadium}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            if (isLocked) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Bloqueado",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "REGISTRO CERRADO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSaved) TurfGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Edit,
                                        contentDescription = "Estado",
                                        tint = if (isSaved) TurfGreenPrimary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isSaved) "GUARDADO" else "EDITABLE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSaved) TurfGreenPrimary else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Text(
                            text = formatMatchTime(match.matchTimeMillis),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = match.team1,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.2f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Goals Team 1 spinner
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (!isLocked) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            .clickable {
                                                if (predG1 > 0) {
                                                    onSavePrediction(match.id, predG1 - 1, predG2)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Text(
                                    text = "$predG1",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    color = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (!isLocked) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            .clickable {
                                                onSavePrediction(match.id, predG1 + 1, predG2)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            Text(
                                text = "-",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.outline
                            )

                            // Goals Team 2 spinner
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (!isLocked) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            .clickable {
                                                if (predG2 > 0) {
                                                    onSavePrediction(match.id, predG1, predG2 - 1)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Text(
                                    text = "$predG2",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    color = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (!isLocked) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            .clickable {
                                                onSavePrediction(match.id, predG1, predG2 + 1)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            Text(
                                text = match.team2,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.weight(1.2f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (match.isCompleted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Resultado Oficial: ${match.actualGoals1} - ${match.actualGoals2}",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                val exact = predG1 == match.actualGoals1 && predG2 == match.actualGoals2
                                val actualOt = getOutcome(match.actualGoals1 ?: 0, match.actualGoals2 ?: 0)
                                val predOt = getOutcome(predG1, predG2)
                                val points = when {
                                    exact -> 5
                                    actualOt == predOt -> 3
                                    else -> 0
                                }

                                Text(
                                    text = if (points > 0) "+$points pts! 🔥" else "0 pts",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (points > 0) TurfGreenPrimary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        if (parsedResultsToReview != null) {
            ReviewScannedPredictionsDialog(
                currentUser = currentUser,
                parsedPredictions = parsedResultsToReview!!,
                matches = matches,
                onConfirm = { list ->
                    onSaveMultiplePredictions(list)
                    parsedResultsToReview = null
                },
                onDismiss = {
                    parsedResultsToReview = null
                }
            )
        }

        if (showExampleFormatDialog) {
            ShowExampleFormatDialog(
                onDismiss = { showExampleFormatDialog = false }
            )
        }
    }
}

private fun getOutcome(goals1: Int, goals2: Int): Int {
    return if (goals1 > goals2) 1 else if (goals1 < goals2) 2 else 0
}

@Composable
fun ReviewScannedPredictionsDialog(
    currentUser: UserEntity,
    parsedPredictions: List<GeminiParserService.ParsedPrediction>,
    matches: List<MatchEntity>,
    onConfirm: (List<PredictionEntity>) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "📋 Revisar Pronósticos de Foto/IA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Gemini detectó ${parsedPredictions.size} predicciones para ${currentUser.name}. Revísalas antes de guardarlas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(parsedPredictions) { parsed ->
                        val matchingMatch = matches.find { it.id == parsed.matchId }
                        if (matchingMatch != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${matchingMatch.groupCode} • Partido #${matchingMatch.id}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${matchingMatch.team1} vs ${matchingMatch.team2}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${parsed.goals1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = " - ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${parsed.goals2}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val predictionEntities = parsedPredictions.map {
                                PredictionEntity(
                                    userId = currentUser.id,
                                    matchId = it.matchId,
                                    predictedGoals1 = it.goals1,
                                    predictedGoals2 = it.goals2
                                )
                            }
                            onConfirm(predictionEntities)
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Confirmar y Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun ShowExampleFormatDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "📝 Formato Escrito Recomendado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Para que la Sra. Graciela o Salvador llenen su planilla de partidos en papel, diles que anoten de forma legible indicando los números de partido:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "PLANTILLA COPILADA MUNDIAL 2026\n" +
                                   "Participante: Sra. Graciela / Salvador\n\n" +
                                   "Partido #1: México [ 2 ] vs [ 1 ] Sudáfrica\n" +
                                   "Partido #2: República [ 0 ] vs [ 0 ] Rep Checa\n" +
                                   "Partido #3: Canadá [ 1 ] vs [ 2 ] Bosnia\n" +
                                   "Partido #4: EE.UU. [ 3 ] vs [ 1 ] Paraguay\n" +
                                   "Partido #5: Croacia [ 1 ] vs [ 1 ] Ecuador\n" +
                                   "...\n",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "💡 Sugerencia: Al tomar la fotografía enfoca recto con buena luz para un escaneo 100% preciso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entendido, ¡gracias!")
                }
            }
        }
    }
}

// ---------------------- SPECIALS PREDICTIONS SCREEN ----------------------
@Composable
fun SpecialsScreen(
    currentUser: UserEntity?,
    isTourneyStarted: Boolean,
    onSaveSpecials: (String, String, String, String, String, String, String, String, String) -> Unit
) {
    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Por favor selecciona un perfil", fontWeight = FontWeight.Bold)
        }
        return
    }

    var champ by remember(currentUser) { mutableStateOf(currentUser.predictedChampion) }
    var subc by remember(currentUser) { mutableStateOf(currentUser.predictedSubchampion) }
    var third by remember(currentUser) { mutableStateOf(currentUser.predictedThirdPlace) }
    var gol by remember(currentUser) { mutableStateOf(currentUser.predictedGoleador) }
    var asis by remember(currentUser) { mutableStateOf(currentUser.predictedAsistente) }
    var mvp by remember(currentUser) { mutableStateOf(currentUser.predictedMvp) }
    var port by remember(currentUser) { mutableStateOf(currentUser.predictedPortero) }
    var valla by remember(currentUser) { mutableStateOf(currentUser.predictedVallaMenosVencida) }
    var joven by remember(currentUser) { mutableStateOf(currentUser.predictedJoven) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Predicciones Especiales ⭐",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Estas predicciones otorgan puntos según el reglamento (Total 91 puntos posibles). Se bloquean automáticamente al iniciar el mundial.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PollaDropdown(
                    label = "🏆 1. Campeón del Mundo (15 pts)",
                    selectedValue = champ,
                    options = worldCupCountries,
                    onValueChange = { if (!isTourneyStarted) champ = it },
                    enabled = !isTourneyStarted
                )

                PollaDropdown(
                    label = "🥈 2. Subcampeón del Mundo (10 pts)",
                    selectedValue = subc,
                    options = worldCupCountries,
                    onValueChange = { if (!isTourneyStarted) subc = it },
                    enabled = !isTourneyStarted
                )

                PollaDropdown(
                    label = "🥉 3. Tercer Puesto (8 pts)",
                    selectedValue = third,
                    options = worldCupCountries,
                    onValueChange = { if (!isTourneyStarted) third = it },
                    enabled = !isTourneyStarted
                )

                PollaDropdown(
                    label = "⚽ 4. Goleador - Bota de Oro (10 pts)",
                    selectedValue = gol,
                    options = starPlayers,
                    onValueChange = { if (!isTourneyStarted) gol = it },
                    showCustomInputOption = true,
                    customInputPlaceholder = "¿Qué jugador?",
                    enabled = !isTourneyStarted
                )

                PollaDropdown(
                    label = "👟 5. Máximo Asistente (10 pts)",
                    selectedValue = asis,
                    options = starPlayers,
                    onValueChange = { if (!isTourneyStarted) asis = it },
                    showCustomInputOption = true,
                    customInputPlaceholder = "¿Qué jugador?",
                    enabled = !isTourneyStarted
                )

                PollaDropdown(
                    label = "🌟 6. Jugador Más Valioso - MVP (12 pts)",
                    selectedValue = mvp,
                    options = starPlayers,
                    onValueChange = { if (!isTourneyStarted) mvp = it },
                    showCustomInputOption = true,
                    customInputPlaceholder = "¿Qué jugador?",
                    enabled = !isTourneyStarted
                )

                PollaDropdown(
                    label = "🧤 7. Mejor Portero - Guante de Oro (10 pts)",
                    selectedValue = port,
                    options = worldCupCountries,
                    onValueChange = { if (!isTourneyStarted) port = it },
                    enabled = !isTourneyStarted
                )

                PollaDropdown(
                    label = "🛡️ 8. Valla Menos Vencida - Selección (8 pts)",
                    selectedValue = valla,
                    options = worldCupCountries,
                    onValueChange = { if (!isTourneyStarted) valla = it },
                    enabled = !isTourneyStarted
                )

                PollaDropdown(
                    label = "👶 9. Mejor Jugador Joven (8 pts)",
                    selectedValue = joven,
                    options = starPlayers,
                    onValueChange = { if (!isTourneyStarted) joven = it },
                    showCustomInputOption = true,
                    customInputPlaceholder = "¿Qué jugador?",
                    enabled = !isTourneyStarted
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isTourneyStarted) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Bloqueados", tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "El mundial ya inició. No es posible editar especiales.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            onSaveSpecials(champ, subc, third, gol, asis, mvp, port, valla, joven)
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Predicciones Especiales")
                    }
                }
            }
        }
    }
}

// ---------------------- OFFICIAL RESULTS / ADMIN SCREEN ----------------------
@Composable
fun AdminScreen(
    matches: List<MatchEntity>,
    specials: OfficialSpecialsEntity?,
    isAdminMode: Boolean,
    users: List<UserEntity>,
    onToggleAdmin: (Boolean) -> Unit,
    onSaveResult: (Int, Int, Int) -> Unit,
    onClearResult: (Int) -> Unit,
    onSaveSpecials: (String, String, String, String, String, String, String, String, String) -> Unit,
    onUpdateUserPin: (Int, String) -> Unit,
    onUpdateUserName: (Int, String) -> Unit,
    onToggleMatchOverrideLock: (Int, Boolean) -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var showPinError by remember { mutableStateOf(false) }

    var editChamp by remember(specials) { mutableStateOf(specials?.champion ?: "") }
    var editSubc by remember(specials) { mutableStateOf(specials?.subchampion ?: "") }
    var editThird by remember(specials) { mutableStateOf(specials?.thirdPlace ?: "") }
    var editGol by remember(specials) { mutableStateOf(specials?.goleador ?: "") }
    var editAsis by remember(specials) { mutableStateOf(specials?.asistente ?: "") }
    var editMvp by remember(specials) { mutableStateOf(specials?.mvp ?: "") }
    var editPort by remember(specials) { mutableStateOf(specials?.portero ?: "") }
    var editValla by remember(specials) { mutableStateOf(specials?.vallaMenosVencida ?: "") }
    var editJoven by remember(specials) { mutableStateOf(specials?.joven ?: "") }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Resultados Oficiales ⚙️",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        if (!isAdminMode) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "🔓 Certificación",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Modo Administrador Requerido",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Accede para actualizar los marcadores oficiales y especiales del mundial. Clave familiar por defecto: 2026",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinInput = it
                            showPinError = false
                        },
                        label = { Text("PIN de Administrador") },
                        placeholder = { Text("Clave familiar") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            if (pinInput == "2026") {
                                onToggleAdmin(true)
                                pinInput = ""
                            } else {
                                showPinError = true
                            }
                        }),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = showPinError,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )

                    if (showPinError) {
                        Text(
                            text = "❌ PIN incorrecto. Pista: Es el año del Mundial (2026)",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (pinInput == "2026") {
                                onToggleAdmin(true)
                                pinInput = ""
                            } else {
                                showPinError = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Activar Modo Admin 🔐")
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔒 MODO ADMINISTRADOR ACTIVO",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 13.sp
                )
                Button(
                    onClick = { onToggleAdmin(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Cerrar 🔓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "👥 Configuración de Jugadores 🔐",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = "Como administrador, puedes editar los nombres de los jugadores de acuerdo a cómo prefieran identificarse, y asignarles un PIN individual para proteger sus pronósticos (PIN por defecto: 0000).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            users.forEach { u ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = u.avatar, fontSize = 24.sp, modifier = Modifier.padding(end = 2.dp))
                                    
                                    var localName by remember(u.name) { mutableStateOf(u.name) }
                                    OutlinedTextField(
                                        value = localName,
                                        onValueChange = { newValue ->
                                            localName = newValue
                                            onUpdateUserName(u.id, newValue)
                                        },
                                        label = { Text("Nombre", style = MaterialTheme.typography.labelSmall) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1.3f),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    )
                                    
                                    var localPin by remember(u.pin) { mutableStateOf(u.pin) }
                                    OutlinedTextField(
                                        value = localPin,
                                        onValueChange = { newValue ->
                                            if (newValue.length <= 8) {
                                                localPin = newValue
                                                onUpdateUserPin(u.id, newValue)
                                            }
                                        },
                                        label = { Text("PIN", style = MaterialTheme.typography.labelSmall) },
                                        placeholder = { Text("0000") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(90.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, fontSize = 14.sp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "⭐ Especiales Oficiales",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            PollaDropdown(
                                label = "Campeón Oficial",
                                selectedValue = editChamp,
                                options = worldCupCountries,
                                onValueChange = { editChamp = it }
                            )

                            PollaDropdown(
                                label = "Subcampeón Oficial",
                                selectedValue = editSubc,
                                options = worldCupCountries,
                                onValueChange = { editSubc = it }
                            )

                            PollaDropdown(
                                label = "Tercer Puesto Oficial",
                                selectedValue = editThird,
                                options = worldCupCountries,
                                onValueChange = { editThird = it }
                            )

                            PollaDropdown(
                                label = "Goleador Oficial",
                                selectedValue = editGol,
                                options = starPlayers,
                                onValueChange = { editGol = it },
                                showCustomInputOption = true
                            )

                            PollaDropdown(
                                label = "Asistente Oficial",
                                selectedValue = editAsis,
                                options = starPlayers,
                                onValueChange = { editAsis = it },
                                showCustomInputOption = true
                            )

                            PollaDropdown(
                                label = "MVP Oficial",
                                selectedValue = editMvp,
                                options = starPlayers,
                                onValueChange = { editMvp = it },
                                showCustomInputOption = true
                            )

                            PollaDropdown(
                                label = "Mejor Portero Oficial",
                                selectedValue = editPort,
                                options = worldCupCountries,
                                onValueChange = { editPort = it }
                            )

                            PollaDropdown(
                                label = "Valla Menos Vencida Oficial",
                                selectedValue = editValla,
                                options = worldCupCountries,
                                onValueChange = { editValla = it }
                            )

                            PollaDropdown(
                                label = "Mejor Jugador Joven Oficial",
                                selectedValue = editJoven,
                                options = starPlayers,
                                onValueChange = { editJoven = it },
                                showCustomInputOption = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    onSaveSpecials(editChamp, editSubc, editThird, editGol, editAsis, editMvp, editPort, editValla, editJoven)
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Guardar Especiales Oficiales")
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "⚽ Marcadores Oficiales de Partidos",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(matches) { match ->
                    val actualG1 = match.actualGoals1 ?: 0
                    val actualG2 = match.actualGoals2 ?: 0
                    val isCompleted = match.isCompleted

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${match.groupCode} • ${match.stadium}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )

                                if (isCompleted) {
                                    Text(
                                        text = "FINALIZADO ✅",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TurfGreenPrimary
                                    )
                                } else {
                                    Text(
                                        text = "PENDIENTE ⏳",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Text(
                                text = "${match.team1} vs ${match.team2}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = match.team1, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                .clickable {
                                                    if (actualG1 > 0) {
                                                        onSaveResult(match.id, actualG1 - 1, actualG2)
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Text(
                                            text = "$actualG1",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                .clickable {
                                                    onSaveResult(match.id, actualG1 + 1, actualG2)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(32.dp))

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = match.team2, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                .clickable {
                                                    if (actualG2 > 0) {
                                                        onSaveResult(match.id, actualG1, actualG2 - 1)
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Text(
                                            text = "$actualG2",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                .clickable {
                                                    onSaveResult(match.id, actualG1, actualG2 + 1)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onToggleMatchOverrideLock(match.id, !match.isOverrideUnlocked)
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (match.isOverrideUnlocked) Icons.Default.Edit else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (match.isOverrideUnlocked) TurfGreenPrimary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Apertura de Pronósticos 🔓",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (match.isOverrideUnlocked) TurfGreenPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (match.isOverrideUnlocked) "Permitiendo registrar pronósticos tarde" else "Bloqueo por horario normal de partido",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (match.isOverrideUnlocked) TurfGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                Switch(
                                    checked = match.isOverrideUnlocked,
                                    onCheckedChange = { onToggleMatchOverrideLock(match.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = TurfGreenPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (!isCompleted) {
                                Button(
                                    onClick = { onSaveResult(match.id, actualG1, actualG2) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Confirmar Marcador Oficial")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onClearResult(match.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Anular Marcador (Volver a pendiente)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserPinVerificationDialog(
    user: UserEntity,
    onPinCorrect: () -> Unit,
    onDismiss: () -> Unit
) {
    var pinValue by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "PIN de Seguridad",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Confirmar Identidad",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ingresa el PIN de ${user.avatar} ${user.name} para activar su perfil.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pinValue,
                    onValueChange = {
                        pinValue = it
                        pinError = false
                    },
                    label = { Text("PIN de Seguridad") },
                    placeholder = { Text("PIN por defecto: 0000") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    isError = pinError,
                    modifier = Modifier.fillMaxWidth()
                )

                if (pinError) {
                    Text(
                        text = "PIN Incorrecto. Intenta de nuevo.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (pinValue == user.pin) {
                                onPinCorrect()
                            } else {
                                pinError = true
                            }
                        }
                    ) {
                        Text("Ingresar")
                    }
                }
            }
        }
    }
}

// ---------------------- DIALOG: USER PICKER ----------------------
@Composable
fun UserPickerDialog(
    users: List<UserEntity>,
    currentUser: UserEntity?,
    onSelectUser: (UserEntity) -> Unit,
    onAddUserClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Seleccionar Jugador ⚽",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 300.dp)) {
                    items(users) { user ->
                        val isSelected = user.id == currentUser?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { onSelectUser(user) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = user.avatar, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Activo", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onAddUserClick) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuevo Jugador")
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

// ---------------------- DIALOG: ADD USER ----------------------
@Composable
fun AddUserDialog(
    onAddUser: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf("⚽") }
    val avatars = listOf("⚽", "👧", "👦", "👵", "👩", "👨", "🦁", "🏆", "🌟", "🥅")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Añadir Participante",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Selecciona un Avatar:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(avatars) { avatar ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (selectedAvatar == avatar) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { selectedAvatar = avatar }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = avatar, fontSize = 24.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.trim().isNotEmpty()) {
                                onAddUser(name.trim(), selectedAvatar)
                            }
                        },
                        enabled = name.trim().isNotEmpty()
                    ) {
                        Text("Agregar")
                    }
                }
            }
        }
    }
}

// ---------------------- DIALOG: COMPARATIVE COMPARISON ----------------------
@Composable
fun ComparisonDialog(
    userWithPoints: UserWithPoints,
    matches: List<MatchEntity>,
    predictions: List<PredictionEntity>,
    specials: OfficialSpecialsEntity?,
    onDismiss: () -> Unit
) {
    val user = userWithPoints.user
    val userPredictionsMap = remember(predictions, user) {
        predictions.filter { it.userId == user.id }.associateBy { it.matchId }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = user.avatar, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pronósticos de ${user.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Total Acumulado: ${userWithPoints.breakdown.totalPoints} puntos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⭐ Predicciones Especiales:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                SpecialCompRow(
                                    label = "1. Campeón:",
                                    predValue = user.predictedChampion,
                                    offValue = specials?.champion ?: "",
                                    points = userWithPoints.breakdown.championPoints
                                )
                                SpecialCompRow(
                                    label = "2. Subcampeón:",
                                    predValue = user.predictedSubchampion,
                                    offValue = specials?.subchampion ?: "",
                                    points = userWithPoints.breakdown.subchampionPoints
                                )
                                SpecialCompRow(
                                    label = "3. Tercer Puesto:",
                                    predValue = user.predictedThirdPlace,
                                    offValue = specials?.thirdPlace ?: "",
                                    points = userWithPoints.breakdown.thirdPlacePoints
                                )
                                SpecialCompRow(
                                    label = "4. Goleador:",
                                    predValue = user.predictedGoleador,
                                    offValue = specials?.goleador ?: "",
                                    points = userWithPoints.breakdown.goleadorPoints
                                )
                                SpecialCompRow(
                                    label = "5. Máximo Asistente:",
                                    predValue = user.predictedAsistente,
                                    offValue = specials?.asistente ?: "",
                                    points = userWithPoints.breakdown.asistentePoints
                                )
                                SpecialCompRow(
                                    label = "6. Jugador MVP:",
                                    predValue = user.predictedMvp,
                                    offValue = specials?.mvp ?: "",
                                    points = userWithPoints.breakdown.mvpPoints
                                )
                                SpecialCompRow(
                                    label = "7. Mejor Portero:",
                                    predValue = user.predictedPortero,
                                    offValue = specials?.portero ?: "",
                                    points = userWithPoints.breakdown.porteroPoints
                                )
                                SpecialCompRow(
                                    label = "8. Valla Menos Vencida:",
                                    predValue = user.predictedVallaMenosVencida,
                                    offValue = specials?.vallaMenosVencida ?: "",
                                    points = userWithPoints.breakdown.vallaMenosVencidaPoints
                                )
                                SpecialCompRow(
                                    label = "9. Mejor Jugador Joven:",
                                    predValue = user.predictedJoven,
                                    offValue = specials?.joven ?: "",
                                    points = userWithPoints.breakdown.jovenPoints
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(matches) { match ->
                        val pred = userPredictionsMap[match.id]
                        val predGoals1 = pred?.predictedGoals1 ?: 0
                        val predGoals2 = pred?.predictedGoals2 ?: 0
                        
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${match.team1} vs ${match.team2}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Pronóstico: $predGoals1 - $predGoals2",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (match.isCompleted) {
                                                "Oficial: ${match.actualGoals1} - ${match.actualGoals2}"
                                            } else {
                                                "Pendiente"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (match.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    if (match.isCompleted) {
                                        val exact = predGoals1 == match.actualGoals1 && predGoals2 == match.actualGoals2
                                        val actualOt = getOutcome(match.actualGoals1 ?: 0, match.actualGoals2 ?: 0)
                                        val predOt = getOutcome(predGoals1, predGoals2)
                                        val points = when {
                                            exact -> 5
                                            actualOt == predOt -> 3
                                            else -> 0
                                        }

                                        Text(
                                            text = if (points > 0) "+$points pts" else "0 pts",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (points > 0) TurfGreenPrimary else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Volver")
                }
            }
        }
    }
}

@Composable
fun SpecialCompRow(label: String, predValue: String, offValue: String, points: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "Pronosticó: ${predValue.ifEmpty { "(Vacío)" }}",
                style = MaterialTheme.typography.bodySmall
            )
            if (offValue.isNotEmpty()) {
                Text(
                    text = "Oficial: $offValue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        if (offValue.isNotEmpty()) {
            Text(
                text = if (points > 0) "Correcto (+10)" else "Incorrecto (0)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (points > 0) TurfGreenPrimary else MaterialTheme.colorScheme.error
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}

data class TeamStanding(
    val name: String,
    var played: Int = 0,
    var won: Int = 0,
    var drawn: Int = 0,
    var lost: Int = 0,
    var goalsFor: Int = 0,
    var goalsAgainst: Int = 0
) {
    val points: Int get() = won * 3 + drawn
    val goalDifference: Int get() = goalsFor - goalsAgainst
}

@Composable
fun GroupsScreen(
    matches: List<MatchEntity>,
    predictions: List<PredictionEntity>,
    currentUser: UserEntity?
) {
    val groupList = listOf(
        "Grupo A", "Grupo B", "Grupo C", "Grupo D", "Grupo E", "Grupo F",
        "Grupo G", "Grupo H", "Grupo I", "Grupo J", "Grupo K", "Grupo L"
    )
    var selectedGroup by remember { mutableStateOf("Grupo A") }

    val userPredictionsMap = remember(predictions, currentUser) {
        if (currentUser != null) {
            predictions.filter { it.userId == currentUser.id }.associateBy { it.matchId }
        } else {
            emptyMap()
        }
    }

    // Filter matches belonging to the selected group
    val matchesInGroup = remember(matches, selectedGroup) {
        matches.filter { it.groupCode == selectedGroup }
    }

    // Calculate standings dynamically
    val standings = remember(matchesInGroup, userPredictionsMap) {
        val standingsMap = mutableMapOf<String, TeamStanding>()

        for (match in matchesInGroup) {
            // Check prediction
            val pred = userPredictionsMap[match.id]
            val goals1: Int?
            val goals2: Int?
            val isPlayed: Boolean

            if (pred != null) {
                goals1 = pred.predictedGoals1
                goals2 = pred.predictedGoals2
                isPlayed = true
            } else if (match.isCompleted) {
                goals1 = match.actualGoals1
                goals2 = match.actualGoals2
                isPlayed = true
            } else {
                goals1 = null
                goals2 = null
                isPlayed = false
            }

            // Ensure teams are represented
            standingsMap.getOrPut(match.team1) { TeamStanding(match.team1) }
            standingsMap.getOrPut(match.team2) { TeamStanding(match.team2) }

            if (isPlayed && goals1 != null && goals2 != null) {
                val t1 = standingsMap[match.team1]!!
                val t2 = standingsMap[match.team2]!!

                t1.played++
                t2.played++
                t1.goalsFor += goals1
                t1.goalsAgainst += goals2
                t2.goalsFor += goals2
                t2.goalsAgainst += goals1

                when {
                    goals1 > goals2 -> {
                        t1.won++
                        t2.lost++
                    }
                    goals1 < goals2 -> {
                        t2.won++
                        t1.lost++
                    }
                    else -> {
                        t1.drawn++
                        t2.drawn++
                    }
                }
            }
        }

        // Sort standings: Points -> Goal Diff -> Goals For -> Name
        standingsMap.values.sortedWith(
            compareByDescending<TeamStanding> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
                .thenBy { it.name }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Posiciones por Grupo 📊",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Esta tabla se calcula en tiempo real considerando tus pronósticos guardados y los partidos jugados oficialmente.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Horizontal Group Selectors
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            items(groupList) { g ->
                val isSelected = selectedGroup == g
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { selectedGroup = g }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = g,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#",
                        modifier = Modifier.width(26.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Equipo",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    listOf("PJ", "G", "E", "P", "GF", "GC", "DG", "PTS").forEach { header ->
                        Text(
                            text = header,
                            modifier = Modifier.width(if (header == "PTS") 32.dp else 24.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (header == "PTS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (standings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay equipos registrados en este grupo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    standings.forEachIndexed { index, team ->
                        val pos = index + 1
                        // Green marker for 1st and 2nd qualifying spots
                        val qualifies = pos <= 2

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pos
                            Text(
                                text = pos.toString(),
                                modifier = Modifier.width(26.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (qualifies) TurfGreenPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            // Team Name with a subtle qualifier indicator (e.g. green circle or text color)
                            Text(
                                text = team.name,
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp,
                                fontWeight = if (qualifies) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // PJ
                            Text(
                                text = team.played.toString(),
                                modifier = Modifier.width(24.dp),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // G
                            Text(
                                text = team.won.toString(),
                                modifier = Modifier.width(24.dp),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // E
                            Text(
                                text = team.drawn.toString(),
                                modifier = Modifier.width(24.dp),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // P
                            Text(
                                text = team.lost.toString(),
                                modifier = Modifier.width(24.dp),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // GF
                            Text(
                                text = team.goalsFor.toString(),
                                modifier = Modifier.width(24.dp),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            // GC
                            Text(
                                text = team.goalsAgainst.toString(),
                                modifier = Modifier.width(24.dp),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            // DG
                            val dg = team.goalDifference
                            val dgStr = if (dg > 0) "+$dg" else dg.toString()
                            Text(
                                text = dgStr,
                                modifier = Modifier.width(24.dp),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                color = when {
                                    dg > 0 -> TurfGreenPrimary
                                    dg < 0 -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }
                            )

                            // PTS
                            Text(
                                text = team.points.toString(),
                                modifier = Modifier.width(32.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                color = if (qualifies) TurfGreenPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (index < standings.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Los dos primeros lugares (#1 y #2) de cada grupo clasifican automáticamente a la siguiente fase.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun ScoresRulesTable() {
    val rulesList = listOf(
        RuleItem("Marcador Exacto", "6", isMatchStyle = true),
        RuleItem("Selección de Ganador", "3", isMatchStyle = true),
        RuleItem("Goles de un Equipo", "2", isMatchStyle = true),
        RuleItem("Diferencia de gol", "2", isMatchStyle = true),
        RuleItem("Escoge Campeón", "15", isSpecialStyle = true),
        RuleItem("Escoge Sub Campeón", "10", isSpecialStyle = true),
        RuleItem("Escoge 3er Puesto", "8", isSpecialStyle = true),
        RuleItem("Escoge Goleador", "10", isSpecialStyle = true),
        RuleItem("Escoge el Máximo Asistente", "10", isSpecialStyle = true),
        RuleItem("Escoge el Jugador Más Valioso", "12", isSpecialStyle = true),
        RuleItem("Escoge el Mejor Portero", "10", isSpecialStyle = true),
        RuleItem("Malla Menos Vencida", "8", isSpecialStyle = true),
        RuleItem("Mejor Jugador Joven", "8", isSpecialStyle = true)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A13)),
        border = androidx.compose.foundation.BorderStroke(1.dp, TurfGreenPrimary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "¿CÓMO SUMAS PUNTOS?",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Color(0xFFFFEB3B),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TurfGreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DESCRIPCIÓN",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "PUNTAJE",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            rulesList.forEach { rule ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rule.description,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = rule.points,
                        color = if (rule.isMatchStyle) Color(0xFF4CAF50) else Color(0xFFFFC107),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = TurfGreenPrimary.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "⚽ Nota de Partidos:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Los aciertos de Partido son sumativos si no son Marcador Exacto. Por ejemplo, si predices 2-1 y queda 3-1: ganas 3 pts por Ganador y 2 pts por encajar el gol exacto del equipo 2, con un total de 5 pts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

data class RuleItem(
    val description: String,
    val points: String,
    val isMatchStyle: Boolean = false,
    val isSpecialStyle: Boolean = false
)
