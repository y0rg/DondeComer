package com.example.y0rg.dondecomer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private LatLng latLong;
    private Marker estasAqui;

    private LocationManager locationManager;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(getApplicationContext(), "Actualizacion de posicion", Toast.LENGTH_LONG).show();
            //Se recupera la posicion
            latLong = new LatLng(location.getLatitude(), location.getLongitude());
            // Ponemos la camara en la posicion recuperada
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 10));

            //Ponemos un "usted esta aqui"
            estasAqui = mMap.addMarker(new MarkerOptions()
                    .position(latLong)
                    .title("Estás aqui"));

            //Una vez recuperada la posicion, ya no hace falta actualizar mas hasta nueva orden
            //locationManager.removeUpdates(this);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();

        locationManager= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //TODO se puede cambiar para que elija recuperar la posicion de otros dispositivos
        if(PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, locationListener);
        } else {
            //TODO que coño pasa si no tiene permisos, XQ NO LOS COGE (se los he dado a manubrio Settings->Apps->DondeComer->Permission)
            //requestPermissions(); ??
            Toast.makeText(getApplicationContext(), "Sin permiso", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //TODO eliminar, en el emulador no se guarda lastKnowLocation, la pongo a pelo, elimarlo
        latLong = new LatLng(37.2772453,-5.9206954);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15));
        estasAqui = mMap.addMarker(new MarkerOptions()
                .position(latLong)
                .title("Estás aqui"));
        //TODO FIN

        //Por si no tiene localizacion de requestUpdate
        Location loc = getLastKnownLocation();

        if(loc != null){
            latLong = new LatLng(loc.getLatitude(), loc.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 10));
            estasAqui = mMap.addMarker(new MarkerOptions()
                    .position(latLong)
                    .title("Estás aqui"));
        }
    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if(PackageManager.PERMISSION_GRANTED == checkSelfPermission(provider)) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }
        }
        return bestLocation;
    }

    public void buscar(View v){ //TODO pasar una posicion objeto tipo LatLng
        //Limpiamos el mapa de posibles busquedas anteriores
        mMap.clear();

        //TODO llamada al webservice, que recibe una lista de restaurantes con todos sus valores
        List<Restaurante> list = cargaEstatica();
        //iterar sobre la lista y poner los markers
        for(Restaurante rest : list) {
            float porcentajeOcupacion = (float)rest.getPlazasReservadas()/rest.getMaxPlazas();
            if(porcentajeOcupacion < 0.33) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(rest.getLatitud(), rest.getLongitud()))
                        .title(rest.getNombre())
                        .snippet("Plazas: " +rest.getPlazasReservadas()+"/" +rest.getMaxPlazas())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_green)));
            } else {
                if(porcentajeOcupacion >0.66){
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(rest.getLatitud(), rest.getLongitud()))
                            .title(rest.getNombre())
                            .snippet("Plazas: " +rest.getPlazasReservadas()+"/" +rest.getMaxPlazas())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_red)));
                } else {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(rest.getLatitud(), rest.getLongitud()))
                            .title(rest.getNombre())
                            .snippet("Plazas: " +rest.getPlazasReservadas()+"/" +rest.getMaxPlazas())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_yellow)));
                }
            }
        }

        //TODO El centro del mapa debe ser la posicion recuperada del dispositivo o la dada por el usuario
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 13));

        //Volvemos a añadir el estasAqui
        estasAqui = mMap.addMarker(new MarkerOptions()
                .position(latLong)
                .title("Estás aqui"));
    }



    //TODO ELIMINAR
    @Deprecated
    public List<Restaurante> cargaEstatica(){
        List<Restaurante> list = new ArrayList<Restaurante>();

        Restaurante rest = new Restaurante();
        rest.setMaxPlazas(20);
        rest.setPlazasReservadas(18);//0.9 rojo
        rest.setNombre("Burri Kin");
        rest.setLatitud(37.2843647);
        rest.setLongitud(-5.9327765);
        list.add(rest);

        rest = new Restaurante();
        rest.setMaxPlazas(200);
        rest.setPlazasReservadas(80); //0.04 amarillo
        rest.setNombre("McPollas");
        rest.setLatitud(37.287154);
        rest.setLongitud(-5.920713);
        list.add(rest);

        rest = new Restaurante();
        rest.setMaxPlazas(120);
        rest.setPlazasReservadas(12); //0.01 verde
        rest.setNombre("Casa Manolo");
        rest.setLatitud(37.288227);
        rest.setLongitud(-5.924299);
        list.add(rest);

        return list;
    }
}
