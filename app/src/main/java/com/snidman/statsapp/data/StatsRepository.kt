package com.snidman.statsapp.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class StatsRepository(private val db: AppDatabase) {
    val playersFlow: Flow<List<PlayerEntity>> = db.playerDao().getPlayersFlow()
    val teamsFlow: Flow<List<TeamEntity>> = db.teamDao().getTeamsFlow()
    val matchesFlow: Flow<List<MatchEntity>> = db.matchDao().getMatchesFlow()

    fun getEventsFlow(matchId: Long, setNumber: Int?): Flow<List<StatEventEntity>> {
        return db.statEventDao().getEventsFlow(matchId, setNumber)
    }

    fun getEventCountForMatchFlow(matchId: Long): Flow<Int> {
        return db.statEventDao().getEventCountForMatchFlow(matchId)
    }

    fun getEventCountForMatchSetFlow(matchId: Long, setNumber: Int): Flow<Int> {
        return db.statEventDao().getEventCountForMatchSetFlow(matchId, setNumber)
    }

    fun getEventCountForPlayerFlow(playerId: Long): Flow<Int> {
        return db.statEventDao().getEventCountForPlayerFlow(playerId)
    }

    fun getSetLineupsFlow(matchId: Long, setNumber: Int): Flow<List<SetLineupEntity>> {
        return db.setLineupDao().getSetLineupsFlow(matchId, setNumber)
    }

    fun getSetPlayerRolesFlow(matchId: Long, setNumber: Int): Flow<List<SetPlayerRoleEntity>> {
        return db.setLineupDao().getSetPlayerRolesFlow(matchId, setNumber)
    }

    suspend fun insertEvent(event: StatEventEntity) {
        db.statEventDao().insertEvent(event)
    }

    suspend fun addPlayer(name: String, jerseyNumber: Int): Long {
        return db.playerDao().insertPlayer(PlayerEntity(name = name, jerseyNumber = jerseyNumber))
    }

    suspend fun updatePlayer(playerId: Long, name: String, jerseyNumber: Int) {
        db.playerDao().updatePlayer(playerId, name, jerseyNumber)
    }

    suspend fun deletePlayer(playerId: Long) {
        db.playerDao().deletePlayer(playerId)
    }

    suspend fun addTeam(name: String, playerIds: List<Long>): Long {
        return db.withTransaction {
            val teamId = db.teamDao().insertTeam(TeamEntity(name = name.trim()))
            replaceTeamMembers(teamId, playerIds)
            teamId
        }
    }

    suspend fun updateTeam(teamId: Long, name: String, playerIds: List<Long>) {
        db.withTransaction {
            db.teamDao().updateTeam(teamId, name.trim())
            replaceTeamMembers(teamId, playerIds)
        }
    }

    suspend fun deleteTeam(teamId: Long) {
        db.withTransaction {
            db.playerDao().clearTeamMembers(teamId)
            db.teamDao().deleteTeam(teamId)
        }
    }

    suspend fun addMatch(name: String, teamId: Long?, opponentTeamName: String): Long {
        return db.matchDao().insertMatch(
            MatchEntity(
                name = name,
                teamId = teamId,
                opponentTeamName = opponentTeamName,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateMatch(matchId: Long, name: String, teamId: Long?, opponentTeamName: String) {
        db.matchDao().updateMatch(matchId, name, teamId, opponentTeamName)
    }

    suspend fun deleteMatch(matchId: Long) {
        db.matchDao().deleteMatch(matchId)
    }

    suspend fun deleteSetEvents(matchId: Long, setNumber: Int) {
        db.statEventDao().deleteSetEvents(matchId, setNumber)
    }

    suspend fun saveSetLineup(
        matchId: Long,
        setNumber: Int,
        lineups: List<SetLineupEntity>,
        roles: List<SetPlayerRoleEntity>
    ) {
        db.withTransaction {
            db.setLineupDao().deleteSetLineups(matchId, setNumber)
            db.setLineupDao().deleteSetPlayerRoles(matchId, setNumber)

            if (lineups.isNotEmpty()) {
                db.setLineupDao().upsertSetLineups(lineups)
            }
            if (roles.isNotEmpty()) {
                db.setLineupDao().upsertSetPlayerRoles(roles)
            }
        }
    }

    suspend fun ensureSeedData() {
        if (db.playerDao().playerCount() == 0) {
            db.playerDao().insertPlayer(PlayerEntity(name = "Player 1", jerseyNumber = 1))
            db.playerDao().insertPlayer(PlayerEntity(name = "Player 2", jerseyNumber = 2))
            db.playerDao().insertPlayer(PlayerEntity(name = "Player 3", jerseyNumber = 3))
            db.playerDao().insertPlayer(PlayerEntity(name = "Player 4", jerseyNumber = 4))
            db.playerDao().insertPlayer(PlayerEntity(name = "Player 5", jerseyNumber = 5))
            db.playerDao().insertPlayer(PlayerEntity(name = "Player 6", jerseyNumber = 6))
        }

        if (db.matchDao().matchCount() == 0) {
            db.matchDao().insertMatch(
                MatchEntity(name = "Match 1", teamId = null, opponentTeamName = "", createdAt = System.currentTimeMillis())
            )
        }
    }

    private suspend fun replaceTeamMembers(teamId: Long, playerIds: List<Long>) {
        db.playerDao().clearTeamMembers(teamId)
        if (playerIds.isNotEmpty()) {
            db.playerDao().clearTeamForPlayers(playerIds)
            db.playerDao().assignTeamForPlayers(playerIds, teamId)
        }
    }
}
