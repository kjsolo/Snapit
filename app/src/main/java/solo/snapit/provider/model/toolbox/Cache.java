package solo.snapit.provider.model.toolbox;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.WeakHashMap;

import solo.snapit.provider.model.BaseEntity;

/**
 * Created by solo on 14/7/15.
 */
public class Cache {

    private static final String TAG = Cache.class.getSimpleName();
    private static final boolean DEBUG = false;

    private Cache() {}

    public static CacheEntity with(Context context) {
        return new CacheEntity(context);
    }

    public static final class CacheEntity {

        private static final WeakHashMap<Uri, BaseEntity> entitys = new WeakHashMap<>();

        private final Context context;

        public CacheEntity(Context context) {
            this.context = context;
        }

        public <T extends BaseEntity> T get(Uri uri, String selection, Class<T> clz) {
            BaseEntity entity = entitys.get(uri);
            if (entity != null) {
                if (entity.getLocalId() != null) {
                    if (DEBUG) Log.d(TAG, "[get] 使用内存缓存");
                    return (T) entity;
                }
                if (DEBUG) Log.d(TAG, "[get] 没有LocalId，正在修复");
            }
            Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
            T localEntity = EntityUtils.get(cursor, clz);
            if (localEntity != null) {
                entitys.put(uri, localEntity);
            }
            return localEntity;
        }

        public Uri save(BaseEntity entity) {
            // IDE的Bug，这里强转
            String entityString = ((Object) entity).toString();

            ContentResolver cr = context.getContentResolver();
            Uri uri = entity.getContentUri();
            ContentValues values = entity.toContentValues();

            if (uri == null) {
                throw new RuntimeException("[save] " + entityString + " 无法保存数据，getContentUri不能为空");
            }

            if (values == null) {
                throw new RuntimeException("[save] " + entityString + " 无法保存数据，toContentValues不能为空");
            }

            Long id = entity.getLocalId();
            if (id != null) {
                int num = cr.update(uri, values, BaseColumns._ID + "=" + id, null);
                if (DEBUG) Log.d(TAG, "[save] " + entityString + " 已更新" + num + "条");
            } else {
                Uri entityUri = cr.insert(uri, values);
                id = ContentUris.parseId(entityUri);
                entity.setLocalId(id);
                if (DEBUG) Log.d(TAG, "[save] " + entityString + " 已插入");
            }
            entitys.put(uri, entity);
            return ContentUris.withAppendedId(uri, id);
        }

        public void delete(Uri uri, String where) {
            if (DEBUG) Log.d(TAG, "[delete] 删除");
            entitys.remove(uri);
            context.getContentResolver().delete(uri, where, null);
        }

    }

}
