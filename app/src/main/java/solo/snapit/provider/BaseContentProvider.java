package solo.snapit.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.SparseArray;

import solo.snapit.provider.model.IProviderOperation;

/**
 * Created by solo on 14/7/10.
 */
public abstract class BaseContentProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final SparseArray<IProviderOperation> operations = new SparseArray<>();

    private SQLiteOpenHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = onCreateSQLiteOpenHelper();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        IProviderOperation operation = operations.get(uriMatcher.match(uri));
        if (operation != null) {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            return operation.query(db, uri, projection, selection, selectionArgs, sortOrder);
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        IProviderOperation operation = operations.get(uriMatcher.match(uri));
        if (operation != null) {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            long rowId = operation.insert(db, values);
            if (rowId > 0) {
                Uri newUri = ContentUris.withAppendedId(operation.getContentUri(), rowId);
                getContext().getContentResolver().notifyChange(newUri, null);
                return newUri;
            }
            throw new SQLException("Failed to insert row into " + uri);
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        IProviderOperation operation = operations.get(uriMatcher.match(uri));
        if (operation != null) {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            int count = operation.delete(db, uri, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        IProviderOperation operation = operations.get(uriMatcher.match(uri));
        if (operation != null) {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            int count = operation.update(db, uri, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        IProviderOperation operation = operations.get(uriMatcher.match(uri));
        if (operation != null) {
            SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (ContentValues cv : values) {
                    long rowId = operation.insert(db, cv);
                    if (rowId <= 0) {
                        throw new SQLException("Failed to insert row into " + uri);
                    }
                }
                db.setTransactionSuccessful();
                getContext().getContentResolver().notifyChange(uri, null);
                return values.length;
            } finally {
                db.endTransaction();
            }
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    public void addProviderOperation(String path, int code, IProviderOperation operation) {
        // e.g: book -> 0 -> operation
        //      book_id -> 1 -> operation
        //      0/1 -> operation
        uriMatcher.addURI(getAuthority(), path, code);
        operations.put(code, operation);
    }

    protected abstract String getAuthority();

    protected abstract SQLiteOpenHelper onCreateSQLiteOpenHelper();

}
