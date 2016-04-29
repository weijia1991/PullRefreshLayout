# PullRefreshLayout
PullRefreshLayout是一个为android组件提供下拉刷新和上拉加载功能的轻便的自定义控件 <br><br>
![](https://github.com/weijia1991/PullRefreshLayout/blob/master/pullDown.gif) <br><br>
![](https://github.com/weijia1991/PullRefreshLayout/blob/master/pullUp.gif) <br><br>
# Usage
`dependencies {` <br>
`　　　compile 'com.wj.refresh:PullRefreshLayout:1.0.0'` <br>
`}` <br>
1.xml layout: <br>
`<com.wj.refresh.PullRefreshLayout` <br>
`　　　　android:layout_width="match_parent"` <br>
`　　　　android:layout_height="match_parent"` <br>
`　　　　srl:refreshMode="both" >` <br><br>
`　　　　<...ListView or GridView or ScrollView and more...>` <br><br>
`</com.wj.refresh.PullRefreshLayout>` <br><br>
2.Set up refresh listener:　<br>
`mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {` <br>
`　　　　@Override` <br>
`　　　　public void onPullDownRefresh() {` <br>
`　　　　　　// The drop-down refresh` <br>
`　　　　}` <br>
`　　　　@Override` <br>
`　　　　public void onPullUpRefresh() {` <br>
`　　　　　　// Pull on loading` <br>
`　　　　}` <br>
`});` <br><br>
3.Refresh to complete:  <br>
`mRefreshLayout.onRefreshComplete();`
