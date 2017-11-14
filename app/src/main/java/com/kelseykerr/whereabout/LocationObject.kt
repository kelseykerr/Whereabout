package com.kelseykerr.whereabout

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import java.util.*

/**
 * Created by kelseykerr on 11/14/17.
 */
class LocationObject(latLng: LatLng, date: Date) : Serializable {
    var latLng: LatLng = latLng
    var date: Date = date
}