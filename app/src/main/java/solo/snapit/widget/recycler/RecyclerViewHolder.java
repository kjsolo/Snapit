package solo.snapit.widget.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by solo on 14/12/8.
 */
public class RecyclerViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context context;
    private OnRecyclerItemClickListener onItemClickListener;

    public RecyclerViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
    }

    public RecyclerViewHolder(View itemView, OnRecyclerItemClickListener onItemClickListener) {
        this(itemView);
        this.onItemClickListener = onItemClickListener;
    }

    public void populate(int position, T item) {
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClickListener(this, v);
        }
    }

}
