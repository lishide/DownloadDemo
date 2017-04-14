# DownloadDemo
Android-Service之多线程断点续传下载
### Downloads
[Demo](https://github.com/lishide/DownloadDemo/raw/master/art/app-debug.apk)
### Screenshot
![](https://github.com/lishide/DownloadDemo/raw/master/art/Screenshot_2016-07-26-10-30-06.png "界面截图") 
### Thanks
1. Android-Service系列之断点续传下载：[http://www.imooc.com/view/363](http://www.imooc.com/view/363)
1. Android-Service系列之多线程断点续传下载：[http://www.imooc.com/view/376](http://www.imooc.com/view/376)

### Update
- 修改ThreadDAOImpl单例模式调用，解决暂停下载时进度写入数据库操作连接失败的问题。
- 追加文件信息的数据库，保存已下载文件，避免重复下载。
