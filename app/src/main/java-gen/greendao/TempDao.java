package greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import greendao.Temp;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "TEMP".
*/
public class TempDao extends AbstractDao<Temp, Long> {

    public static final String TABLENAME = "TEMP";

    /**
     * Properties of entity Temp.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Hz = new Property(1, Double.class, "hz", false, "HZ");
        public final static Property MinPow = new Property(2, Double.class, "minPow", false, "MIN_POW");
        public final static Property MaxPow = new Property(3, Double.class, "maxPow", false, "MAX_POW");
    };


    public TempDao(DaoConfig config) {
        super(config);
    }
    
    public TempDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"TEMP\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"HZ\" REAL," + // 1: hz
                "\"MIN_POW\" REAL," + // 2: minPow
                "\"MAX_POW\" REAL);"); // 3: maxPow
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"TEMP\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Temp entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Double hz = entity.getHz();
        if (hz != null) {
            stmt.bindDouble(2, hz);
        }
 
        Double minPow = entity.getMinPow();
        if (minPow != null) {
            stmt.bindDouble(3, minPow);
        }
 
        Double maxPow = entity.getMaxPow();
        if (maxPow != null) {
            stmt.bindDouble(4, maxPow);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Temp readEntity(Cursor cursor, int offset) {
        Temp entity = new Temp( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getDouble(offset + 1), // hz
            cursor.isNull(offset + 2) ? null : cursor.getDouble(offset + 2), // minPow
            cursor.isNull(offset + 3) ? null : cursor.getDouble(offset + 3) // maxPow
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Temp entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setHz(cursor.isNull(offset + 1) ? null : cursor.getDouble(offset + 1));
        entity.setMinPow(cursor.isNull(offset + 2) ? null : cursor.getDouble(offset + 2));
        entity.setMaxPow(cursor.isNull(offset + 3) ? null : cursor.getDouble(offset + 3));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Temp entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Temp entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
