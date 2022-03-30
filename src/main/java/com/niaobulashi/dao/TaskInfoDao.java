package com.niaobulashi.dao;


import com.niaobulashi.model.TaskInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *  task jpa
 */
public interface TaskInfoDao extends JpaRepository<TaskInfo, Integer> {

}
