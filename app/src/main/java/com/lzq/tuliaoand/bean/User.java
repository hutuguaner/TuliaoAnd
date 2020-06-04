package com.lzq.tuliaoand.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.SPUtils;
import com.lzq.tuliaoand.common.SPKey;
import com.stfalcon.chatkit.commons.models.IUser;

import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class User implements IUser, Parcelable {

    private String email;
    private String broadcast;
    private double lng;
    private double lat;

    public User() {
    }

    protected User(Parcel in) {
        email = in.readString();
        broadcast = in.readString();
        lng = in.readDouble();
        lat = in.readDouble();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (email == null) return false;
        if (obj == null) return false;
        return email.equals(((User) obj).getEmail());
    }

    @Override
    public String getId() {
        String myEmail = SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName());
        if (myEmail.equals(email)) {
            return "1";
        } else {
            return "2";
        }
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public String getAvatar() {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(email);
        parcel.writeString(broadcast);
        parcel.writeDouble(lng);
        parcel.writeDouble(lat);
    }
}
