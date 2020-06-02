package com.lzq.tuliaoand.view;

import android.view.View;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class MyInfoWindow extends InfoWindow {



    public MyInfoWindow(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
    }

    public MyInfoWindow(View v, MapView mapView) {
        super(v, mapView);
    }

    @Override
    public void onOpen(Object item) {

    }

    @Override
    public void onClose() {

    }
}
