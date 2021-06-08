package com.dayouzc.efk.logcenter.Test;

/**
 * @author FanJiangFeng
 * @version 1.0.0
 * @ClassName User.java
 * @Description TODO
 * @createTime 2021年06月08日 10:06:00
 */
public class User {
    private String age;
    private String name;
    private String sex;
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public User(String age, String name, String sex) {
        this.age = age;
        this.name = name;
        this.sex = sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }



}
