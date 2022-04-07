package com.niaobulashi.dao;


import com.niaobulashi.model.TaskInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 *  task jpa
 */
public interface TaskInfoDao extends JpaRepository<TaskInfo, Integer> {

    @Query(value = "select * FROM task_info where name=?1",nativeQuery = true)
    TaskInfo findbyFile(String taskName);
}
