package solo.snapit.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * Created by solo on 15/5/31.
 */
public abstract class DataLoader<T> extends AsyncTaskLoader<T> {

    private T mData;

    public DataLoader(Context context) {
        super(context);
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(T data) {
        if (isReset()) {
            // 如果Loader已经Reset，我们认为这份数据是不需要的
            if (data != null) {
                onReleaseResources(data);
            }
            return;
        }
        T oldData = mData;
        mData = data;

        if (isStarted()) {
            // 如果Loader已经start了，立即分发data
            super.deliverResult(data);
        }

        // 处理旧的数据
        if (oldData != null && oldData != data) {
            onReleaseResources(oldData);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mData != null) {
            // 如果我们已经有存在的数据，直接分发
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            // 如果数据自从上次加载之后已经改变了，或者没有数据，我们就重新加载
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        // loader已经进入停止状态，我们应该尝试停止当前的加载
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(T data) {
        super.onCanceled(data);

        onReleaseResources(data);
    }

    @Override
    protected void onReset() {
        super.onReset();

        // 确保loader已经被停止
        onStopLoading();

        if (mData != null) {
            onReleaseResources(mData);
            mData = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(T data) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }

}
