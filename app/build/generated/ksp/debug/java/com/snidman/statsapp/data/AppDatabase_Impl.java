package com.snidman.statsapp.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile PlayerDao _playerDao;

  private volatile TeamDao _teamDao;

  private volatile MatchDao _matchDao;

  private volatile StatEventDao _statEventDao;

  private volatile SetLineupDao _setLineupDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `players` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `jerseyNumber` INTEGER NOT NULL, `teamId` INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `teams` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `matches` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `opponentTeamName` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `stat_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `playerId` INTEGER NOT NULL, `matchId` INTEGER NOT NULL, `setNumber` INTEGER NOT NULL, `skill` TEXT NOT NULL, `outcome` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`playerId`) REFERENCES `players`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`matchId`) REFERENCES `matches`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stat_events_playerId` ON `stat_events` (`playerId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stat_events_matchId` ON `stat_events` (`matchId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `set_lineups` (`matchId` INTEGER NOT NULL, `setNumber` INTEGER NOT NULL, `position` INTEGER NOT NULL, `frontPlayerId` INTEGER, `backPlayerId` INTEGER, `servingPlayerId` INTEGER, PRIMARY KEY(`matchId`, `setNumber`, `position`), FOREIGN KEY(`matchId`) REFERENCES `matches`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_set_lineups_matchId` ON `set_lineups` (`matchId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `set_player_roles` (`matchId` INTEGER NOT NULL, `setNumber` INTEGER NOT NULL, `playerId` INTEGER NOT NULL, `isLibero` INTEGER NOT NULL, `isSetter` INTEGER NOT NULL, PRIMARY KEY(`matchId`, `setNumber`, `playerId`), FOREIGN KEY(`matchId`) REFERENCES `matches`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`playerId`) REFERENCES `players`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_set_player_roles_matchId` ON `set_player_roles` (`matchId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_set_player_roles_playerId` ON `set_player_roles` (`playerId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '27fa8ea805c58acb4f3f17805ab53ed7')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `players`");
        db.execSQL("DROP TABLE IF EXISTS `teams`");
        db.execSQL("DROP TABLE IF EXISTS `matches`");
        db.execSQL("DROP TABLE IF EXISTS `stat_events`");
        db.execSQL("DROP TABLE IF EXISTS `set_lineups`");
        db.execSQL("DROP TABLE IF EXISTS `set_player_roles`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsPlayers = new HashMap<String, TableInfo.Column>(4);
        _columnsPlayers.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayers.put("jerseyNumber", new TableInfo.Column("jerseyNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayers.put("teamId", new TableInfo.Column("teamId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlayers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlayers = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlayers = new TableInfo("players", _columnsPlayers, _foreignKeysPlayers, _indicesPlayers);
        final TableInfo _existingPlayers = TableInfo.read(db, "players");
        if (!_infoPlayers.equals(_existingPlayers)) {
          return new RoomOpenHelper.ValidationResult(false, "players(com.snidman.statsapp.data.PlayerEntity).\n"
                  + " Expected:\n" + _infoPlayers + "\n"
                  + " Found:\n" + _existingPlayers);
        }
        final HashMap<String, TableInfo.Column> _columnsTeams = new HashMap<String, TableInfo.Column>(2);
        _columnsTeams.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTeams.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTeams = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTeams = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTeams = new TableInfo("teams", _columnsTeams, _foreignKeysTeams, _indicesTeams);
        final TableInfo _existingTeams = TableInfo.read(db, "teams");
        if (!_infoTeams.equals(_existingTeams)) {
          return new RoomOpenHelper.ValidationResult(false, "teams(com.snidman.statsapp.data.TeamEntity).\n"
                  + " Expected:\n" + _infoTeams + "\n"
                  + " Found:\n" + _existingTeams);
        }
        final HashMap<String, TableInfo.Column> _columnsMatches = new HashMap<String, TableInfo.Column>(4);
        _columnsMatches.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatches.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatches.put("opponentTeamName", new TableInfo.Column("opponentTeamName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatches.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMatches = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMatches = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMatches = new TableInfo("matches", _columnsMatches, _foreignKeysMatches, _indicesMatches);
        final TableInfo _existingMatches = TableInfo.read(db, "matches");
        if (!_infoMatches.equals(_existingMatches)) {
          return new RoomOpenHelper.ValidationResult(false, "matches(com.snidman.statsapp.data.MatchEntity).\n"
                  + " Expected:\n" + _infoMatches + "\n"
                  + " Found:\n" + _existingMatches);
        }
        final HashMap<String, TableInfo.Column> _columnsStatEvents = new HashMap<String, TableInfo.Column>(7);
        _columnsStatEvents.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStatEvents.put("playerId", new TableInfo.Column("playerId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStatEvents.put("matchId", new TableInfo.Column("matchId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStatEvents.put("setNumber", new TableInfo.Column("setNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStatEvents.put("skill", new TableInfo.Column("skill", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStatEvents.put("outcome", new TableInfo.Column("outcome", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStatEvents.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStatEvents = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysStatEvents.add(new TableInfo.ForeignKey("players", "CASCADE", "NO ACTION", Arrays.asList("playerId"), Arrays.asList("id")));
        _foreignKeysStatEvents.add(new TableInfo.ForeignKey("matches", "CASCADE", "NO ACTION", Arrays.asList("matchId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesStatEvents = new HashSet<TableInfo.Index>(2);
        _indicesStatEvents.add(new TableInfo.Index("index_stat_events_playerId", false, Arrays.asList("playerId"), Arrays.asList("ASC")));
        _indicesStatEvents.add(new TableInfo.Index("index_stat_events_matchId", false, Arrays.asList("matchId"), Arrays.asList("ASC")));
        final TableInfo _infoStatEvents = new TableInfo("stat_events", _columnsStatEvents, _foreignKeysStatEvents, _indicesStatEvents);
        final TableInfo _existingStatEvents = TableInfo.read(db, "stat_events");
        if (!_infoStatEvents.equals(_existingStatEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "stat_events(com.snidman.statsapp.data.StatEventEntity).\n"
                  + " Expected:\n" + _infoStatEvents + "\n"
                  + " Found:\n" + _existingStatEvents);
        }
        final HashMap<String, TableInfo.Column> _columnsSetLineups = new HashMap<String, TableInfo.Column>(6);
        _columnsSetLineups.put("matchId", new TableInfo.Column("matchId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetLineups.put("setNumber", new TableInfo.Column("setNumber", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetLineups.put("position", new TableInfo.Column("position", "INTEGER", true, 3, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetLineups.put("frontPlayerId", new TableInfo.Column("frontPlayerId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetLineups.put("backPlayerId", new TableInfo.Column("backPlayerId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetLineups.put("servingPlayerId", new TableInfo.Column("servingPlayerId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSetLineups = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysSetLineups.add(new TableInfo.ForeignKey("matches", "CASCADE", "NO ACTION", Arrays.asList("matchId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesSetLineups = new HashSet<TableInfo.Index>(1);
        _indicesSetLineups.add(new TableInfo.Index("index_set_lineups_matchId", false, Arrays.asList("matchId"), Arrays.asList("ASC")));
        final TableInfo _infoSetLineups = new TableInfo("set_lineups", _columnsSetLineups, _foreignKeysSetLineups, _indicesSetLineups);
        final TableInfo _existingSetLineups = TableInfo.read(db, "set_lineups");
        if (!_infoSetLineups.equals(_existingSetLineups)) {
          return new RoomOpenHelper.ValidationResult(false, "set_lineups(com.snidman.statsapp.data.SetLineupEntity).\n"
                  + " Expected:\n" + _infoSetLineups + "\n"
                  + " Found:\n" + _existingSetLineups);
        }
        final HashMap<String, TableInfo.Column> _columnsSetPlayerRoles = new HashMap<String, TableInfo.Column>(5);
        _columnsSetPlayerRoles.put("matchId", new TableInfo.Column("matchId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetPlayerRoles.put("setNumber", new TableInfo.Column("setNumber", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetPlayerRoles.put("playerId", new TableInfo.Column("playerId", "INTEGER", true, 3, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetPlayerRoles.put("isLibero", new TableInfo.Column("isLibero", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSetPlayerRoles.put("isSetter", new TableInfo.Column("isSetter", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSetPlayerRoles = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysSetPlayerRoles.add(new TableInfo.ForeignKey("matches", "CASCADE", "NO ACTION", Arrays.asList("matchId"), Arrays.asList("id")));
        _foreignKeysSetPlayerRoles.add(new TableInfo.ForeignKey("players", "CASCADE", "NO ACTION", Arrays.asList("playerId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesSetPlayerRoles = new HashSet<TableInfo.Index>(2);
        _indicesSetPlayerRoles.add(new TableInfo.Index("index_set_player_roles_matchId", false, Arrays.asList("matchId"), Arrays.asList("ASC")));
        _indicesSetPlayerRoles.add(new TableInfo.Index("index_set_player_roles_playerId", false, Arrays.asList("playerId"), Arrays.asList("ASC")));
        final TableInfo _infoSetPlayerRoles = new TableInfo("set_player_roles", _columnsSetPlayerRoles, _foreignKeysSetPlayerRoles, _indicesSetPlayerRoles);
        final TableInfo _existingSetPlayerRoles = TableInfo.read(db, "set_player_roles");
        if (!_infoSetPlayerRoles.equals(_existingSetPlayerRoles)) {
          return new RoomOpenHelper.ValidationResult(false, "set_player_roles(com.snidman.statsapp.data.SetPlayerRoleEntity).\n"
                  + " Expected:\n" + _infoSetPlayerRoles + "\n"
                  + " Found:\n" + _existingSetPlayerRoles);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "27fa8ea805c58acb4f3f17805ab53ed7", "25fb42f2401d17a31c5af8af10869521");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "players","teams","matches","stat_events","set_lineups","set_player_roles");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `players`");
      _db.execSQL("DELETE FROM `teams`");
      _db.execSQL("DELETE FROM `matches`");
      _db.execSQL("DELETE FROM `stat_events`");
      _db.execSQL("DELETE FROM `set_lineups`");
      _db.execSQL("DELETE FROM `set_player_roles`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(PlayerDao.class, PlayerDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TeamDao.class, TeamDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MatchDao.class, MatchDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StatEventDao.class, StatEventDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SetLineupDao.class, SetLineupDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public PlayerDao playerDao() {
    if (_playerDao != null) {
      return _playerDao;
    } else {
      synchronized(this) {
        if(_playerDao == null) {
          _playerDao = new PlayerDao_Impl(this);
        }
        return _playerDao;
      }
    }
  }

  @Override
  public TeamDao teamDao() {
    if (_teamDao != null) {
      return _teamDao;
    } else {
      synchronized(this) {
        if(_teamDao == null) {
          _teamDao = new TeamDao_Impl(this);
        }
        return _teamDao;
      }
    }
  }

  @Override
  public MatchDao matchDao() {
    if (_matchDao != null) {
      return _matchDao;
    } else {
      synchronized(this) {
        if(_matchDao == null) {
          _matchDao = new MatchDao_Impl(this);
        }
        return _matchDao;
      }
    }
  }

  @Override
  public StatEventDao statEventDao() {
    if (_statEventDao != null) {
      return _statEventDao;
    } else {
      synchronized(this) {
        if(_statEventDao == null) {
          _statEventDao = new StatEventDao_Impl(this);
        }
        return _statEventDao;
      }
    }
  }

  @Override
  public SetLineupDao setLineupDao() {
    if (_setLineupDao != null) {
      return _setLineupDao;
    } else {
      synchronized(this) {
        if(_setLineupDao == null) {
          _setLineupDao = new SetLineupDao_Impl(this);
        }
        return _setLineupDao;
      }
    }
  }
}
