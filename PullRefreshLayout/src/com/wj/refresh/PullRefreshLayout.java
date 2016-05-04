package com.wj.refresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ScrollView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * 下拉刷新上拉加载控件
 * Created by jia.wei on 16/3/28.
 */
public class PullRefreshLayout extends ViewGroup {

    private RefreshHeaderView mRefreshHeaderView = null; // 下拉刷新view
    private LoadingFooterView mLoadingFooterView = null; // 上拉加载view
    private View mRefreshView = null; // 刷新view

    private OnRefreshListener mRefreshListener = null;
    private float mDownY;
    private float mDownX;
    private float mLastY;
    private boolean mIsLoadingMore; // 是否正在加载
    private boolean mIsDispatchDown;
    private float mPullDownDistance; // 下拉距离
    private float mPullUpDistance; // 上拉距离

    private boolean mIsHeaderCollapseAnimating;
    private boolean mIsFooterCollapseAnimating;

    private int mMode;
    // 关闭下拉刷新和上拉加载更多
    public static final int DISABLED = 0x0;
    // 只开启下拉刷新
    public static final int PULL_FROM_START = 0x1;
    // 只开启上拉加载更多
    public static final int PULL_FROM_END = 0x2;
    // 开启下拉刷新和上拉加载更多
    public static final int BOTH = 0x3;

    // 阻力因数,数值越小阻力越大,数值必须大于1
    private final float PULL_DOWN_FACTOR = 20;
    private final float PULL_UP_FACTOR = 5;

    public PullRefreshLayout(Context context) {
        this(context, null);
    }

