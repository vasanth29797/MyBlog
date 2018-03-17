package com.elite.blog;

/**
 * Created by evk29 on 14-01-2018.
 */

public class Blog {

    String title, desc, image, uname, pp, time;

    public Blog() {

    }

    public Blog(String title, String desc, String image, String uname, String pp, String time) {
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.uname = uname;
        this.pp = pp;
        this.time = time;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getPp() {
        return pp;
    }

    public void setPp(String pp) {
        this.pp = pp;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
