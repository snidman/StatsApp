package com.snidman.statsapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val jerseyNumber: Int,
    val teamId: Long? = null
)

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val opponentTeamName: String,
    val createdAt: Long
)

@Entity(
    tableName = "stat_events",
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playerId"), Index("matchId")]
)
data class StatEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerId: Long,
    val matchId: Long,
    val setNumber: Int,
    val skill: String,
    val outcome: String,
    val createdAt: Long
)

@Entity(
    tableName = "set_lineups",
    primaryKeys = ["matchId", "setNumber", "position"],
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("matchId")]
)
data class SetLineupEntity(
    val matchId: Long,
    val setNumber: Int,
    val position: Int,
    val frontPlayerId: Long? = null,
    val backPlayerId: Long? = null,
    val servingPlayerId: Long? = null
)

@Entity(
    tableName = "set_player_roles",
    primaryKeys = ["matchId", "setNumber", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("matchId"), Index("playerId")]
)
data class SetPlayerRoleEntity(
    val matchId: Long,
    val setNumber: Int,
    val playerId: Long,
    val isLibero: Boolean,
    val isSetter: Boolean
)
