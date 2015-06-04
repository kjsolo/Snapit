package cn.soloho.snapit.model;

import android.content.ContentValues;
import android.graphics.Color;
import android.net.Uri;

import cn.soloho.snapit.provider.SnapitContentProvider;
import cn.soloho.snapit.provider.model.BaseEntity;
import nl.qbusict.cupboard.CupboardFactory;

/**
 * Created by solo on 15/2/18.
 */
public class Note extends BaseEntity {

    public static final Uri CONTENT_URI = Uri.parse("content://" + SnapitContentProvider.AUTHORITY + "/notes");
    public static final String COL_ITEM_BACKGROUND_COLOR = "itemBackgroundColor";
    public static final String COL_ITEM_REMARK_TEXT_COLOR = "itemRemarkTextColor";

    private String id;
    private String imageUri;
    private String remark;
    private String location;
    private double lat;
    private double lng;
    private long remind;

    private int itemBackgroundColor;
    private int itemRemarkTextColor;
    private int itemRemindTextColor;
    private int itemCreatedTextColor;

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

    public int getItemBackgroundColor() {
        return itemBackgroundColor;
    }

    public void setItemBackgroundColor(int itemBackgroundColor) {
        this.itemBackgroundColor = itemBackgroundColor;
    }

    public int getItemRemarkTextColor() {
        if (itemRemarkTextColor == 0) {
            return Color.BLACK;
        }
        return itemRemarkTextColor;
    }

    public void setItemRemarkTextColor(int itemRemarkTextColor) {
        this.itemRemarkTextColor = itemRemarkTextColor;
    }

    public int getItemRemindTextColor() {
        if (itemRemindTextColor == 0) {
            return Color.BLACK;
        }
        return itemRemindTextColor;
    }

    public void setItemRemindTextColor(int itemRemindTextColor) {
        this.itemRemindTextColor = itemRemindTextColor;
    }

    public int getItemCreatedTextColor() {
        if (itemCreatedTextColor == 0) {
            return Color.BLACK;
        }
        return itemCreatedTextColor;
    }

    public void setItemCreatedTextColor(int itemCreatedTextColor) {
        this.itemCreatedTextColor = itemCreatedTextColor;
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
