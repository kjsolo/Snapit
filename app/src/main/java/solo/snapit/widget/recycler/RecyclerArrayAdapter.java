package solo.snapit.widget.recycler;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by solo on 14/12/8.
 */
public abstract class RecyclerArrayAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final Object mLock = new Object();
    private List<T> mItems = new ArrayList<>();
    private Activity mActivity;

    public RecyclerArrayAdapter(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public int getItemCount() {
        return mItems != null ? mItems.size() : 0;
    }

    public Activity getActivity() {
        return mActivity;
    }

    public void setItem(int index, T item) {
        synchronized (mLock) {
            mItems.set(index, item);
        }
        notifyItemChanged(index);
    }

    public void addItems(List<T> items) {
        synchronized (mLock) {
            mItems.addAll(items);
        }
        notifyItemRangeInserted(mItems.size(), items.size());
    }

    public void addItems(int index, List<T> items) {
        synchronized (mLock) {
            mItems.addAll(index, items);
        }
        notifyItemRangeInserted(index, items.size());
    }

    public void addItem(T item) {
        synchronized (mLock) {
            mItems.add(item);
        }
        notifyItemInserted(mItems.size() - 1);
    }

    public void addItem(int index, T item) {
        synchronized (mLock) {
            mItems.add(index, item);
        }
        notifyItemInserted(index);
    }

    public void clear() {
        int count = mItems.size();
        synchronized (mLock) {
            mItems.clear();
        }
        notifyItemRangeRemoved(0, count);
    }

    public void removeItem(int position) {
        synchronized (mLock) {
            mItems.remove(position);
        }
        notifyItemRemoved(position);
    }

    public void removeItems(int positionStart, int itemCount) {
        synchronized (mLock) {
            if (positionStart < 0 && positionStart >= mItems.size()) {
                return;
            }
            for (int i = (positionStart + itemCount) - 1; i >= positionStart; i--) {
                mItems.remove(i);
            }
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    public void removeItems(int positionStart) {
        synchronized (mLock) {
            if (positionStart < 0 && positionStart >= mItems.size()) {
                return;
            }
            removeItems(positionStart, mItems.size() - positionStart);
        }
    }

    public T getItem(int position) {
        return mItems.get(position);
    }

    public List<T> getItems() {
        return mItems;
    }

    public void sort(Comparator<? super T> comparator) {
        synchronized (mLock) {
            Collections.sort(mItems, comparator);
        }
        notifyDataSetChanged();
    }

}
