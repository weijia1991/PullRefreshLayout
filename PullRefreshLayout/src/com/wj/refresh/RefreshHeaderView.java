package com.wj.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nineoldandroids.animation.ObjectAnimator;

/**
 * 下拉刷新视图
 * Created by jia.wei on 16/3/28.
 */
public class RefreshHeaderView extends FrameLayout {

    private ImageView mPullArrowIv = null;
    private ProgressBar mRefreshingPb = null;
    private TextView mRefreshLabelTv = null;

    private PullDownRefreshStatus mRefreshStatus = PullDownRefreshStatus.PULL_DOWN_REFRESH;

    public RefreshHeaderView(Context context) {
        this(context, null);
    }

    public RefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_pull_down_refreshing, this, true);

        mPullArrowIv = (ImageView) findViewById(R.id.iv_pull_arrow);
        mRefreshingPb = (ProgressBar) findViewById(R.id.pb_refreshing);
        mRefreshLabelTv = (TextView) findViewById(R.id.tv_refresh_label);
    }

    /**
     * 更新下拉图标
     * @param refreshStatus {@link PullDownRefreshStatus}
     */
    private void updatePullIcon(PullDownRefreshStatus refreshStatus) {
        if (mPullArrowIv != null) {
            if (refreshStatus != mRefreshStatus) {
                if (refreshStatus == PullDownRefreshStatus.RELEASE_REFRESH) {
                    mPullArrowIv.setVisibility(VISIBLE);
                    mRefreshingPb.setVisibility(INVISIBLE);

                    ObjectAnimator.ofFloat(mPullArrowIv, "rotation", 0, -180)
                            .setDuration(150)
                            .start();
                } else if (refreshStatus == PullDownRefreshStatus.PULL_DOWN_REFRESH) {
                    mPullArrowIv.setVisibility(VISIBLE);
                    mRefreshingPb.setVisibility(INVISIBLE);

                    ObjectAnimator.ofFloat(mPullArrowIv, "rotation", -180, 0)
                            .setDuration(150)
                            .start();
                } else if (refreshStatus == PullDownRefreshStatus.REFRESHING) {
                    mPullArrowIv.setVisibility(INVISIBLE);
                    mRefreshingPb.setVisibility(VISIBLE);
                }
            }
        }
    }

    /**
     * 跟新下拉刷新的标签
     * @param refreshStatus {@link PullDownRefreshStatus}
     */
    private void updateRefreshLabel(PullDownRefreshStatus refreshStatus) {
        if (mRefreshLabelTv != null) {
            if (refreshStatus == PullDownRefreshStatus.RELEASE_REFRESH) {
                mRefreshLabelTv.setText(getContext().getResources().getString(R.string.pull_down_release_label));
            } else if (refreshStatus == PullDownRefreshStatus.PULL_DOWN_REFRESH) {
                mRefreshLabelTv.setText(getContext().getResources().getString(R.string.pull_down_refresh_label));
            } else if (refreshStatus == PullDownRefreshStatus.REFRESHING) {
                mRefreshLabelTv.setText(getContext().getResources().getString(R.string.pull_down_refreshing_label));
            }
        }
    }

    public void setRefreshStatus(PullDownRefreshStatus refreshStatus) {
        updatePullIcon(refreshStatus);
        updateRefreshLabel(refreshStatus);

        mRefreshStatus = refreshStatus;
    }

    public PullDownRefreshStatus getRefreshStatus() {
        return mRefreshStatus;
    }

    /**
     * 下拉刷新状态
     */
    public enum PullDownRefreshStatus {
        /** 释放刷新 */
        RELEASE_REFRESH,
        /** 下拉刷新 */
        PULL_DOWN_REFRESH,
        /** 正在刷新 */
        REFRESHING
    }

}
