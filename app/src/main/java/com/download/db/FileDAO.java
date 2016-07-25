package com.download.db;

import com.download.entity.FileInfo;

import java.util.List;

/**
 * use for 数据访问接口
 */
public interface FileDAO {
    /**
     * 插入文件信息
     *
     * @param fileInfo 文件信息
     */
    void insertFile(FileInfo fileInfo);

    /**
     * 删除文件信息
     *
     * @param url 地址
     */
    void deleteFile(String url);

    /**
     * 查询文件信息
     *
     * @param url 地址
     * @return 信息
     */
    List<FileInfo> getFile(String url);

}
