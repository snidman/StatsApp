package com.snidman.statsapp.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
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
public final class PlayerDao_Impl implements PlayerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlayerEntity> __insertionAdapterOfPlayerEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePlayer;

  private final SharedSQLiteStatement __preparedStmtOfClearTeamMembers;

  private final SharedSQLiteStatement __preparedStmtOfDeletePlayer;

  public PlayerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlayerEntity = new EntityInsertionAdapter<PlayerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `players` (`id`,`name`,`jerseyNumber`,`teamId`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlayerEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getJerseyNumber());
        if (entity.getTeamId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getTeamId());
        }
      }
    };
    this.__preparedStmtOfUpdatePlayer = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE players SET name = ?, jerseyNumber = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearTeamMembers = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE players SET teamId = NULL WHERE teamId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeletePlayer = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM players WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertPlayer(final PlayerEntity player,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPlayerEntity.insertAndReturnId(player);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePlayer(final long playerId, final String name, final int jerseyNumber,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePlayer.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, name);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, jerseyNumber);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, playerId);
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
          __preparedStmtOfUpdatePlayer.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearTeamMembers(final long teamId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearTeamMembers.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, teamId);
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
          __preparedStmtOfClearTeamMembers.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePlayer(final long playerId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePlayer.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, playerId);
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
          __preparedStmtOfDeletePlayer.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PlayerEntity>> getPlayersFlow() {
    final String _sql = "SELECT * FROM players ORDER BY jerseyNumber ASC, name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"players"}, new Callable<List<PlayerEntity>>() {
      @Override
      @NonNull
      public List<PlayerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfJerseyNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "jerseyNumber");
          final int _cursorIndexOfTeamId = CursorUtil.getColumnIndexOrThrow(_cursor, "teamId");
          final List<PlayerEntity> _result = new ArrayList<PlayerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlayerEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpJerseyNumber;
            _tmpJerseyNumber = _cursor.getInt(_cursorIndexOfJerseyNumber);
            final Long _tmpTeamId;
            if (_cursor.isNull(_cursorIndexOfTeamId)) {
              _tmpTeamId = null;
            } else {
              _tmpTeamId = _cursor.getLong(_cursorIndexOfTeamId);
            }
            _item = new PlayerEntity(_tmpId,_tmpName,_tmpJerseyNumber,_tmpTeamId);
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
  public Object playerCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM players";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearTeamForPlayers(final List<Long> playerIds,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE players SET teamId = NULL WHERE id IN (");
        final int _inputSize = playerIds.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (long _item : playerIds) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object assignTeamForPlayers(final List<Long> playerIds, final long teamId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE players SET teamId = ");
        _stringBuilder.append("?");
        _stringBuilder.append(" WHERE id IN (");
        final int _inputSize = playerIds.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, teamId);
        _argIndex = 2;
        for (long _item : playerIds) {
          _stmt.bindLong(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
