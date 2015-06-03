package solo.snapit.model;

import android.content.ContentValues;
import android.net.Uri;

import nl.qbusict.cupboard.CupboardFactory;
import solo.snapit.provider.SnapitContentProvider;
import solo.snapit.provider.model.BaseEntity;

/**
 * Created by solo on 15/2/18.
 */
public class Note extends BaseEntity {

    public static final Uri CONTENT_URI = Uri.parse("content://" + SnapitContentProvider.AUTHORITY + "/notes");

    private String id;
    private String imageUri;
    private String remark;
    private String location;
    private double lat;
    private double lng;
    private long remind;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getRemind() {
        return remind;
    }

    public void setRemind(long remind) {
        this.remind = remind;
    }

    @Override
    public ContentValues toContentValues() {
        return CupboardFactory.getInstance().withEntity(Note.class).toContentValues(this);
    }

    @Override
    public Uri getContentUri() {
        return CONTENT_URI;
    }
}
