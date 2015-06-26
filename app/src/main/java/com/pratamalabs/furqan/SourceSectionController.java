package com.pratamalabs.furqan;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.pratamalabs.furqan.models.Source;
import com.pratamalabs.furqan.views.dragsortlistview.DragSortController;
import com.pratamalabs.furqan.views.dragsortlistview.DragSortListView;

/**
 * Created by pratamalabs on 18/9/13.
 */
class SourceSectionController extends DragSortController {

    DragSortListView mDslv;
    private int mPos;
    private int mDivPos;
    private SourceListAdapter mAdapter;
    private int origHeight = -1;

    public SourceSectionController(DragSortListView dslv, SourceListAdapter adapter) {
        super(dslv, R.id.drag_handle, DragSortController.ON_DOWN, 0);
        setRemoveEnabled(false);
        mDslv = dslv;
        mAdapter = adapter;
        mDivPos = adapter.getDivPosition();
    }

    public void setmDivPos(int mDivPos) {
        this.mDivPos = mDivPos;
    }

    @Override
    public View onCreateFloatView(int position) {
        Object item = mAdapter.getItem(position);
        if (!(item instanceof Source)) {
            return null;
        }
        Source source = (Source) item;
        if (SourceListAdapter.NOTDOWNLOADED.equals(source.getStatus())) {
            return null;
        }
        ;
        View v = mAdapter.getView(position, null, mDslv);
        return v;
    }

    @Override
    public int startDragPosition(MotionEvent ev) {
        int res = super.dragHandleHitPosition(ev);
        if (res == mDivPos || res == 0) {
            return DragSortController.MISS;
        }

        int width = mDslv.getWidth();

        if ((int) ev.getX() < width / 3) {
            return res;
        } else {
            return DragSortController.MISS;
        }
    }

    @Override
    public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint) {

        final int first = mDslv.getFirstVisiblePosition();
        final int lvDivHeight = mDslv.getDividerHeight();

        if (origHeight == -1) {
            origHeight = floatView.getHeight();
        }

        View div = mDslv.getChildAt(mDivPos - first);

        if (touchPoint.x > mDslv.getWidth() / 2) {
            float scale = touchPoint.x - mDslv.getWidth() / 2;
            scale /= (float) (mDslv.getWidth() / 5);
            ViewGroup.LayoutParams lp = floatView.getLayoutParams();
            lp.height = Math.max(origHeight, (int) (scale * origHeight));
            Log.d("mobeta", "setting height " + lp.height);
            floatView.setLayoutParams(lp);
        }

        if (div != null) {

            if (mPos >= 0) {
                View firstDiv = mDslv.getChildAt(0);
                final int limit = firstDiv.getBottom();
                if (floatPoint.y < limit) {
                    floatPoint.y = limit;
                }
            }

            if (mPos > mDivPos) {
                // don't allow floating View to go above
                // section divider
                final int limit = div.getBottom() + lvDivHeight;
                if (floatPoint.y < limit) {
                    floatPoint.y = limit;
                }
            } else {
                // don't allow floating View to go below
                // section divider
                final int limit = div.getTop() - lvDivHeight - floatView.getHeight();
                if (floatPoint.y > limit) {
                    floatPoint.y = limit;
                }
            }
        }
    }

    @Override
    public void onDestroyFloatView(View floatView) {
        //do nothing; block super from crashing
    }

}