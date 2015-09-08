package com.example.y0rg.dondecomer.util;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

/**
 * Clase con metodo utiles estaticos que seran usados en toda la app
 */
public class Utils {

    //Metodo que convierte una Latitutud/longitud y su radio (circulo) en un LatLngBound que se compone de dos coordenadas
    public static LatLngBounds convertCenterAndRadiusToBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }
}
