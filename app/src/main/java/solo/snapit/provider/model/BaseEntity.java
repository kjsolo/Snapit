package solo.snapit.provider.model;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by solo on 14/7/10.
 */
public abstract class BaseEntity implements IEntity, IProviderOperation, Parcelable {

    public static final String LOCAL_ID = BaseColumns._ID;
    public static final String LOCAL_CREATED = "localCreated";
    public static final String LOCAL_LAST_MODIFIED = "lastModified";
    public static final String LOCAL_TYPE = "localType";

    public static final int LOCAL_TYPE_NONE = 0;

    private Long _id;
    private long localCreated;
    private long lastModified;
    private int localType;

    public BaseEntity() {

    }

    public BaseEntity(BaseEntity baseEntity) {
        this._id = baseEntity._id;
        this.localCreated = baseEntity.localCreated;
        this.lastModified = baseEntity.lastModified;
        this.localType = baseEntity.localType;
    }

    @Override
    public Long getLocalId() {
        return _id;
    }

    @Override
    public void setLocalId(Long id) {
        _id = id;
    }

    @Override
    public Long getLocalCreated() {
        return localCreated;
    }

    @Override
    public void setLocalCreated(long time) {
        localCreated = time;
    }

    @Override
    public Long getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(long time) {
        lastModified = time;
    }

    @Override
    public int getLocalType() {
        return localType;
    }

    @Override
    public void setLocalType(int type) {
        localType = type;
    }

    @Override
    public boolean isLocalValid() {
        return false;
    }

    @Override
    public Cursor query(SQLiteDatabase database, Uri uri, String[] projection, String selection, String[] args, String order) {
        long id = getUriId(uri);
        Class<? extends BaseEntity> clz = this.getClass();
        if (id > 0) {
            return cupboard().withDatabase(database).query(clz).byId(id).getCursor();
        } else {
            return cupboard().withDatabase(database).query(clz).withProjection(projection)
                    .withSelection(selection, args).orderBy(order).getCursor();
        }
    }

    @Override
    public long insert(SQLiteDatabase database, ContentValues values) {
        long now = System.currentTimeMillis();
        if (!values.containsKey(LOCAL_CREATED) || values.getAsLong(LOCAL_CREATED) <= 0) {
            values.put(LOCAL_CREATED, now);
        }
        if (!values.containsKey(LOCAL_LAST_MODIFIED) || values.getAsLong(LOCAL_LAST_MODIFIED) <= 0) {
            values.put(LOCAL_LAST_MODIFIED, now);
        }
        Class<? extends BaseEntity> clz = this.getClass();
        return cupboard().withDatabase(database).put(clz, values);
    }

    @Override
    public int delete(SQLiteDatabase database, Uri uri, String selection, String... selectionArgs) {
        long id = getUriId(uri);
        Class<? extends BaseEntity> clz = this.getClass();
        int ret;
        if (id > 0) {
            cupboard().withDatabase(database).delete(clz, id);
            ret = 1;
        } else {
            ret = cupboard().withDatabase(database).delete(clz, selection, selectionArgs);
        }
        return ret;
    }

    @Override
    public int update(SQLiteDatabase database, Uri uri, ContentValues values, String selection, String... selectionArgs) {
        long now = System.currentTimeMillis();
        values.put(LOCAL_LAST_MODIFIED, now);
        long id = getUriId(uri);
        Class<? extends BaseEntity> clz = this.getClass();
        if (id > 0) {
            if (!values.containsKey(LOCAL_ID)) {
                values.put(LOCAL_ID, id);
            }
            return cupboard().withDatabase(database).update(clz, values);
        } else {
            return cupboard().withDatabase(database).update(clz, values, selection, selectionArgs);
        }
    }

    private long getUriId(Uri uri) {
        try {
            return ContentUris.parseId(uri);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this._id);
        dest.writeLong(this.localCreated);
        dest.writeLong(this.lastModified);
        dest.writeInt(this.localType);
    }

    protected BaseEntity(Parcel in) {
        this._id = (Long) in.readValue(Long.class.getClassLoader());
        this.localCreated = in.readLong();
        this.lastModified = in.readLong();
        this.localType = in.readInt();
    }

    public int asInt(String value, int defaultValue) {
        if (value == null) {
            value = String.valueOf(defaultValue);
        }
        return Integer.valueOf(value);
    }

    public double asDouble(String value, double defaultValue) {
        if (value == null) {
            value = String.valueOf(defaultValue);
        }
        return Double.valueOf(value);
    }

    public long asLong(String value, long defaultValue) {
        if (value == null) {
            value = String.valueOf(defaultValue);
        }
        return Long.valueOf(value);
    }

    public boolean asBoolean(String value, boolean defaultValue) {
        if (value == null) {
            value = String.valueOf(defaultValue ? "1" : "0");
        }
        return value.equals("1");
    }

    public String asString(boolean value) {
        return value ? "1" : "0";
    }
}
