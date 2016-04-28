package com.refresh.demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wj.refresh.OnRefreshListener;
import com.wj.refresh.PullRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PullRefreshLayout mRefreshLayout;
    private ListView mListView;
    private List<String> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBoot();
        initData();
        initEvent();
    }

    private void initBoot() {
        mRefreshLayout = (PullRefreshLayout) findViewById(R.id.refresh_layout);
        mListView = (ListView) findViewById(R.id.lv);
        mData = new ArrayList<>();
    }

    private void initData() {
        for (int i = 0; i < 20; i++) {
            mData.add("refresh " + i);
        }

        TextView headerTv = new TextView(this);
        headerTv.setText("HeaderView");
        headerTv.setTextSize(20);
        headerTv.setGravity(Gravity.CENTER);
        headerTv.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 200));
        mListView.addHeaderView(headerTv);

        TextView footerTv = new TextView(this);
        footerTv.setText("FooterView");
        footerTv.setTextSize(20);
        footerTv.setGravity(Gravity.CENTER);
        footerTv.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, 200));
        mListView.addFooterView(footerTv);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mData);
        mListView.setAdapter(adapter);
    }

    private void initEvent() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onPullDownRefresh() {
                mHandler.sendEmptyMessageDelayed(0, 5000);
            }

            @Override
            public void onPullUpRefresh() {
                mHandler.sendEmptyMessageDelayed(0, 5000);
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Item is clicked which position is" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mRefreshLayout.onRefreshComplete();
        }
    };

}
