package com.github.ridkins.rud;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class LoadScrollListener extends RecyclerView.OnScrollListener{

    private int previousTotal = 0;
    private boolean loading = true;

    private LinearLayoutManager linearLayoutManager;

    public LoadScrollListener(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView,int dx, int dy) {
        super.onScrolled(recyclerView,dx,dy);

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = linearLayoutManager.getItemCount();
        int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 1)) {
            load();
            loading = true;
        }

    }

    public abstract void load();

}
