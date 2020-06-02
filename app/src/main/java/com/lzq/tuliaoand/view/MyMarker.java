package com.lzq.tuliaoand.view;

import android.content.Context;
import android.graphics.Canvas;

import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;

public class MyMarker extends Marker {
    public MyMarker(MapView mapView) {
        super(mapView);
    }

    public MyMarker(MapView mapView, Context resourceProxy) {
        super(mapView, resourceProxy);
    }

}
