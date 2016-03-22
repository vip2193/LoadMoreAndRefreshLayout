package com.example.sampellistview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.model.loadmoreandrefresh.LoadMoreAndRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        LoadMoreAndRefreshLayout.OnLoadListener, OnRefreshListener {

    private ListView mListView;
    private List<String> mData;
    private Button emptyButton, errorButton;
    private LoadMoreAndRefreshLayout refreshLayout;
    private ArrayAdapter adapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:    //下拉刷新
                    mData.add(0, "下拉刷新数据");
                    adapter.notifyDataSetChanged();
                    refreshLayout.setRefreshing(false);
                    break;
                case 2:   //下拉加载
                    mData.add("上拉加载数据");
                    adapter.notifyDataSetChanged();
                    refreshLayout.setLoading(false);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        refreshLayout = (LoadMoreAndRefreshLayout) findViewById(R.id.myRefresh);
        mListView = (ListView) findViewById(R.id.mlistview);
        emptyButton = (Button) findViewById(R.id.emptyButton);
        errorButton = (Button) findViewById(R.id.errorButton);
        initData();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mData);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setOnLoadListener(this);
        errorButton.setOnClickListener(this);
        emptyButton.setOnClickListener(this);
        mListView.setAdapter(adapter);

    }

    private void initData() {
        mData = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            mData.add("测试数据，测试数据" + i);
        }

    }

    @Override
    public void onClick(View v) {
        mData.removeAll(mData);
        adapter.notifyDataSetChanged();
        switch (v.getId()) {
            case R.id.emptyButton:
                refreshLayout.setEmptyView("这里好像什么也没有", false, 0);
                break;
            case R.id.errorButton:
                refreshLayout.setEmptyView(true);
                //可以使用重新获取按钮点击事件来让用户重新获取信息
                refreshLayout.setGetAgainButtonListener(onClickListener);
                break;

        }
    }

    //上拉加载
    @Override
    public void onLoad() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i =1;
                    Thread.sleep(1500);
                    handler.sendEmptyMessage(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //下拉刷新
    @Override
    public void onRefresh() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i =1;
                    Thread.sleep(1500);
                    handler.sendEmptyMessage(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * "重新获取"点击事件
     */
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < 20; i++) {
                mData.add("测试数据，测试数据" + i);
            }
            adapter.notifyDataSetChanged();
            //当  data的长度不为0时会隐藏emptyView
            refreshLayout.setEmptyView("",false,mData.size());
        }
    };
}
