package com.model.loadmoreandrefresh;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 继承自SwipeRefreshLayout,从而实现滑动到底部时上拉加载更多的功能.
 *
 * @author XLC
 * @version [1.0, 2015/12/31]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class LoadMoreAndRefreshLayout extends SwipeRefreshLayout implements AbsListView.OnScrollListener {

    /**
     * 滑动到最下面时的上拉操作
     */
    private int mTouchSlop;

    /**
     * listview实例
     */
    private ListView mListView;

    /**
     * 上拉监听器, 到了最底部的上拉加载操作
     */
    private OnLoadListener mOnLoadListener;

    /**
     * ListView的加载中footer
     */
    private View mListViewFooter;

    /**
     * 按下时的y坐标
     */
    private int mYDown;
    /**
     * 抬起时的y坐标, 与mYDown一起用于滑动到底部时判断是上拉还是下拉
     */
    private int mLastY;

    /**
     * 是否在加载中 ( 上拉加载更多 )
     */
    private boolean isLoading = false;


    private View emptyView;

    /**
     * 父布局
     */
    private ViewGroup parentGroup;

    /**
     * 数据为空时的提示信息
     */
    private TextView emptyInfo;

    /**
     * 重新获取的按钮
     */
    private View getAgain;

    /**
     * 进度条
     */
    private View progress;

    /**
     * footerView的提示信息
     */
    private TextView footerInfo;

    public LoadMoreAndRefreshLayout(Context context) {
        this(context, null);
    }

    public LoadMoreAndRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mListViewFooter = LayoutInflater.from(context).inflate(R.layout.listview_footer, null,
                false);
        emptyView = LayoutInflater.from(context).inflate(R.layout.empty_view, null,
                false);
        emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // 初始化ListView对象
        if (mListView == null) {
            getListView();
        }
    }

    /**
     * 获取ListView对象
     */
    private void getListView() {
        int childCount = getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                if (childView instanceof ListView) {
                    mListView = (ListView) childView;
                    // 设置滚动监听器给ListView, 使得滚动的情况下也可以自动加载
                    mListView.setOnScrollListener(this);
                    if (mListView.getFooterViewsCount() == 0) {
                        mListView.addFooterView(mListViewFooter);
                        mListView.setAdapter(mListView.getAdapter());
                        mListView.removeFooterView(mListViewFooter);
                    }
                }
            }
        }
    }

    /*
    * (non-Javadoc)
    * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
    */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 按下
                mYDown = (int) event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                // 移动
                mLastY = (int) event.getRawY();
                break;

            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    /**
     * 是否可以加载更多, 条件是到了最底部, listview不在加载中, 且为上拉操作.
     *
     * @return
     */
    private boolean canLoad() {
        return isBottom() && !isLoading && isPullUp();
    }

    /**
     * 判断是否到了最底部
     */
    private boolean isBottom() {

        if (mListView != null && mListView.getAdapter() != null) {
            return mListView.getLastVisiblePosition() == (mListView.getAdapter().getCount() - 1);
        }
        return false;
    }

    /**
     * 是否是上拉操作
     *
     * @return
     */
    private boolean isPullUp() {
        return (mYDown - mLastY) >= mTouchSlop;
    }

    /**
     * 如果到了最底部,而且是上拉操作.那么执行onLoad方法
     */
    private void loadData() {
        if (mOnLoadListener != null) {
            // 设置状态
            setLoading(true);
            //
            mOnLoadListener.onLoad();
        }
    }

    /**
     * @param loading
     */
    public void setLoading(boolean loading) {
        isLoading = loading;
        if (isLoading && mListView.getFooterViewsCount() == 0) {
            mListViewFooter.setVisibility(View.VISIBLE);
            mListView.addFooterView(mListViewFooter);
        } else if (mListView.getAdapter() instanceof HeaderViewListAdapter) {
            mListView.removeFooterView(mListViewFooter);
        } else {
            mListViewFooter.setVisibility(View.GONE);
            mYDown = 0;
            mLastY = 0;
        }

        if (progress != null && footerInfo != null) {
            progress.setVisibility(View.VISIBLE);
            footerInfo.setText("正在加载");
        }
    }

    /**
     * 获取上拉刷新状态
     *
     * @return
     */
    public boolean getLoading() {
        return isLoading;
    }

    /**
     * @param loadListener
     */
    public void setOnLoadListener(OnLoadListener loadListener) {
        mOnLoadListener = loadListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        // 滚动时到了最底部加载更多
        if (canLoad()) {
            loadData();
        }
    }


    /**
     * 加载更多的监听器
     *
     * @author mrsimple
     */
    public interface OnLoadListener {
        void onLoad();
    }

    /**
     * 数据加载完毕时可以调用此方法
     *
     * @param hintInfo 给用户的提示信息
     */
    public void setLoaded(String hintInfo) {
        if (progress == null && footerInfo == null) {
            progress = mListViewFooter.findViewById(R.id.pull_to_refresh_load_progress);
            footerInfo = (TextView) mListViewFooter.findViewById(R.id.pull_to_refresh_loadmore_text);
        }

        progress.setVisibility(GONE);
        footerInfo.setText(hintInfo);
    }

    public void setLoaded() {
        setLoaded(getContext().getString(R.string.data_is_loaded));
    }

    public void setEmptyView(boolean showButton) {
        this.setEmptyView(getContext().getString(R.string.get_data_failed_please_get_again), showButton, 0);
    }

    /**
     * 当数据为空时显示的布局  emptyView会加在父View的第0个位置
     */
    public void setEmptyView(String textInfo, Boolean showButton, int dataSize) {
        if (dataSize == 0) {
            if (parentGroup == null) {
                parentGroup = (ViewGroup) getParent();
                emptyView.setFocusable(false);
                getAgain = emptyView.findViewById(R.id.getAgain);
                emptyInfo = (TextView) emptyView.findViewById(R.id.emptyInformation);
            }
            if (parentGroup.getChildAt(0) != emptyView) {
                parentGroup.addView(emptyView, 0);
            }
            if (textInfo != null) {
                emptyInfo.setText(textInfo);
            }
            getAgain.setVisibility(showButton ? VISIBLE : GONE);
        } else if (parentGroup != null && parentGroup.getChildAt(0) == emptyView) {
            parentGroup.removeView(emptyView);
        }

    }

    /**
     * 设置重新获取按钮的监听
     *
     * @param onClickListener
     */
    public void setGetAgainButtonListener(OnClickListener onClickListener) {
        if (getAgain != null) {
            getAgain.setOnClickListener(onClickListener);
        }
    }

    /**
     * 修改footerView
     *
     * @param newFooterView
     */
    public void exchangeFooterView(View newFooterView) {
        mListViewFooter = newFooterView;
    }

    /**
     * 修改emptyView
     *
     * @param newEmptyView
     */
    public void exchangeEmptyView(View newEmptyView) {
        emptyView = newEmptyView;
    }
}