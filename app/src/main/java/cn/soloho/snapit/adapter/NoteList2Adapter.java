package cn.soloho.snapit.adapter;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.soloho.snapit.R;
import cn.soloho.snapit.model.Note;
import cn.soloho.snapit.tools.SLog;
import cn.soloho.snapit.widget.recycler.OnRecyclerItemClickListener;
import cn.soloho.snapit.widget.recycler.RecyclerArrayAdapter;
import cn.soloho.snapit.widget.recycler.RecyclerViewHolder;

/**
 * Created by solo on 15/6/1.
 */
public class NoteList2Adapter extends RecyclerArrayAdapter<Note, NoteList2Adapter.ViewHolder> implements OnRecyclerItemClickListener {

    private static final String TAG = NoteList2Adapter.class.getSimpleName();

    public NoteList2Adapter(Activity activity) {
        super(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.note_item2, parent, false);
        return new ViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.populate(position, getItem(position));
    }

    @Override
    public void onItemClickListener(RecyclerView.ViewHolder holder, View view) {
        // TODO 跳转到笔记详情
    }

    public static class ViewHolder extends RecyclerViewHolder<Note> {

        @InjectView(R.id.imageView) ImageView mImageView;
        @InjectView(R.id.remark) TextView mRemarkView;
        @InjectView(R.id.item_background) View mItemBgView;
        //@InjectView(R.id.remind) TextView mRemindView;
        //@InjectView(R.id.created) TextView mCreatedView;

        public ViewHolder(View itemView, OnRecyclerItemClickListener onItemClickListener) {
            super(itemView, onItemClickListener);
            ButterKnife.inject(this, itemView);
        }

        @Override
        public void populate(int position, final Note item) {
            super.populate(position, item);

            // 加载笔记图片
            ImageLoader.getInstance().displayImage(item.getImageUri(), mImageView, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);

                    if (item.getItemRemarkTextColor() != Color.BLACK) {
                        // 如果背景有颜色，我们的文字统一使用白色，
                        // 所以可以根据文字的颜色来判断是否配过颜色
                        SLog.d(TAG, "[NoteList2Adapter] Background color was set " + item.getItemBackgroundColor());
                        return;
                    }

                    // 生成文字覆盖所需的缩略图
                    Bitmap overlay = ThumbnailUtils.extractThumbnail(loadedImage,
                            mRemarkView.getMeasuredWidth(),
                            mRemarkView.getMeasuredHeight());

                    // 生成对应的背景颜色
                    Palette.from(overlay).generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            if (getContext() == null) {
                                return;
                            }

                            Palette.Swatch swatch = palette.getVibrantSwatch();
                            if (swatch == null) {
                                swatch = palette.getMutedSwatch();
                            }
                            if (swatch != null) {
                                // 更新这一项的背景和文字颜色
                                ContentValues values = new ContentValues();
                                values.put(Note.COL_ITEM_BACKGROUND_COLOR, swatch.getRgb());
                                values.put(Note.COL_ITEM_REMARK_TEXT_COLOR, Color.WHITE);

                                Uri uri = ContentUris.withAppendedId(Note.CONTENT_URI, item.getLocalId());
                                getContext().getContentResolver().update(uri, values, null, null);
                            }
                        }
                    });
                }
            });

            mRemarkView.setText(item.getRemark());
            mRemarkView.setTextColor(item.getItemRemarkTextColor());
            mItemBgView.setBackgroundColor(item.getItemBackgroundColor());
            mItemBgView.getBackground().setAlpha(153);

        }
    }

}

