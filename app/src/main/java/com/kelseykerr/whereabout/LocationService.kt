package com.kelseykerr.whereabout

import android.content.Context
import android.location.Location
import android.util.Log
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.kelseykerr.whereabout.models.LocationObject
import java.util.*


/**
 * Created by kelseykerr on 11/14/17.
 */
class LocationService(private var context: Context) {

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

    private fun onLocationChanged(location: Location) {
        Log.d(TAG, "New Location point: " + java.lang.Double.toString(location.latitude) + "," +
                java.lang.Double.toString(location.longitude))
        val latLng = LatLng(location.latitude, location.longitude)
        val locObject = LocationObject(latLng.latitude, latLng.longitude, Date())
        val storage = context.getSharedPreferences(Utils.LOCATION_STORAGE_NAME, 0)
        val locObjects = storage.getString(Utils.LOCATION_STORAGE_KEY, null)
        val mapper = jacksonObjectMapper()
        val locObjectList: MutableList<LocationObject>
        if (locObjects != null) {
            locObjectList = mapper.readValue<MutableList<LocationObject>>(locObjects, object : TypeReference<MutableList<LocationObject>>() {})
        } else {
            locObjectList = mutableListOf()
        }
        if (locObjectList != null && locObjectList.size >= 100) {
            locObjectList.removeAt(0)
        }
        locObjectList.add(locObject)
        val editor = storage.edit()
        val newLocObjString = mapper.writeValueAsString(locObjectList)
        editor.putString(Utils.LOCATION_STORAGE_KEY, newLocObjString)
        editor.apply()
    }
}