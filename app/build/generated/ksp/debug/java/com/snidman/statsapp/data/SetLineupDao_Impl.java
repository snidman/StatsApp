package com.snidman.statsapp.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SetLineupDao_Impl implements SetLineupDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SetLineupEntity> __insertionAdapterOfSetLineupEntity;

  private final EntityInsertionAdapter<SetPlayerRoleEntity> __insertionAdapterOfSetPlayerRoleEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSetLineups;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSetPlayerRoles;

  public SetLineupDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSetLineupEntity = new EntityInsertionAdapter<SetLineupEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `set_lineups` (`matchId`,`setNumber`,`position`,`frontPlayerId`,`backPlayerId`,`servingPlayerId`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SetLineupEntity entity) {
        statement.bindLong(1, entity.getMatchId());
        statement.bindLong(2, entity.getSetNumber());
        statement.bindLong(3, entity.getPosition());
        if (entity.getFrontPlayerId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getFrontPlayerId());
        }
        if (entity.getBackPlayerId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getBackPlayerId());
        }
        if (entity.getServingPlayerId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getServingPlayerId());
        }
      }
    };
    this.__insertionAdapterOfSetPlayerRoleEntity = new EntityInsertionAdapter<SetPlayerRoleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `set_player_roles` (`matchId`,`setNumber`,`playerId`,`isLibero`,`isSetter`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SetPlayerRoleEntity entity) {
        statement.bindLong(1, entity.getMatchId());
        statement.bindLong(2, entity.getSetNumber());
        statement.bindLong(3, entity.getPlayerId());
        final int _tmp = entity.isLibero() ? 1 : 0;
        statement.bindLong(4, _tmp);
        final int _tmp_1 = entity.isSetter() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
      }
    };
    this.__preparedStmtOfDeleteSetLineups = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM set_lineups WHERE matchId = ? AND setNumber = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSetPlayerRoles = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM set_player_roles WHERE matchId = ? AND setNumber = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsertSetLineups(final List<SetLineupEntity> lineups,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSetLineupEntity.insert(lineups);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertSetPlayerRoles(final List<SetPlayerRoleEntity> roles,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSetPlayerRoleEntity.insert(roles);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSetLineups(final long matchId, final int setNumber,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSetLineups.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, matchId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, setNumber);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSetLineups.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSetPlayerRoles(final long matchId, final int setNumber,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSetPlayerRoles.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, matchId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, setNumber);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSetPlayerRoles.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SetLineupEntity>> getSetLineupsFlow(final long matchId, final int setNumber) {
    final String _sql = "\n"
            + "        SELECT * FROM set_lineups\n"
            + "        WHERE matchId = ? AND setNumber = ?\n"
            + "        ORDER BY position ASC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, matchId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, setNumber);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"set_lineups"}, new Callable<List<SetLineupEntity>>() {
      @Override
      @NonNull
      public List<SetLineupEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "matchId");
          final int _cursorIndexOfSetNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "setNumber");
          final int _cursorIndexOfPosition = CursorUtil.getColumnIndexOrThrow(_cursor, "position");
          final int _cursorIndexOfFrontPlayerId = CursorUtil.getColumnIndexOrThrow(_cursor, "frontPlayerId");
          final int _cursorIndexOfBackPlayerId = CursorUtil.getColumnIndexOrThrow(_cursor, "backPlayerId");
          final int _cursorIndexOfServingPlayerId = CursorUtil.getColumnIndexOrThrow(_cursor, "servingPlayerId");
          final List<SetLineupEntity> _result = new ArrayList<SetLineupEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SetLineupEntity _item;
            final long _tmpMatchId;
            _tmpMatchId = _cursor.getLong(_cursorIndexOfMatchId);
            final int _tmpSetNumber;
            _tmpSetNumber = _cursor.getInt(_cursorIndexOfSetNumber);
            final int _tmpPosition;
            _tmpPosition = _cursor.getInt(_cursorIndexOfPosition);
            final Long _tmpFrontPlayerId;
            if (_cursor.isNull(_cursorIndexOfFrontPlayerId)) {
              _tmpFrontPlayerId = null;
            } else {
              _tmpFrontPlayerId = _cursor.getLong(_cursorIndexOfFrontPlayerId);
            }
            final Long _tmpBackPlayerId;
            if (_cursor.isNull(_cursorIndexOfBackPlayerId)) {
              _tmpBackPlayerId = null;
            } else {
              _tmpBackPlayerId = _cursor.getLong(_cursorIndexOfBackPlayerId);
            }
            final Long _tmpServingPlayerId;
            if (_cursor.isNull(_cursorIndexOfServingPlayerId)) {
              _tmpServingPlayerId = null;
            } else {
              _tmpServingPlayerId = _cursor.getLong(_cursorIndexOfServingPlayerId);
            }
            _item = new SetLineupEntity(_tmpMatchId,_tmpSetNumber,_tmpPosition,_tmpFrontPlayerId,_tmpBackPlayerId,_tmpServingPlayerId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SetPlayerRoleEntity>> getSetPlayerRolesFlow(final long matchId,
      final int setNumber) {
    final String _sql = "\n"
            + "        SELECT * FROM set_player_roles\n"
            + "        WHERE matchId = ? AND setNumber = ?\n"
            + "        ORDER BY playerId ASC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, matchId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, setNumber);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"set_player_roles"}, new Callable<List<SetPlayerRoleEntity>>() {
      @Override
      @NonNull
      public List<SetPlayerRoleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "matchId");
          final int _cursorIndexOfSetNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "setNumber");
          final int _cursorIndexOfPlayerId = CursorUtil.getColumnIndexOrThrow(_cursor, "playerId");
          final int _cursorIndexOfIsLibero = CursorUtil.getColumnIndexOrThrow(_cursor, "isLibero");
          final int _cursorIndexOfIsSetter = CursorUtil.getColumnIndexOrThrow(_cursor, "isSetter");
          final List<SetPlayerRoleEntity> _result = new ArrayList<SetPlayerRoleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SetPlayerRoleEntity _item;
            final long _tmpMatchId;
            _tmpMatchId = _cursor.getLong(_cursorIndexOfMatchId);
            final int _tmpSetNumber;
            _tmpSetNumber = _cursor.getInt(_cursorIndexOfSetNumber);
            final long _tmpPlayerId;
            _tmpPlayerId = _cursor.getLong(_cursorIndexOfPlayerId);
            final boolean _tmpIsLibero;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsLibero);
            _tmpIsLibero = _tmp != 0;
            final boolean _tmpIsSetter;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsSetter);
            _tmpIsSetter = _tmp_1 != 0;
            _item = new SetPlayerRoleEntity(_tmpMatchId,_tmpSetNumber,_tmpPlayerId,_tmpIsLibero,_tmpIsSetter);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
