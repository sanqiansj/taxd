package com.niaobulashi.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author y
 * @date 2022/3/18
 */
public class NewTask {


    public static void main(String[] args) {
        String name = newName("沙和尚公司");
        System.out.println(name);
    }

    public static String newName(String name) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String newName = "任务" + '〔' + localDateTime.getYear() + '〕' + localDateTime.format(DateTimeFormatter.ofPattern("MMdd")) + "-" + name;
        return newName;
    }
}
