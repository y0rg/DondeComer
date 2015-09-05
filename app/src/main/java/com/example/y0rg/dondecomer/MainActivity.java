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

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;
    private AutoCompleteTextView mAutocompleteView;
    private TextView mPlaceDetailsText;
    private TextView mPlaceDetailsAttribution;

    private static LatLng latLong;
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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        setContentView(R.layout.activity_main);

        setUpMapIfNeeded();

        //Inicio atuocomplete
        // Retrieve the AutoCompleteTextView that will display Place suggestions.
        mAutocompleteView = (AutoCompleteTextView)
                findViewById(R.id.inputBuscar);
        // Register a listener that receives callbacks when a suggestion has been selected
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

        // Retrieve the TextViews that will display details and attributions of the selected place.
        mPlaceDetailsText = (TextView) findViewById(R.id.place_details);
        mPlaceDetailsAttribution = (TextView) findViewById(R.id.place_attribution);

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover the entire world.
        LatLngBounds zonaBusqueda = Utils.convertCenterAndRadiusToBounds(latLong, 100);//TODO comprobar las unidades del radio
        mAdapter = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, zonaBusqueda, null);
        mAutocompleteView.setAdapter(mAdapter);
        //FIN autocomplete


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

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(PlaceBuffer places) {
                            if (places.getStatus().isSuccess()) {
                                final Place myPlace = places.get(0);
                                //Actualizamos la LatLong del centro del mapa
                                latLong = myPlace.getLatLng();
                            }
                            places.release();
                        }
                    });

            Toast.makeText(getApplicationContext(), "Clicked: " + latLong, Toast.LENGTH_SHORT).show();

        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            // Format details of the place for display and show it in a TextView.
            mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
                    place.getId(), place.getAddress(), place.getPhoneNumber(),
                    place.getWebsiteUri()));

            // Display the third party attributions if set.
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
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        //Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "+ connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this, "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
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
        //Variable para guardar el numero de restaurantes encontrados que no estan llenos
        int libres =0;
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
            if(rest.getPlazasReservadas()!=rest.getMaxPlazas()){
                libres++;
            }
        }

        //El centro del mapa debe ser la posicion recuperada del dispositivo o la dada por el usuario
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 12));

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
        rest.setPlazasReservadas(20);//0.9 rojo
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
