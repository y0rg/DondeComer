package com.example.y0rg.dondecomer;

/**
 * Created by y0rg on 29/08/2015.
 */
public class Restaurante {
    private double latitud;
    private double longitud;

    private String nombre;
    private int maxPlazas;

    //TODO faltan mas atributos??, preguntar??


    public int getMaxPlazas() {
        return maxPlazas;
    }

    public void setMaxPlazas(int maxPlazas) {
        this.maxPlazas = maxPlazas;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getLongitud() {

        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getLatitud() {

        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }
}
