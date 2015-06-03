package cn.soloho.snapit.provider;

import android.content.Context;

import cn.soloho.snapit.model.Note;

/**
 * Created by solo on 15/3/2.
 */
public class SnapitSQLiteOpenHelper extends BaseSQLiteOpenHelper {

    private static final String DATABASE_NAME = "card.db";
    private static final int DATABASE_VERSION = 1;

    static {
//        CupboardFactory.setCupboard(new CupboardBuilder()
//                .useAnnotations()
//                .registerEntityConverterFactory(new MyEntityConverterFactory())
//                .build());
    }

    public SnapitSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    protected Class[] getRegisters() {
        return new Class[] {
                Note.class,
        };
    }
}
