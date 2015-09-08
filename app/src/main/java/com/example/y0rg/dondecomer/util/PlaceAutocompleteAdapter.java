/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.y0rg.dondecomer.util;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Clase para el manejo del input  que se rellena con las localizaciones "Places" sugeridas por la api de Google
 *
 */
public class PlaceAutocompleteAdapter
        extends ArrayAdapter<PlaceAutocompleteAdapter.PlaceAutocomplete> implements Filterable {

    /**
     * Resultados devueltos por el adaptador
     */
    private ArrayList<PlaceAutocomplete> mResultList;

    /**
     * Instancia de la api de Google que devuelve los resultados
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Los limites de zona en los que se buscan los resultados sugeridos
     */
    private LatLngBounds mBounds;

    /**
     * Un filtro por si se quiere indicar que solo muestre un tipo concreto de localizacion (Plazas por ejemplo)
     */
    private AutocompleteFilter mPlaceFilter;

    /**
     * Inicializa el objeto de la clase
     */
    public PlaceAutocompleteAdapter(Context context, int resource, GoogleApiClient googleApiClient,
                                    LatLngBounds bounds, AutocompleteFilter filter) {
        super(context, resource);
        mGoogleApiClient = googleApiClient;
        mBounds = bounds;
        mPlaceFilter = filter;
    }

    /**
     * Establece los "limites" para las consultas
     */
    public void setBounds(LatLngBounds bounds) {
        mBounds = bounds;
    }

    /**
     * Devuelve el numero de resultados obtenidos
     */
    @Override
    public int getCount() {
        return mResultList.size();
    }

    /**
     * Devuelve uno de los resultados pasando en el parametro su posicion
     */
    @Override
    public PlaceAutocomplete getItem(int position) {
        return mResultList.get(position);
    }

    /**
     * Devuelve el filtro utilizado para las busqueda en caso de haber establecido uno
     */
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                // No busca si no existe restriccion
                if (constraint != null) {
                    // Se piden resultados
                    mResultList = getAutocomplete(constraint);
                    if (mResultList != null) {
                        // Si ha habido resultados....
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    // Si devuelve al menos un resultado, se actualizan los datos
                    notifyDataSetChanged();
                } else {
                    // Si no se han obetnido resultados, se invalidan los datos
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    /**
     * Recupera las localizacions, "places" de la API Places Geo Data
     * Los resultados se devuelven como objetos del tipo
     * {@link com.example.y0rg.dondecomer.util.PlaceAutocompleteAdapter.PlaceAutocomplete}
     * que almacenan un id, por el cual luego se puede recuperar toda la informacion de la "places"
     * a traves de la API
     *
     * Devuelve una lista vacia si no hay resultados
     * Devuelve NULL si la API no esta disponible, o la consulta no se recibe correctamente
     *
     * Hay que llamar a este metodo en el MainActivity, necesita acceso/permisos a NETWORK para recuperar los datos
     *
     * @param constraint Restriccion de busqueda del Autocomplete
     * @return Resultados de la API para el autocomplete, null si no encuentra
     * @see Places#GEO_DATA_API#getAutocomplete(CharSequence)
     */
    private ArrayList<PlaceAutocomplete> getAutocomplete(CharSequence constraint) {
        if (mGoogleApiClient.isConnected()) {
            // Envia la consulta a la api y se queda "a la espera" de resultados
            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi
                            .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                                    mBounds, mPlaceFilter);

            // LLama al metodo y espera a los resultados hasta 60 segundos
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(60, TimeUnit.SECONDS);

            // Comprueba si la respuesta es correcta, en caso contrario devuelve null
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Toast.makeText(getContext(), "Error conectando con la API: " + status.toString(),
                        Toast.LENGTH_SHORT).show();
                autocompletePredictions.release();
                return null;
            }

            //Se encapsulan los resultados recuperados por la API en objetos con la ID y la descripcion

            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                //Por cada resultado, cogemos la descripcion y la guardamos en la lista a mostrar
                resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                        prediction.getDescription()));
            }

            // Limpiamos el buffer de predicciones
            autocompletePredictions.release();

            return resultList;
        }
        return null;
    }

    /**
     * Contenedor para almacenar los resultados obtenidos de la API Place Geo Data Autocomplete
     */
    public class PlaceAutocomplete {

        public CharSequence placeId;
        public CharSequence description;

        PlaceAutocomplete(CharSequence placeId, CharSequence description) {
            this.placeId = placeId;
            this.description = description;
        }

        @Override
        public String toString() {
            return description.toString();
        }
    }
}
