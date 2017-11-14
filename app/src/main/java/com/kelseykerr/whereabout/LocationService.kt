package com.kelseykerr.whereabout

import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.*


/**
 * Created by kelseykerr on 11/14/17.
 */
class LocationService(context: Context) {

    var context: Context = context

    companion object {
        const val TAG = "LocationService"
    }

    private val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun getLocation() {
        try {
            mFusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.d(TAG, "Error trying to get last GPS location")
                        e.printStackTrace()
                    }
        } catch (e: SecurityException) {
            Log.d(TAG, "Didn't get updated location because we don't have sufficient permissions")
        }
    }

    fun onLocationChanged(location: Location) {
        Log.d(TAG, "Updated Location: " + java.lang.Double.toString(location.latitude) + "," +
                java.lang.Double.toString(location.longitude))
        val latLng = LatLng(location.latitude, location.longitude)
        val locObject = LocationObject(latLng, Date())
        //TODO: store ordered list of location objects, check if length > 100 and
        // remove oldest if so, then write new location object
        val storage = context.getSharedPreferences(Utils.LOCATION_STORAGE_NAME, 0)
        val locObjects = storage.getStringSet(Utils.LOCATION_STORAGE_KEY, HashSet())

    }
}