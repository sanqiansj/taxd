package com.niaobulashi.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * 任务表
 */
@Data
@Entity
@Table(name = "task_info")
public class TaskInfo implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String time;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private Integer status;


    @Column(nullable = false)
    private Integer type;

}
