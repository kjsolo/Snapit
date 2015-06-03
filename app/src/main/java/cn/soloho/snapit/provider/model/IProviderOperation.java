package cn.soloho.snapit.provider.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by solo on 14/7/10.
 */
public interface IProviderOperation {

    public Cursor query(SQLiteDatabase database, Uri uri, String[] projection, String selection, String[] args, String order);

    public long insert(SQLiteDatabase database, ContentValues values);

    public int delete(SQLiteDatabase database, Uri uri, String selection, String... selectionArgs);

    public int update(SQLiteDatabase database, Uri uri, ContentValues values, String selection, String... selectionArgs);

    public Uri getContentUri();

}
