package com.example.data

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DbInitializer {

    private fun getMillisFromDate(dateString: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("GMT-5") // Colombia Time is UTC-5
            sdf.parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun createM(
        id: Int,
        team1: String,
        team2: String,
        dateStr: String,
        groupCode: String,
        stadium: String
    ): MatchEntity {
        return MatchEntity(
            id = id,
            team1 = team1,
            team2 = team2,
            matchTimeMillis = getMillisFromDate(dateStr),
            groupCode = groupCode,
            stadium = stadium
        )
    }

    suspend fun populateDatabase(repo: PollaRepository) {
        // 1. Pre-populate default users
        val defaultUsers = listOf(
            UserEntity(name = "Anto", avatar = "👧", pin = "0000"),
            UserEntity(name = "Salva", avatar = "👦", pin = "0000"),
            UserEntity(name = "Sra. Graciela", avatar = "👵", pin = "0000"),
            UserEntity(name = "Papá (Edwin)", avatar = "👨", pin = "0000"),
            UserEntity(name = "Mamá (Chiquis)", avatar = "👩", pin = "0000")
        )
        for (user in defaultUsers) {
            repo.insertUser(user)
        }

        // 2. Pre-populate 72 matches of World Cup 2026 starting June 11, 2026 in Colombian Time (-1 hour from EDT)
        val matches = listOf(
            // Jueves 11
            createM(1, "México", "Sudáfrica", "2026-06-11 14:00", "Grupo A", "Estadio Ciudad de México"),
            createM(2, "Corea del Sur", "República Checa", "2026-06-11 21:00", "Grupo A", "Estadio Guadalajara"),
            // Viernes 12
            createM(3, "Canadá", "Bosnia y Herzegovina", "2026-06-12 14:00", "Grupo B", "Estadio Toronto"),
            createM(4, "Estados Unidos", "Paraguay", "2026-06-12 20:00", "Grupo D", "Estadio Los Ángeles"),
            // Sábado 13
            createM(5, "Catar", "Suiza", "2026-06-13 14:00", "Grupo B", "Estadio Bahía de San Francisco"),
            createM(6, "Brasil", "Marruecos", "2026-06-13 17:00", "Grupo C", "Estadio Nueva York Nueva Jersey"),
            createM(7, "Haití", "Escocia", "2026-06-13 20:00", "Grupo C", "Estadio Boston"),
            createM(8, "Australia", "Turquía", "2026-06-13 23:00", "Grupo D", "Estadio BC Place Vancouver"),
            // Domingo 14
            createM(9, "Alemania", "Curazao", "2026-06-14 12:00", "Grupo E", "Estadio Houston"),
            createM(10, "Países Bajos", "Japón", "2026-06-14 15:00", "Grupo F", "Estadio Dallas"),
            createM(11, "Costa de Marfil", "Ecuador", "2026-06-14 18:00", "Grupo E", "Estadio Filadelfia"),
            createM(12, "Suecia", "Túnez", "2026-06-14 21:00", "Grupo F", "Estadio Monterrey"),
            // Lunes 15
            createM(13, "España", "Cabo Verde", "2026-06-15 11:00", "Grupo H", "Estadio Atlanta"),
            createM(14, "Bélgica", "Egipto", "2026-06-15 14:00", "Grupo G", "Estadio Seattle"),
            createM(15, "Arabia Saudita", "Uruguay", "2026-06-15 17:00", "Grupo H", "Estadio Miami"),
            createM(16, "Irán", "Nueva Zelanda", "2026-06-15 20:00", "Grupo G", "Estadio Los Ángeles"),
            // Martes 16
            createM(17, "Francia", "Senegal", "2026-06-16 14:00", "Grupo I", "Estadio Nueva York Nueva Jersey"),
            createM(18, "Irak", "Noruega", "2026-06-16 17:00", "Grupo I", "Estadio Boston"),
            createM(19, "Argentina", "Argelia", "2026-06-16 20:00", "Grupo J", "Estadio Kansas City"),
            createM(20, "Austria", "Jordania", "2026-06-16 23:00", "Grupo J", "Estadio Bahía de San Francisco"),
            // Miércoles 17
            createM(21, "Portugal", "RD Congo", "2026-06-17 12:00", "Grupo K", "Estadio Houston"),
            createM(22, "Inglaterra", "Croacia", "2026-06-17 15:00", "Grupo L", "Estadio Dallas"),
            createM(23, "Ghana", "Panamá", "2026-06-17 18:00", "Grupo L", "Estadio Toronto"),
            createM(24, "Uzbekistán", "Colombia", "2026-06-17 21:00", "Grupo K", "Estadio Ciudad de México"),
            // Jueves 18
            createM(25, "República Checa", "Sudáfrica", "2026-06-18 11:00", "Grupo A", "Estadio Atlanta"),
            createM(26, "Suiza", "Bosnia y Herzegovina", "2026-06-18 14:00", "Grupo B", "Estadio Los Ángeles"),
            createM(27, "Canadá", "Catar", "2026-06-18 17:00", "Grupo B", "Estadio BC Place Vancouver"),
            createM(28, "México", "Corea del Sur", "2026-06-18 20:00", "Grupo A", "Estadio Guadalajara"),
            // Viernes 19
            createM(29, "Estados Unidos", "Australia", "2026-06-19 14:00", "Grupo D", "Estadio Seattle"),
            createM(30, "Escocia", "Marruecos", "2026-06-19 17:00", "Grupo C", "Estadio Boston"),
            createM(31, "Brasil", "Haití", "2026-06-19 20:00", "Grupo C", "Estadio Filadelfia"),
            createM(32, "Turquía", "Paraguay", "2026-06-19 23:00", "Grupo D", "Estadio Bahía de San Francisco"),
            // Sábado 20
            createM(33, "Países Bajos", "Suecia", "2026-06-20 12:00", "Grupo F", "Estadio Houston"),
            createM(34, "Alemania", "Costa de Marfil", "2026-06-20 15:00", "Grupo E", "Estadio Toronto"),
            createM(35, "Ecuador", "Curazao", "2026-06-20 21:00", "Grupo E", "Estadio Kansas City"),
            createM(36, "Túnez", "Japón", "2026-06-20 23:00", "Grupo F", "Estadio Monterrey"),
            // Domingo 21
            createM(37, "España", "Arabia Saudita", "2026-06-21 11:00", "Grupo H", "Estadio Atlanta"),
            createM(38, "Bélgica", "Irán", "2026-06-21 14:00", "Grupo G", "Estadio Los Ángeles"),
            createM(39, "Uruguay", "Cabo Verde", "2026-06-21 17:00", "Grupo H", "Estadio Miami"),
            createM(40, "Nueva Zelanda", "Egipto", "2026-06-21 20:00", "Grupo G", "Estadio BC Place Vancouver"),
            // Lunes 22
            createM(41, "Argentina", "Austria", "2026-06-22 12:00", "Grupo J", "Estadio Dallas"),
            createM(42, "Francia", "Irak", "2026-06-22 16:00", "Grupo I", "Estadio Filadelfia"),
            createM(43, "Noruega", "Senegal", "2026-06-22 19:00", "Grupo I", "Estadio Nueva York Nueva Jersey"),
            createM(44, "Jordania", "Argelia", "2026-06-22 22:00", "Grupo J", "Estadio Bahía de San Francisco"),
            // Martes 23
            createM(45, "Portugal", "Uzbekistán", "2026-06-23 12:00", "Grupo K", "Estadio Houston"),
            createM(46, "Inglaterra", "Ghana", "2026-06-23 15:00", "Grupo L", "Estadio Boston"),
            createM(47, "Panamá", "Croacia", "2026-06-23 18:00", "Grupo L", "Estadio Toronto"),
            createM(48, "Colombia", "RD Congo", "2026-06-23 21:00", "Grupo K", "Estadio Guadalajara"),
            // Miércoles 24
            createM(49, "Suiza", "Canadá", "2026-06-24 14:00", "Grupo B", "Estadio BC Place Vancouver"),
            createM(50, "Bosnia y Herzegovina", "Catar", "2026-06-24 14:00", "Grupo B", "Estadio Seattle"),
            createM(51, "Escocia", "Brasil", "2026-06-24 17:00", "Grupo C", "Estadio Miami"),
            createM(52, "Marruecos", "Haití", "2026-06-24 17:00", "Grupo C", "Estadio Atlanta"),
            createM(53, "República Checa", "México", "2026-06-24 20:00", "Grupo A", "Estadio Ciudad de México"),
            createM(54, "Sudáfrica", "Corea del Sur", "2026-06-24 20:00", "Grupo A", "Estadio Monterrey"),
            // Jueves 25
            createM(55, "Curazao", "Costa de Marfil", "2026-06-25 15:00", "Grupo E", "Estadio Filadelfia"),
            createM(56, "Ecuador", "Alemania", "2026-06-25 15:00", "Grupo E", "Estadio Nueva York Nueva Jersey"),
            createM(57, "Japón", "Suecia", "2026-06-25 18:00", "Grupo F", "Estadio Dallas"),
            createM(58, "Túnez", "Países Bajos", "2026-06-25 18:00", "Grupo F", "Estadio Kansas City"),
            createM(59, "Turquía", "Estados Unidos", "2026-06-25 21:00", "Grupo D", "Estadio Los Ángeles"),
            createM(60, "Paraguay", "Australia", "2026-06-25 21:00", "Grupo D", "Estadio Bahía de San Francisco"),
            // Viernes 26
            createM(61, "Noruega", "Francia", "2026-06-26 14:00", "Grupo I", "Estadio Boston"),
            createM(62, "Senegal", "Irak", "2026-06-26 14:00", "Grupo I", "Estadio Toronto"),
            createM(63, "Cabo Verde", "Arabia Saudita", "2026-06-26 19:00", "Grupo H", "Estadio Houston"),
            createM(64, "Uruguay", "España", "2026-06-26 19:00", "Grupo H", "Estadio Guadalajara"),
            createM(65, "Egipto", "Irán", "2026-06-26 22:00", "Grupo G", "Estadio Seattle"),
            createM(66, "Nueva Zelanda", "Bélgica", "2026-06-26 22:00", "Grupo G", "Estadio BC Place Vancouver"),
            // Sábado 27
            createM(67, "Panamá", "Inglaterra", "2026-06-27 16:00", "Grupo L", "Estadio Nueva York Nueva Jersey"),
            createM(68, "Croacia", "Ghana", "2026-06-27 16:00", "Grupo L", "Estadio Filadelfia"),
            createM(69, "Colombia", "Portugal", "2026-06-27 18:30", "Grupo K", "Estadio Miami"),
            createM(70, "RD Congo", "Uzbekistán", "2026-06-27 18:30", "Grupo K", "Estadio Atlanta"),
            createM(71, "Argelia", "Austria", "2026-06-27 21:00", "Grupo J", "Estadio Kansas City"),
            createM(72, "Jordania", "Argentina", "2026-06-27 21:00", "Grupo J", "Estadio Dallas")
        )

        repo.insertMatches(matches)

        // 3. Pre-populate empty official specials
        repo.insertOfficialSpecials(
            OfficialSpecialsEntity(
                id = 1,
                champion = "",
                subchampion = "",
                thirdPlace = "",
                goleador = "",
                asistente = "",
                mvp = "",
                portero = "",
                vallaMenosVencida = ""
            )
        )
    }
}
