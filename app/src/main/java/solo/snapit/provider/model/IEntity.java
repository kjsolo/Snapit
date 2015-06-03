package solo.snapit.provider.model;

import android.content.ContentValues;

/**
 * Created by solo on 14/7/10.
 */
public interface IEntity {

    /** 数据库id */
    public Long getLocalId();

    public void setLocalId(Long id);

    /** 数据库创建日期 */
    public Long getLocalCreated();

    public void setLocalCreated(long time);

    /** 数据库更新日期 */
    public Long getLastModified();

    public void setLastModified(long time);

    /** 数据库类型 */
    public int getLocalType();

    public void setLocalType(int type);

    /** 数据库是否有效 */
    public boolean isLocalValid();

    public ContentValues toContentValues();

}
