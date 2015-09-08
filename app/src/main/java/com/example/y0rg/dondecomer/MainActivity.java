package com.example.y0rg.dondecomer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.y0rg.dondecomer.util.PlaceAutocompleteAdapter;
import com.example.y0rg.dondecomer.util.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap; // Ojo que este activa la api de Google PLay

    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;
    private AutoCompleteTextView mAutocompleteView;
    private TextView mPlaceDetailsText;
    private TextView mPlaceDetailsAttribution;

    private static LatLng latLong;
    private Marker estasAqui; //Por si hay que recuperar o mover el marker de "Estas aqui". posible mejora para actualizar en tiempo real

    private LocationManager locationManager;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(getApplicationContext(), "Actualizacion de posicion", Toast.LENGTH_LONG).show();
            //Se recupera la posicion
            latLong = new LatLng(location.getLatitude(), location.getLongitude());
            // Ponemos la camara en la posicion recuperada
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 10));

            //Ponemos un "Estas aqui"
            estasAqui = mMap.addMarker(new MarkerOptions()
                    .position(latLong)
                    .title("Estás aqui"));

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
        //Al crear la actividad, inicializamos la api de Google Places.GEO_DATA_API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        //Asignamos el layout a la actividad
        setContentView(R.layout.activity_main);

        //Si aun no esta inicalizado, inicializamos el mapa
        setUpMapIfNeeded();

        //Inicio atuocomplete
        // Recuperamos el input que mostrara las sugerencias de busqueda
        mAutocompleteView = (AutoCompleteTextView) findViewById(R.id.inputBuscar);

        // Registramos un listener en el input para cuando se seleccione un elemento de la lista ejecutar una accion
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

        // Recuperamos un textview donde mostrar informacion detallada de la busqueda
        mPlaceDetailsText = (TextView) findViewById(R.id.place_details);
        mPlaceDetailsAttribution = (TextView) findViewById(R.id.place_attribution);

        // Limitamos las sugerencias a un radio de accion, para que no muestre sugerencias de todo el mundo
        LatLngBounds zonaBusqueda = Utils.convertCenterAndRadiusToBounds(latLong, 100);
        mAdapter = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, zonaBusqueda, null);
        mAutocompleteView.setAdapter(mAdapter);
        //FIN autocomplete


        locationManager= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //TODO se puede cambiar para que elija recuperar la posicion de otros dispositivos
        // Comprobamos que la aplicacion tiene los permisos de acceso a la localizacion del dispositivo
        if(PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, locationListener);
        } else {
            Toast.makeText(getApplicationContext(), "Sin permiso", Toast.LENGTH_LONG).show();
        }
    }

    /**
        Este metodo se ejecuta cuando pulsamos un item de la lista de sugerencias de busqueda
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            //Recuperamos el id del la sugerencia de localizacion seleccioanda
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(PlaceBuffer places) {
                            if (places.getStatus().isSuccess()) {
                                final Place myPlace = places.get(0);
                                //Actualizamos la LatLong del centro del mapa con la de la sugerencia seleccionada
                                latLong = myPlace.getLatLng();
                            }
                            places.release();
                        }
                    });
            //Mostramos un pequeño mensaje de comprobacion con las nuevas coordenadas
            Toast.makeText(getApplicationContext(), "Clicked: " + latLong, Toast.LENGTH_SHORT).show();

        }
    };

    /**
     * Metodo que recupera la lista de sugerencias al ir escrbiendo en el inputtext de busqueda
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();
                return;
            }
            // Se recupera la "Place" localizacion
            final Place place = places.get(0);

            // Recuperamos una serie de datos de la localizacion por si son de interes
            mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
                    place.getId(), place.getAddress(), place.getPhoneNumber(),
                    place.getWebsiteUri()));

            // Si hay informacion de "terceros" tambien se puede mostrar
            final CharSequence thirdPartyAttribution = places.getAttributions();
            if (thirdPartyAttribution == null) {
                mPlaceDetailsAttribution.setVisibility(View.GONE);
            } else {
                mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
                mPlaceDetailsAttribution.setText(Html.fromHtml(thirdPartyAttribution.toString()));
            }
            places.release();
        }
    };

    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {

        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber, websiteUri));

    }

    /**
     * Metodo que se llama cuando no se puede conectar con el servicio de GooglePlay
     *
     * @param connectionResult  devolve el motivo del fallo de la conexion
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        // TODO tratamiento del error y acciones en consecuencia, solo se esta mostrando el error en pantalla
        Toast.makeText(this, "Could not connect to Google API Client: Error "
                        + connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Metodo al que se llama en caso de volver de estar en segundo plano la app
     */
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    private void setUpMapIfNeeded() {
        // Comprobamos si no esta instanciado el mapa
        if (mMap == null) {
            // Si no lo esta, intentamos recuperar del fragment de la actividad
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Comprobamos si lo hemos podido recuperar y los "levantamos"
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * Metodo que se llama una vez tenemos la instancia de Google Maps
     * Es la primera carga que se hace, por lo que recogemos la posicion actual del dispositivo,
     * centramos el mapa y añadimos la marca "Estas aqui"
     */
    private void setUpMap() {
        //TODO eliminar, en el emulador no se guarda lastKnowLocation, la pongo a hardcode, elimarlo
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

    //Metodo para recuperar la ulitma localizacion guardada por el dispositivo.
    //Se ejecuta en caso de no poder recuperar la posicion actual
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
                    bestLocation = l;
                }
            }
        }
        return bestLocation;
    }

    public void buscar(View v){
        //Limpiamos el mapa de posibles busquedas anteriores
        mMap.clear();

        //TODO llamada al webservice, que recibe una lista de restaurantes con todos sus valores
        List<Restaurante> list = cargaEstatica();

        //iterar sobre la lista y poner los markers de los restaurantes
        for(Restaurante rest : list) {
            float porcentajeOcupacion = (float)rest.getPlazasReservadas()/rest.getMaxPlazas();
            if(porcentajeOcupacion < 0.33) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(rest.getLatitud(), rest.getLongitud()))
                        .title(rest.getNombre())
                        .snippet("Plazas: " +rest.getPlazasReservadas()+"/" +rest.getMaxPlazas())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_green)));
            } else {
                if(porcentajeOcupacion >0.66 ){
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

        //El centro del mapa debe ser la posicion recuperada del dispositivo o la dada por el usuario
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 12));

        //Volvemos a añadir el estasAqui
        estasAqui = mMap.addMarker(new MarkerOptions()
                .position(latLong)
                .title("Estás aqui"));
    }



    //TODO ELIMINAR CUANDO SE RECUPEREN LOS DATOS DEL WEBSERVICE
    @Deprecated
    public List<Restaurante> cargaEstatica(){
        List<Restaurante> list = new ArrayList<Restaurante>();

        Restaurante rest = new Restaurante();
        rest.setMaxPlazas(20);
        rest.setPlazasReservadas(20);//0.9 rojo
        rest.setNombre("Burguer King");
        rest.setLatitud(37.3908891);
        rest.setLongitud(-5.9748133);
        list.add(rest);

        rest = new Restaurante();
        rest.setMaxPlazas(200);
        rest.setPlazasReservadas(80); //0.04 amarillo
        rest.setNombre("McDonalds");
        rest.setLatitud(37.3912703);
        rest.setLongitud(-5.9752772);
        list.add(rest);

        rest = new Restaurante();
        rest.setMaxPlazas(120);
        rest.setPlazasReservadas(12); //0.01 verde
        rest.setNombre("Casa Manolo");
        rest.setLatitud(37.3896175);
        rest.setLongitud(-5.9804919);
        list.add(rest);

        return list;
    }
}
