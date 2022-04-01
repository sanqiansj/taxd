package com.niaobulashi.dao;

import com.niaobulashi.model.SysFileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

/**
 * @program: spring-boot-learning
 * @description: 文件
 * @author: 鸟不拉屎 https://niaobulashi.com
 * @create: 2019-07-19 22:17
 */
public interface SysFileInfoDao extends JpaRepository<SysFileInfo, Integer> {

    @Query(value = "select * FROM sys_file_info where task_name=?1",nativeQuery = true)
    List<SysFileInfo> searchByTaskName(@Param("taskName") String taskName);
}
