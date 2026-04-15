package com.snidman.statsapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY jerseyNumber ASC, name ASC")
    fun getPlayersFlow(): Flow<List<PlayerEntity>>

    @Query("SELECT COUNT(*) FROM players")
    suspend fun playerCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Query("UPDATE players SET name = :name, jerseyNumber = :jerseyNumber WHERE id = :playerId")
    suspend fun updatePlayer(playerId: Long, name: String, jerseyNumber: Int)

    @Query("UPDATE players SET teamId = NULL WHERE teamId = :teamId")
    suspend fun clearTeamMembers(teamId: Long)

    @Query("UPDATE players SET teamId = NULL WHERE id IN (:playerIds)")
    suspend fun clearTeamForPlayers(playerIds: List<Long>)

    @Query("UPDATE players SET teamId = :teamId WHERE id IN (:playerIds)")
    suspend fun assignTeamForPlayers(playerIds: List<Long>, teamId: Long)

    @Query("DELETE FROM players WHERE id = :playerId")
    suspend fun deletePlayer(playerId: Long)
}

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun getTeamsFlow(): Flow<List<TeamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity): Long

    @Query("UPDATE teams SET name = :name WHERE id = :teamId")
    suspend fun updateTeam(teamId: Long, name: String)

    @Query("DELETE FROM teams WHERE id = :teamId")
    suspend fun deleteTeam(teamId: Long)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY createdAt DESC")
    fun getMatchesFlow(): Flow<List<MatchEntity>>

    @Query("SELECT COUNT(*) FROM matches")
    suspend fun matchCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity): Long

    @Query("UPDATE matches SET name = :name, opponentTeamName = :opponentTeamName WHERE id = :matchId")
    suspend fun updateMatch(matchId: Long, name: String, opponentTeamName: String)

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatch(matchId: Long)
}

@Dao
interface StatEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: StatEventEntity): Long

    @Query(
        """
        SELECT * FROM stat_events
        WHERE matchId = :matchId
          AND (:setNumber IS NULL OR setNumber = :setNumber)
        ORDER BY createdAt DESC
        """
    )
    fun getEventsFlow(matchId: Long, setNumber: Int?): Flow<List<StatEventEntity>>

    @Query("SELECT COUNT(*) FROM stat_events WHERE matchId = :matchId")
    fun getEventCountForMatchFlow(matchId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM stat_events WHERE matchId = :matchId AND setNumber = :setNumber")
    fun getEventCountForMatchSetFlow(matchId: Long, setNumber: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM stat_events WHERE playerId = :playerId")
    fun getEventCountForPlayerFlow(playerId: Long): Flow<Int>

    @Query("DELETE FROM stat_events WHERE matchId = :matchId AND setNumber = :setNumber")
    suspend fun deleteSetEvents(matchId: Long, setNumber: Int)
}

@Dao
interface SetLineupDao {
    @Query(
        """
        SELECT * FROM set_lineups
        WHERE matchId = :matchId AND setNumber = :setNumber
        ORDER BY position ASC
        """
    )
    fun getSetLineupsFlow(matchId: Long, setNumber: Int): Flow<List<SetLineupEntity>>

    @Query(
        """
        SELECT * FROM set_player_roles
        WHERE matchId = :matchId AND setNumber = :setNumber
        ORDER BY playerId ASC
        """
    )
    fun getSetPlayerRolesFlow(matchId: Long, setNumber: Int): Flow<List<SetPlayerRoleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSetLineups(lineups: List<SetLineupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSetPlayerRoles(roles: List<SetPlayerRoleEntity>)

    @Query("DELETE FROM set_lineups WHERE matchId = :matchId AND setNumber = :setNumber")
    suspend fun deleteSetLineups(matchId: Long, setNumber: Int)

    @Query("DELETE FROM set_player_roles WHERE matchId = :matchId AND setNumber = :setNumber")
    suspend fun deleteSetPlayerRoles(matchId: Long, setNumber: Int)
}
