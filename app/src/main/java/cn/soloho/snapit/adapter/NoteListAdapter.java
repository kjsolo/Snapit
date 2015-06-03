package cn.soloho.snapit.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.soloho.snapit.model.Note;
import cn.soloho.snapit.R;

/**
 * Created by solo on 15/5/31.
 */
public class NoteListAdapter extends ArrayAdapter<Note> {

    public NoteListAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.populate(position, convertView, parent, getItem(position));
        return convertView;
    }

    public static class ViewHolder {

        private static SimpleDateFormat mDateFormat;

        @InjectView(R.id.imageView) RoundedImageView mImageView;
        @InjectView(R.id.remark) TextView mRemarkView;
        @InjectView(R.id.remind) TextView mRemindView;
        @InjectView(R.id.created) TextView mCreatedView;

        public ViewHolder(View itemView) {
            ButterKnife.inject(this, itemView);
            if (mDateFormat == null) {
                mDateFormat = new SimpleDateFormat(itemView.getContext().getString(R.string.time_format), Locale.getDefault());
            }
        }

        public void populate(int position, View convertView, ViewGroup parent, Note item) {
            // 备注 地理位置 编辑时间
            ImageLoader.getInstance().displayImage(item.getImageUri(), mImageView);
            if (!TextUtils.isEmpty(item.getRemark())) {
                mRemarkView.setText(item.getRemark());
            } else if (!TextUtils.isEmpty(item.getLocation())) {
                mRemarkView.setText(item.getLocation());
            } else {
                mRemarkView.setText(convertView.getContext().getString(R.string.edit_on, mDateFormat.format(new Date(item.getLastModified()))));
            }

            if (item.getRemind() > 0) {
                mRemindView.setText(mDateFormat.format(new Date(item.getRemind())));
                mRemindView.setVisibility(View.VISIBLE);
            } else {
                mRemindView.setVisibility(View.GONE);
            }
            mCreatedView.setText(DateUtils.getRelativeTimeSpanString(item.getLocalCreated()));
        }

    }

}
