package com.kelseykerr.whereabout

import java.io.Serializable

/**
 * Created by kelseykerr on 11/15/17.
 */
class SavedPlace : Serializable {

    lateinit var name:String

    lateinit var address:String

    var lat:Double = 0.0

    var lng:Double = 0.0

    var showOnMap:Boolean = true

}