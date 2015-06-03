package cn.soloho.snapit.provider.model.toolbox;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import cn.soloho.snapit.provider.SnapitContentProvider;
import cn.soloho.snapit.provider.model.BaseEntity;

/**
 * Created by solo on 14/9/9.
 */
public class ListCache {

    private static final String TAG = ListCache.class.getSimpleName();

    public static <T extends BaseEntity> List<T> get(Context context, Uri uri, int localType, Class<T> clz) {
        return get(context, uri, null, BaseEntity.LOCAL_TYPE + "=" + localType, null, null, clz);
    }

    public static <T extends BaseEntity> List<T> get(Context context, Uri uri,  String[] projection, String selection, String[] selectionArgs, String sortOrder, Class<T> clz) {
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        return EntityUtils.list(cursor, clz, true);
    }

    public static <T extends BaseEntity> void save(Context context, Uri uri, int localType, List<T> result) {
        if (result == null) {
            return;
        }
        int n = result.size();
        ContentValues[] values = new ContentValues[n];
        for (int i = 0; i < n; i++) {
            // 设置实例的本地类型，便于缓存查找
            T entity = result.get(i);
            values[i] = entity.toContentValues();
            values[i].put(BaseEntity.LOCAL_TYPE, localType);
        }

        context.getContentResolver().delete(uri, BaseEntity.LOCAL_TYPE + "=" + localType, null);
        if (values.length > 0) {
            // 删除旧数据，插入新的数据
            context.getContentResolver().bulkInsert(uri, values);
        }
    }

    public static <T extends BaseEntity> void update(Context context, Uri uri, int localType, List<T> result) {
        if (result == null) {
            return;
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for (T entity : result) {
            if (entity.getLocalId() != null) {
                ContentValues cv = entity.toContentValues();
                // 我们不需要更新时间
                cv.put(BaseEntity.LOCAL_TYPE, localType);
                cv.remove(BaseEntity.LOCAL_LAST_MODIFIED);
                ops.add(ContentProviderOperation.newUpdate(Uri.withAppendedPath(uri, entity.getLocalId().toString()))
                        .withValues(cv)
                        .withYieldAllowed(true)
                        .build());
            } else {
                ContentValues cv = entity.toContentValues();
                cv.put(BaseEntity.LOCAL_TYPE, localType);
                ops.add(ContentProviderOperation.newInsert(uri)
                        .withValues(cv)
                        // https://code.google.com/p/android/issues/detail?id=34610
                        .withYieldAllowed(true)
                        .build());
            }
        }

        if (!ops.isEmpty()) {
            try {
                context.getContentResolver().applyBatch(SnapitContentProvider.AUTHORITY, ops);
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }
        }
    }

    public static void delete(Context context, Uri uri, int localType) {
        context.getContentResolver().delete(uri, BaseEntity.LOCAL_TYPE + "=" + localType, null);
    }

    public static <T extends BaseEntity> boolean isValid(List<T> cacheList) {
        // List的前后都为没有过期，我们就暂且认为都没有过期
        boolean isValid = false;
        if (cacheList != null && !cacheList.isEmpty()) {
            isValid = cacheList.get(0).isLocalValid();
            int n = cacheList.size();
            if (n > 1) {
                isValid = cacheList.get(n - 1).isLocalValid();
            }
        }
        return isValid;
    }

    public static <T extends BaseEntity> List<T> copyLocal(List<T> cacheData, List<T> newData) {
        if (cacheData == null && newData != null) {
            return new ArrayList<>(newData);
        } else if (cacheData != null && newData == null) {
            return new ArrayList<>(cacheData);
        } else if (cacheData != null) {
            ArrayList<T> updateTemp = new ArrayList<>(cacheData);
            for (T n : newData) {
                int ci = cacheData.indexOf(n);
                if (ci >= 0) {
                    T c = cacheData.get(ci);
                    n.setLocalId(c.getLocalId());
                    n.setLocalCreated(c.getLocalCreated());
                    n.setLocalType(c.getLocalType());
                    updateTemp.set(ci, n);
                } else {
                    updateTemp.add(n);
                }
            }
            return updateTemp;
        }
        return null;
    }

}
