package com.lzq.tuliaoand.bean;

import org.osmdroid.util.GeoPoint;

public class UserBean {

    private String userId;
    private GeoPoint location;

    public UserBean() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}
