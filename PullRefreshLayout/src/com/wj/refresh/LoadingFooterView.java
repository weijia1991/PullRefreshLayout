package com.wj.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

/**
 * 上拉加载视图
 * Created by jia.wei on 16/3/28.
 */
public class LoadingFooterView extends FrameLayout {

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
    }

}
