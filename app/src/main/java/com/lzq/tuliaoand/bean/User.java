package com.lzq.tuliaoand.bean;

import androidx.annotation.Nullable;

import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class User {

    private String email;
    private String broadcast;
    private double lng;
    private double lat;

    private List<Conversation> conversations;

    private Marker marker;//地图上的

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(String broadcast) {
        this.broadcast = broadcast;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return email.equals(((User) obj).getEmail());
    }
}
