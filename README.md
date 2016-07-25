# DownloadDemo
Android-Service之多线程断点续传下载
#### Downloads
[link](http://note.youdao.com/)
#### Thanks
---
课程地址1--Android-Service系列之断点续传下载：[http://www.imooc.com/view/363](http://www.imooc.com/view/363)
课程地址2--Android-Service系列之多线程断点续传下载：[http://www.imooc.com/view/376](http://www.imooc.com/view/376)
#### Update
---
- No1.修改ThreadDAOImpl单例模式调用，解决暂停下载时进度写入数据库操作连接失败的问题。
- No2.追加文件信息的数据库，保存已下载文件，避免重复下载。
