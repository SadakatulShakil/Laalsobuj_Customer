package com.futureskyltd.app.external;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public abstract class GridRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = GridRecyclerOnScrollListener.class.getSimpleName();

    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = false; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 2; // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount;
    private int current_page = 0;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private GridLayoutManager mGridLayoutManager;

    public GridRecyclerOnScrollListener(GridLayoutManager gridLayoutManager) {
        this.mGridLayoutManager = gridLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (dy < 0) {
            return;
        }
        // check for scroll down only
        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mGridLayoutManager.getItemCount();
        firstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();


        synchronized (this) {
          /*  if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    current_page++;
                }
            }*/
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
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
        this.current_page = 0;
        previousTotal = 0;
    }

    public abstract void onLoadMore(int current_page);
}
