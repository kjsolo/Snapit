package cn.soloho.snapit.provider.model.toolbox;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.List;

import cn.soloho.snapit.provider.model.IEntity;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by solo on 14/7/10.
 */
public class EntityUtils {

    public static <T> T get(Cursor cursor, Class<T> clz) {
        return get(cursor, clz, false);
    }

    public static <T> T get(Cursor cursor, Class<T> clz, boolean closeCursor) {
        T data = cupboard().withCursor(cursor).get(clz);
        if (closeCursor) {
            cursor.close();
        }
        return data;
    }

    public static <T> List<T> list(Cursor cursor, Class<T> clz) {
        return list(cursor, clz, false);
    }

    public static <T> List<T> list(Cursor cursor, Class<T> clz, boolean closeCursor) {
        List<T> list = cupboard().withCursor(cursor).list(clz);
        if (closeCursor) {
            cursor.close();
        }
        return list;
    }

    public static ContentValues[] toContentValues(List<? extends IEntity> result, int type) {
        ContentValues[] values = new ContentValues[result.size()];
        for (int i = 0; i < result.size(); i++) {
            IEntity entity = result.get(i);
            entity.setLocalType(type);
            values[i] = entity.toContentValues();
        }
        return values;
    }

}
