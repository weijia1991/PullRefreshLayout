package com.wj.refresh;

/**
 * 刷新接口
 * Created by jia.wei on 16/3/28.
 */
public interface OnRefreshListener {
    /**
     * 下拉刷新回调
     */
    void onPullDownRefresh();
    /**
     * 上拉加载更多回调
     */
    void onPullUpRefresh();
}
