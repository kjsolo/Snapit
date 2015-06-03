package cn.soloho.snapit.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.soloho.snapit.model.Note;
import cn.soloho.snapit.widget.recycler.OnRecyclerItemClickListener;
import cn.soloho.snapit.widget.recycler.RecyclerArrayAdapter;
import cn.soloho.snapit.widget.recycler.RecyclerViewHolder;
import cn.soloho.snapit.R;

/**
 * Created by solo on 15/6/1.
 */
public class NoteList2Adapter extends RecyclerArrayAdapter<Note, NoteList2Adapter.ViewHolder> implements OnRecyclerItemClickListener {

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
        //@InjectView(R.id.remark) TextView mRemarkView;
        //@InjectView(R.id.remind) TextView mRemindView;
        //@InjectView(R.id.created) TextView mCreatedView;

        public ViewHolder(View itemView, OnRecyclerItemClickListener onItemClickListener) {
            super(itemView, onItemClickListener);
            ButterKnife.inject(this, itemView);
        }

        @Override
        public void populate(int position, Note item) {
            super.populate(position, item);

            // 加载笔记图片
            ImageLoader.getInstance().displayImage(item.getImageUri(), mImageView);

        }
    }

}

