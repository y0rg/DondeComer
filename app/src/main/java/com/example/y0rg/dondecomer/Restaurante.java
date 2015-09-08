package com.example.y0rg.dondecomer;

/**
 * Clase de objetos restaurantes que se recuperaran tras una llamada al webservice
 * Una vez tengamos la lista de restaurantes, se mostraran en markers sobre el mapa
 */
public class Restaurante {
    private double latitud;
    private double longitud;

    private String nombre;
    private int maxPlazas;

    private int plazasReservadas;


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

    public int getPlazasReservadas() {
        return plazasReservadas;
    }

    public void setPlazasReservadas(int plazasReservadas) {
        this.plazasReservadas = plazasReservadas;
    }
}
