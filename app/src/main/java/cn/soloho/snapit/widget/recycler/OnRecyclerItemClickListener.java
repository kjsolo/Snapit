package cn.soloho.snapit.widget.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by solo on 15/3/23.
 */
public interface OnRecyclerItemClickListener {

    void onItemClickListener(RecyclerView.ViewHolder holder, View view);

}
