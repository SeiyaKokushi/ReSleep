package greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import greendao.Recommend;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "RECOMMEND".
*/
public class RecommendDao extends AbstractDao<Recommend, Long> {

    public static final String TABLENAME = "RECOMMEND";

    /**
     * Properties of entity Recommend.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Text = new Property(1, String.class, "text", false, "TEXT");
        public final static Property Evaluation = new Property(2, Boolean.class, "evaluation", false, "EVALUATION");
        public final static Property Date = new Property(3, Long.class, "date", false, "DATE");
    };


    public RecommendDao(DaoConfig config) {
        super(config);
    }
    
    public RecommendDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"RECOMMEND\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"TEXT\" TEXT," + // 1: text
                "\"EVALUATION\" INTEGER," + // 2: evaluation
                "\"DATE\" INTEGER);"); // 3: date
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"RECOMMEND\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Recommend entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String text = entity.getText();
        if (text != null) {
            stmt.bindString(2, text);
        }
 
        Boolean evaluation = entity.getEvaluation();
        if (evaluation != null) {
            stmt.bindLong(3, evaluation ? 1L: 0L);
        }
 
        Long date = entity.getDate();
        if (date != null) {
            stmt.bindLong(4, date);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Recommend readEntity(Cursor cursor, int offset) {
        Recommend entity = new Recommend( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // text
            cursor.isNull(offset + 2) ? null : cursor.getShort(offset + 2) != 0, // evaluation
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3) // date
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Recommend entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setText(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setEvaluation(cursor.isNull(offset + 2) ? null : cursor.getShort(offset + 2) != 0);
        entity.setDate(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Recommend entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Recommend entity) {
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
