package com.example.y0rg.dondecomer;

import com.example.y0rg.dondecomer.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

/**
 * Activididad para mostrar el logo de la app a modo de carga
 */
public class FullscreenActivity extends Activity {
    /**
     * Indica si se muestra la interfaz de botones
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * Indica durante cuanto tiempo se oculta la interfaz
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Si se muestra la interfaz al tocar la pantalla
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * El flag que se le pasa al sistema para que muestre o no la interfaz de navegacion
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * TInstancia del UI del sistema en la actividad
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //Cambiamos de actividad con un retraso de 2 segundos, para que se vao la imagen como de carga
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        }, 2000);
    }


    /**
     * Listener para ocultar/mostrar interfaz al tocar la pantalla
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Programacion de ocultar en caso de que se llamara previamente, se cancela la accion
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
