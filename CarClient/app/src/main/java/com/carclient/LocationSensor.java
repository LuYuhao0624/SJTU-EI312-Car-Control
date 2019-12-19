package com.carclient;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public abstract class LocationSensor {
    private LocationListener location_listener;
    private LocationManager location_manager;
    private Context main_context;
    private boolean update;
    private String provider;
    private double last_saved_latitude;
    private double last_saved_longitude;

    LocationSensor(Context context, WifiClient wifi) {
        main_context = context;
        location_manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setCostAllowed(true);
        provider = location_manager.getBestProvider(criteria, true);
        if (ContextCompat.checkSelfPermission(main_context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location start_location = location_manager.getLastKnownLocation(provider);
            location_listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    onLocationChange();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            location_manager.requestLocationUpdates(provider, 500, 0, location_listener);
            update = true;
        } else
            Toast.makeText(main_context, "Do not have location access.", Toast.LENGTH_SHORT).show();
    }

    public void requestUpdate() {
        try {
            if (!update) {
                location_manager.requestLocationUpdates(provider, 500, 0, location_listener);
                update = true;
            }
        }
        catch (SecurityException se) {
            if (update)
                Toast.makeText(main_context, "No access to request location update. Status: updating.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(main_context, "No access to request location update. Status: not updating.", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeUpdate() {
        try {
            if (update) {
                location_manager.removeUpdates(location_listener);
                update = false;
            }
        }
        catch (SecurityException se) {
            if (update)
                Toast.makeText(main_context, "No access to remove location update. Status: updating.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(main_context, "No access to remove location update. Status: not updating.", Toast.LENGTH_SHORT).show();
        }
    }

    public abstract void onLocationChange();

    private void saveCheckpoint() {
        try {
            Location location = location_manager.getLastKnownLocation(provider);
            last_saved_latitude = location.getLatitude();
            last_saved_longitude = location.getLongitude();
        } catch (SecurityException se) {
            Toast.makeText(main_context, "No access to make location checkpoint.", Toast.LENGTH_SHORT).show();
        }
    }
}
