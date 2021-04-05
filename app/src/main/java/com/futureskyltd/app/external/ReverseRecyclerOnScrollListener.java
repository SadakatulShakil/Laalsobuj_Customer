package com.futureskyltd.app.external;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public abstract class ReverseRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = ReverseRecyclerOnScrollListener.class.getSimpleName();

    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = false; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 2; // The minimum amount of items to have below your current scroll position before loading more.
    int lastVisibleItem, visibleItemCount, totalItemCount;
    private int current_page = 0;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private LinearLayoutManager mLinearLayoutManager;

    public ReverseRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (dy > 0) {
            return;
        }
        // check for scroll up only
        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
        // to make sure only one onLoadMore is triggered
        synchronized (this) {
          /*  if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    current_page++;
                }
            }*/

            if (!loading && lastVisibleItem >= totalItemCount - visibleThreshold) {
                // End has been reached
                //    Log.v("END Reached", "END Reached=" + currentPage);
                current_page++;
                onLoadMore(current_page);
                loading = true;
            }
        }
    }


    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public void resetpagecount() {
        current_page = 0;
        previousTotal = 0;
    }

    public abstract void onLoadMore(int current_page);
}
