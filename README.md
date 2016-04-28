# PullRefreshLayout
PullRefreshLayout是一个为android组件提供下拉刷新和上拉加载功能的轻便的自定义控件
# Usage
1.xml layout: <br><br>

2.Set up refresh listener:　<br><br>
`mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {` <br><br>
            `@Override` <br><br>
            `public void onPullDownRefresh() {` <br><br>
                `// The drop-down refresh` <br><br>
            `}` <br><br>
            `@Override` <br><br>
            `public void onPullUpRefresh() {` <br><br>
                `// Pull on loading` <br><br>
            `}` <br><br>
        `});` <br><br>
3.Refresh to complete:  <br><br>
`mRefreshLayout.onRefreshComplete();`
