package com.kelseykerr.whereabout

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import java.util.*

/**
 * Created by kelseykerr on 11/14/17.
 */
class LocationObject(public var latLng: LatLng = LatLng(0.0,0.0), public var date: Date = Date()) : Serializable