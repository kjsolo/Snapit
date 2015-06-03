package solo.snapit.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import solo.snapit.R;
import solo.snapit.adapter.NoteList2Adapter;
import solo.snapit.model.Note;
import solo.snapit.provider.model.toolbox.EntityUtils;

/**
 * Created by solo on 15/2/18.
 */
public class NoteListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @InjectView(R.id.recyclerView) RecyclerView mRecyclerView;

    private NoteList2Adapter mAdapter;

    /**
     * 生成NoteListFragment实例
     */
    public static Fragment newInstance() {
        return new NoteListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.note_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 注入View
        ButterKnife.inject(this, view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 设置列表布局
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 设置Adapter
        mAdapter = new NoteList2Adapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        // 加载数据
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 重置释放View
        ButterKnife.reset(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Note.CONTENT_URI, // uri
                null, // projection
                null, // selection
                null, // selectionArgs
                Note.LOCAL_ID + " DESC" // sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            return;
        }

        // 对比数据库和列表的数据，动态添加数据
        if (data.moveToFirst()) {
            int rowIDColumn = data.getColumnIndexOrThrow("_id");
            int lastModifiedColumn = data.getColumnIndex(Note.LOCAL_LAST_MODIFIED);

            // 收集数据库中的id
            Long[] newRowIds = new Long[data.getCount()];
            do {
                long rowId = data.getLong(rowIDColumn);
                newRowIds[data.getPosition()] = rowId;
            } while (data.moveToNext());

            // 收集列表中的id
            List<Note> items = mAdapter.getItems();
            Long[] oldRowIds = new Long[items.size()];
            for (int i = 0; i < items.size(); i++) {
                long rowId = items.get(i).getLocalId();
                oldRowIds[i] = rowId;
            }

            if (newRowIds.length > 0 && oldRowIds.length == 0) {
                data.moveToFirst();
                List<Note> notes = EntityUtils.list(data, Note.class);
                mAdapter.addItems(notes);
                return;
            } else if (newRowIds.length == 0 && oldRowIds.length > 0) {
                mAdapter.clear();
                return;
            }

            // 先排序，排序是重点，减少查询
            // 实际上数据库里面读出来的就是经过排序的，所以没必要再次排序
            //Arrays.sort(newRowIds, Collections.reverseOrder());
            //Arrays.sort(oldRowIds, Collections.reverseOrder());

            int newIndex = 0;
            int oldIndex = 0;
            int listIndexOffset = 0;

            while (newIndex < newRowIds.length && oldIndex < oldRowIds.length) {
                int compare = Long.signum(oldRowIds[oldIndex] - newRowIds[newIndex]);

                int listIndex = oldIndex + listIndexOffset;
                switch (compare) {
                    // oldID < newID
                    case -1: {
                        // 表明old集合中的这一项在new集合中找不到
                        // 所以我们将这个从列表中删掉
                        mAdapter.removeItem(listIndex);
                        listIndexOffset--;

                        oldIndex++;
                        break;
                    }

                    // oldID > newID
                    case 1: {
                        // 表明new集合中的这一项在old集合中找不到
                        // 所以我们将这个添加到列表中去
                        data.moveToPosition(newIndex);
                        Note newNote = EntityUtils.get(data, Note.class);
                        mAdapter.addItem(listIndex, newNote);
                        listIndexOffset++;

                        newIndex++;
                        break;
                    }

                    // oldID == newID
                    default: {

                        // 两个的最后更新时间
                        long oldNoteLM = mAdapter.getItem(oldIndex + listIndexOffset).getLastModified();
                        long newNoteLM = data.getLong(lastModifiedColumn);

                        // 比较两个最后的更新时间，如果不一样，我们就替换
                        if (oldNoteLM != newNoteLM) {
                            data.moveToPosition(newIndex);
                            Note newNote = EntityUtils.get(data, Note.class);
                            mAdapter.setItem(listIndex, newNote);
                        }

                        oldIndex++;
                        newIndex++;
                        break;
                    }
                }
            }

            if (oldIndex == oldRowIds.length - 1 && newIndex < newRowIds.length - 1) {
                data.moveToPosition(newIndex);
                List<Note> notes = EntityUtils.list(data, Note.class);
                mAdapter.addItems(oldIndex + listIndexOffset, notes);
            } else if (newIndex == newRowIds.length - 1 && oldIndex < oldRowIds.length - 1) {
                mAdapter.removeItems(oldIndex + listIndexOffset);
            }
        } else {
            // Empty
            mAdapter.clear();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // 清除数据
        mAdapter.clear();
    }

}
