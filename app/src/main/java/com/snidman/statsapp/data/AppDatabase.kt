package com.snidman.statsapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PlayerEntity::class,
        TeamEntity::class,
        MatchEntity::class,
        StatEventEntity::class,
        SetLineupEntity::class,
        SetPlayerRoleEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun teamDao(): TeamDao
    abstract fun matchDao(): MatchDao
    abstract fun statEventDao(): StatEventDao
    abstract fun setLineupDao(): SetLineupDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS teams (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE players ADD COLUMN teamId INTEGER")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE matches ADD COLUMN opponentTeamName TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS set_lineups (
                        matchId INTEGER NOT NULL,
                        setNumber INTEGER NOT NULL,
                        position INTEGER NOT NULL,
                        frontPlayerId INTEGER,
                        backPlayerId INTEGER,
                        servingPlayerId INTEGER,
                        PRIMARY KEY(matchId, setNumber, position),
                        FOREIGN KEY(matchId) REFERENCES matches(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_set_lineups_matchId ON set_lineups(matchId)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS set_player_roles (
                        matchId INTEGER NOT NULL,
                        setNumber INTEGER NOT NULL,
                        playerId INTEGER NOT NULL,
                        isLibero INTEGER NOT NULL,
                        isSetter INTEGER NOT NULL,
                        PRIMARY KEY(matchId, setNumber, playerId),
                        FOREIGN KEY(matchId) REFERENCES matches(id) ON DELETE CASCADE,
                        FOREIGN KEY(playerId) REFERENCES players(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_set_player_roles_matchId ON set_player_roles(matchId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_set_player_roles_playerId ON set_player_roles(playerId)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE matches ADD COLUMN teamId INTEGER")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stats_app.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
