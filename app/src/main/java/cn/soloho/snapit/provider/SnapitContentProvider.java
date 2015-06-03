package cn.soloho.snapit.provider;

import android.database.sqlite.SQLiteOpenHelper;

import cn.soloho.snapit.model.Note;

/**
 * Created by solo on 15/3/2.
 */
public class SnapitContentProvider extends BaseContentProvider {
    public static final String AUTHORITY = "cn.soloho.snapit.provider";

    private static final int NOTES = 1;
    private static final int NOTE_ID = 2;

    @Override
    public boolean onCreate() {
        // 这里使用User作为操作对象，是因为User实现了IProviderOperation接口
        Note noteOperation = new Note();
        addProviderOperation(
                "notes",        // 作为判断content uri的类型
                NOTES,          // path对应的code，也是作为operation的key
                noteOperation   // 实现了基本数据库操作的类
        );
        addProviderOperation("notes/#", NOTE_ID, noteOperation);
        return super.onCreate();
    }

    @Override
    protected String getAuthority() {
        return AUTHORITY;
    }

    @Override
    protected SQLiteOpenHelper onCreateSQLiteOpenHelper() {
        return new SnapitSQLiteOpenHelper(getContext());
    }
}
