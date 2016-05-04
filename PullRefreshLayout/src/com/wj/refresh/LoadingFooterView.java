package com.wj.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 上拉加载视图
 * Created by jia.wei on 16/3/28.
 */
public class LoadingFooterView extends FrameLayout {

    private ProgressBar mLoadingPb;
    private TextView mLoadLabelTv;
    private PullUpLoadStatus mLoadStatus = PullUpLoadStatus.PULL_UP_LOAD;

    public LoadingFooterView(Context context) {
        this(context, null);
    }

    public LoadingFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_pull_up_loading, this, true);

        mLoadingPb = (ProgressBar) findViewById(R.id.pb_loading);
        mLoadLabelTv = (TextView) findViewById(R.id.tv_load_label);
    }

    private void update() {
        if (mLoadStatus == PullUpLoadStatus.PULL_UP_LOAD) {
            mLoadingPb.setVisibility(GONE);
            mLoadLabelTv.setText(getResources().getString(R.string.pull_up_load_label));
        } else if (mLoadStatus == PullUpLoadStatus.LOADING) {
            mLoadingPb.setVisibility(VISIBLE);
            mLoadLabelTv.setText(getResources().getString(R.string.pull_up_loading_label));
        }
    }

    public void setLoadStatus(PullUpLoadStatus loadStatus) {
        mLoadStatus = loadStatus;
        update();
    }

    public PullUpLoadStatus getLoadStatus() {
        return mLoadStatus;
    }

    /**
     * 上拉加载状态
     */
    public enum PullUpLoadStatus {
        /** 上拉加载 */
        PULL_UP_LOAD,
        /** 正在加载 */
        LOADING
    }

}