    public PullRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullRefreshLayout);
        if (a.hasValue(R.styleable.PullRefreshLayout_refreshMode)) {
            mMode = a.getInteger(R.styleable.PullRefreshLayout_refreshMode, DISABLED);
        }
        a.recycle();

        createRefreshHeaderView();
        createLoadingFooterView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mRefreshView == null) {
            ensureRefreshView();
        }
        if (mRefreshView == null) {
            return;
        }
        mRefreshView.measure(MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));

        measureView(mRefreshHeaderView);
        measureView(mLoadingFooterView);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (0 == getChildCount()) {
            return;
        }

        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();
        int childRight = getPaddingRight();
        int childBottom = getPaddingBottom();

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int refreshViewWidth = width - childLeft - childRight;
        int refreshViewHeight = height - childTop - childBottom;
        mRefreshView.layout(childLeft, childTop, childLeft + refreshViewWidth, childTop + refreshViewHeight);

        int headerHeight = mRefreshHeaderView.getMeasuredHeight();
        int headerWidth = mRefreshHeaderView.getMeasuredWidth() - childLeft - childRight;
        mRefreshHeaderView.layout(childLeft, - headerHeight, childLeft + headerWidth, 0);

        int footerHeight = mLoadingFooterView.getMeasuredHeight();
        int footerWidth = mLoadingFooterView.getMeasuredWidth() - childLeft - childRight;
        int footerTop = refreshViewHeight + childBottom;
        mLoadingFooterView.layout(childLeft, footerTop, childLeft + footerWidth, footerTop + footerHeight);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                mLastY = mDownY;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = ev.getX() - mDownX;
                float dy = ev.getY() - mDownY;
                RefreshHeaderView.PullDownRefreshStatus refreshStatus = mRefreshHeaderView.getRefreshStatus();

                if ((refreshStatus == RefreshHeaderView.PullDownRefreshStatus.REFRESHING || mIsLoadingMore)) {
                    if (Math.abs(dy) > 5) {
                        return true;
                    }
                } else {
                    if (Math.abs(dy) > Math.abs(dx)) {
                        return true;
                    }
                }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsHeaderCollapseAnimating || mIsFooterCollapseAnimating) {
            return true;
        }

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                if (!mIsFooterCollapseAnimating && !mIsHeaderCollapseAnimating) {
                    scrollRefreshLayout(event);
                }
                mLastY = event.getY();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                RefreshHeaderView.PullDownRefreshStatus refreshStatus = mRefreshHeaderView.getRefreshStatus();
                // 根据下拉状态,判断是隐藏header,还是触发下拉刷新
                if (refreshStatus == RefreshHeaderView.PullDownRefreshStatus.RELEASE_REFRESH) {
                    collapseRefreshHeaderView(RefreshHeaderView.PullDownRefreshStatus.REFRESHING);
                } else if (refreshStatus == RefreshHeaderView.PullDownRefreshStatus.PULL_DOWN_REFRESH && getScrollY() < 0) {
                    collapseRefreshHeaderView(refreshStatus);
                }

                if (!mIsLoadingMore && getScrollY() > 0) {
                    collapseLoadingFooterView();
                }

                mPullDownDistance = 0;
                mPullUpDistance = 0;

                if (mIsDispatchDown) {
                    mRefreshView.dispatchTouchEvent(event);
                    mIsDispatchDown = false;
                }
                break;
        }
        return true;
    }

    /**
     * 根据手指在屏幕上滑动的距离滚动RefreshLayout
     */
    private void scrollRefreshLayout(MotionEvent event) {
        int scrollY = getScrollY();
        float dy = mLastY - event.getY();
        RefreshHeaderView.PullDownRefreshStatus refreshStatus = mRefreshHeaderView.getRefreshStatus();

        if (refreshStatus == RefreshHeaderView.PullDownRefreshStatus.REFRESHING) {
            if (scrollY == 0) {
                // scrollY == 0时,下拉刷新view已经不可见,此时需要判断事件由谁来处理
                if (dy < 0 && !canChildScrollUp()) {
                    //方向为下拉,并且refreshView不能继续往下滚时自己处理事件,否则事件分发给refreshView处理
                    scrollRefreshLayoutWithRefreshing((int) dy, scrollY);
                } else {
                    dispatchTouchEventToRefreshView(event);
                }
            } else {
                scrollRefreshLayoutWithRefreshing((int) dy, scrollY);
            }
        } else if (mIsLoadingMore) {
            if (scrollY == 0) {
                // scrollY == 0时,上拉加载view已经不可见,此时需要判断事件由谁来处理
                if (dy > 0 && !canChildScrollDown()) {
                    // 方向为上拉,并且refreshView不能继续往上滚时自己处理事件,否则事件分发给refreshView处理
                    scrollRefreshLayoutWithLoading((int) dy, scrollY);
                } else {
                    dispatchTouchEventToRefreshView(event);
                }
            } else {
                scrollRefreshLayoutWithLoading((int) dy, scrollY);
            }
        } else {
            if (dy < 0 && !canChildScrollUp() || getScrollY() < 0) {
                // 下拉刷新
                if (mMode == PULL_FROM_START || mMode == BOTH) {
                    mPullDownDistance += -dy;

                    if (mPullDownDistance > 0) {
                        // 阻尼
                        float damping = (float) (mPullDownDistance / (Math.log(mPullDownDistance) / Math.log(PULL_DOWN_FACTOR)));
                        damping = Math.max(0, damping);
                        scrollTo(0, (int) -damping);

                        if (damping >= mRefreshHeaderView.getHeight()) {
                            // 滚动距离超过下拉刷新视图的高,将状态变为释放
                            mRefreshHeaderView.setRefreshStatus(RefreshHeaderView.PullDownRefreshStatus.RELEASE_REFRESH);
                        } else {
                            mRefreshHeaderView.setRefreshStatus(RefreshHeaderView.PullDownRefreshStatus.PULL_DOWN_REFRESH);
                        }
                    } else {
                        scrollTo(0, 0);
                        mPullDownDistance = 0;
                    }
                }
            } else if (dy > 0 && !canChildScrollDown() || getScrollY() > 0) {
                if (mMode == PULL_FROM_END || mMode == BOTH) {
                    // 上拉加载
                    mPullUpDistance += dy;
                    if (mPullUpDistance > 0) {
                        // 阻尼
                        float damping = (float) (mPullUpDistance / (Math.log(mPullUpDistance) / Math.log(PULL_UP_FACTOR)));
                        damping = Math.max(0, damping);
                        damping = Math.min(damping, mLoadingFooterView.getHeight());
                        scrollTo(0, (int) damping);

                        if (damping == mLoadingFooterView.getHeight()) {
                            // 触发加载,将状态变为加载
                            mLoadingFooterView.setLoadStatus(LoadingFooterView.PullUpLoadStatus.LOADING);
                            mIsLoadingMore = true;

                            if (mRefreshListener != null) {
                                mRefreshListener.onPullUpRefresh();
                            }
                        }
                    } else {
                        scrollTo(0, 0);
                        mPullUpDistance = 0;
                    }
                }
            } else {
                if (!(!canChildScrollDown() && dy > 0)) {
                    if (Math.abs(dy) > 5) {
                        dispatchTouchEventToRefreshView(event);
                    }
                }
            }
        }
    }

    /**
     * 根据下拉刷新状态,折叠下拉刷新view
     * @param refreshStatus 下拉刷新状态
     */
    private void collapseRefreshHeaderView(final RefreshHeaderView.PullDownRefreshStatus refreshStatus) {
        int scrollToY = 0;
        if (refreshStatus == RefreshHeaderView.PullDownRefreshStatus.REFRESHING) {
            scrollToY = - mRefreshHeaderView.getHeight();
        } else if (refreshStatus == RefreshHeaderView.PullDownRefreshStatus.PULL_DOWN_REFRESH) {
            scrollToY = 0;
        }

        ValueAnimator animator = ValueAnimator.ofInt(getScrollY(), scrollToY);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                scrollTo(0, value);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsHeaderCollapseAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsHeaderCollapseAnimating = false;

                if (refreshStatus == RefreshHeaderView.PullDownRefreshStatus.REFRESHING) {
                    if (mRefreshListener != null) {
                        mRefreshListener.onPullDownRefresh();
                    }
                }
                mRefreshHeaderView.setRefreshStatus(refreshStatus);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mIsHeaderCollapseAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(200);
        animator.setInterpolator(new AccelerateInterpolator(3));
        animator.start();
    }

    /**
     * 收起上拉加载view
     */
    private void collapseLoadingFooterView() {
        if (getScrollY() != 0) {
            ValueAnimator animator = ValueAnimator.ofInt(getScrollY(), 0);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    scrollTo(0, value);
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsFooterCollapseAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsFooterCollapseAnimating = false;
                    mIsLoadingMore = false;
                    mLoadingFooterView.setLoadStatus(LoadingFooterView.PullUpLoadStatus.PULL_UP_LOAD);

                    if (mRefreshView instanceof AbsListView) {
                        ((AbsListView) mRefreshView).smoothScrollBy(100, 200);
                    } else if (mRefreshView instanceof ScrollView) {
                        ((ScrollView) mRefreshView).smoothScrollBy(100, 200);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mIsFooterCollapseAnimating = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.setDuration(300);
            animator.setInterpolator(new AccelerateInterpolator(3));
            animator.start();
        } else {
            mIsLoadingMore = false;
            mLoadingFooterView.setLoadStatus(LoadingFooterView.PullUpLoadStatus.PULL_UP_LOAD);
        }
    }

    /**
     * 创建下拉刷新view
     */
    private void createRefreshHeaderView() {
        mRefreshHeaderView = new RefreshHeaderView(getContext());
        addView(mRefreshHeaderView);
    }

    /**
     * 创建上拉加载view
     */
    private void createLoadingFooterView() {
        mLoadingFooterView = new LoadingFooterView(getContext());
        addView(mLoadingFooterView);
    }

    /**
     * 确定刷新view
     */
    private void ensureRefreshView() {
        if (mRefreshView == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mRefreshHeaderView) && !child.equals(mLoadingFooterView)) {
                    mRefreshView = child;
                    mRefreshView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                    break;
                }
            }
        }
    }

    /**
     * 测量view
     * @param targetView 目标view
     */
    private void measureView(View targetView) {
        if (targetView == null) {
            return;
        }

        LayoutParams p = targetView.getLayoutParams();
        if (p == null) {
            p = new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
        }

        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        int childWidthSpec = MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                MeasureSpec.EXACTLY);
        targetView.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * refreshView是否已经滚到最顶部
     * @return true:没有滚到最顶部
     */
    public boolean canChildScrollUp() {
        if (mRefreshView == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mRefreshView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mRefreshView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mRefreshView, -1) || mRefreshView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mRefreshView, -1);
        }
    }

    /**
     * refreshView是否已经滚到最底部
     * @return true:没有滚到最底部
     */
    public boolean canChildScrollDown() {
        if (mRefreshView == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mRefreshView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mRefreshView;
                if (absListView.getChildCount() > 0) {
                    int lastChildBottom = absListView.getChildAt(absListView.getChildCount() - 1).getBottom();
                    return absListView.getLastVisiblePosition() == absListView.getAdapter().getCount() - 1 && lastChildBottom <= absListView.getMeasuredHeight();
                } else {
                    return false;
                }

            } else {
                return ViewCompat.canScrollVertically(mRefreshView, 1) || mRefreshView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mRefreshView, 1);
        }
    }

    private void dispatchTouchEventToRefreshView(MotionEvent event) {
        if (mIsDispatchDown) {
            mRefreshView.dispatchTouchEvent(event);
        } else {
            mIsDispatchDown = true;

            MotionEvent obtain = MotionEvent.obtain(event);
            obtain.setAction(MotionEvent.ACTION_DOWN);
            mRefreshView.dispatchTouchEvent(obtain);
        }
    }

    private void scrollRefreshLayoutWithRefreshing(int dy, int scrollY) {
        int targetY = scrollY + dy;
        if (targetY < -mRefreshHeaderView.getHeight()) {
            dy = -mRefreshHeaderView.getHeight() - scrollY;
        } else if (targetY > 0) {
            dy = -scrollY;
        }
        scrollBy(0, dy);
    }

    private void scrollRefreshLayoutWithLoading(int dy, int scrollY) {
        int targetY = scrollY + dy;
        if (targetY > mLoadingFooterView.getHeight()) {
            dy = mLoadingFooterView.getHeight() - scrollY;
        } else if (targetY < 0) {
            dy = -scrollY;
        }
        scrollBy(0, dy);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mRefreshListener = listener;
    }

    public void setMode(int mode) {
        mMode = mode;
    }

    public void onRefreshComplete() {
        if (mRefreshHeaderView != null && mRefreshHeaderView.getRefreshStatus() == RefreshHeaderView.PullDownRefreshStatus.REFRESHING
                && !mIsHeaderCollapseAnimating) {
            collapseRefreshHeaderView(RefreshHeaderView.PullDownRefreshStatus.PULL_DOWN_REFRESH);
        }
        if (mIsLoadingMore && !mIsFooterCollapseAnimating) {
            mPullUpDistance = 0;
            collapseLoadingFooterView();
        }
    }
}
