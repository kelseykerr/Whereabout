package com.kelseykerr.whereabout

import android.Manifest
import android.app.job.JobScheduler
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.kelseykerr.whereabout.models.LocationObject
import com.kelseykerr.whereabout.models.SavedPlace
import com.wirelessregistry.observersdk.observer.ObserverJobService
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.app_bar_map.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback, AdapterView.OnItemClickListener {

    companion object {
        const val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 11
        const val TAG = "MapActivity"
        lateinit var savedPlaces: MutableList<SavedPlace>
    }

    private lateinit var savedPlacesList: ListView
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            if (savedPlaces.size >= 20) {
                Snackbar.make(view, "Max places reached.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
            } else {
                try {
                    LocationServices.getFusedLocationProviderClient(this).lastLocation
                            .addOnSuccessListener { location ->
                                // GPS location can be null if GPS is switched off
                                if (location != null) {
                                    val newPlaceDialog = NewPlaceDialogFragment.newInstance(location.latitude, location.longitude)
                                    newPlaceDialog.show(fragmentManager, "dialog")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.d(LocationService.TAG, "Error trying to get last GPS location")
                                e.printStackTrace()
                            }
                } catch (e: SecurityException) {
                    Log.d(LocationService.TAG, "Didn't get updated location because we don't have sufficient permissions")
                }
            }
        }
        menu_icon.setOnClickListener{_ ->
            drawer_layout.openDrawer(Gravity.START)
        }

        //val toggle = ActionBarDrawerToggle( this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        //drawer_layout.addDrawerListener(toggle)
        //toggle.syncState()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        checkPermissions()
        getSavedLocations()
        initWRScans()
    }

    private fun initWRScans() {
        val settings = getSharedPreferences(com.wirelessregistry.observersdk.data.Settings.PREFS_NAME, 0)
        val editor = settings.edit()
        editor.putString("tag", BuildConfig.WR_TAG)
        editor.putString("token", BuildConfig.WR_TOKEN)
        editor.apply()
        ObserverJobService.startScans(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        addPoints()
        addSavedPlaces()
    }

    fun clearMarkers() {
        mMap.clear()
    }

    fun addSavedPlaces() {
        savedPlaces.forEach { p ->
            if (p.showOnMap) {
                val latLng = LatLng(p.lat, p.lng)
                var markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                markerOptions.title(p.name)
                mMap.addMarker(markerOptions)
                mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
                    override fun onMarkerClick(p0: Marker?): Boolean {
                        if (p0 != null) {
                            p0.showInfoWindow()
                        }
                        return false
                    }
                })
            }
        }
    }

    fun addPoints() {
        val storage = applicationContext.getSharedPreferences(Utils.LOCATION_STORAGE_NAME, 0)
        val locObjects = storage.getString(Utils.LOCATION_STORAGE_KEY, null)
        val mapper = jacksonObjectMapper()
        val locObjectList: MutableList<LocationObject>
        if (locObjects != null) {
            locObjectList = mapper.readValue<MutableList<LocationObject>>(locObjects, object : TypeReference<MutableList<LocationObject>>() {})
        } else {
            locObjectList = mutableListOf()
        }
        var rectOptions = PolylineOptions()
        rectOptions.color(Color.argb(255, 63, 81, 181))
        try {
            mMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            Log.d(TAG, "Can't get user's current location because permissions aren't granted")
        }
        var lastLatLng: LatLng
        locObjectList.forEach { locObj ->
            lastLatLng = LatLng(locObj.lat, locObj.lng)
            var markerOptions = MarkerOptions()
            markerOptions.position(lastLatLng)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLatLng))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15F))
            markerOptions.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            markerOptions.title("" + locObj.date)
            mMap.addMarker(markerOptions)

            mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
                override fun onMarkerClick(p0: Marker?): Boolean {
                    if (p0 != null) {
                        p0.showInfoWindow()
                    }
                    return false
                }
            })
            rectOptions.add(lastLatLng)
        }
        mMap.addPolyline(rectOptions)
    }

    fun getSavedLocations() {
        val storage = applicationContext.getSharedPreferences(Utils.SAVED_PLACES_STORAGE_NAME, 0)
        val savedPlacesString = storage.getString(Utils.SAVED_PLACES_STORAGE_KEY, null)
        val mapper = jacksonObjectMapper()
        val places: MutableList<SavedPlace>
        if (savedPlacesString != null) {
            places = mapper.readValue<MutableList<SavedPlace>>(savedPlacesString, object : TypeReference<MutableList<SavedPlace>>() {})
        } else {
            places = mutableListOf()
        }
        savedPlaces = places
        savedPlacesList = saved_places_list
        val adapter = SavedPlaceListAdapter(savedPlaces, applicationContext)
        savedPlacesList.adapter = adapter
        savedPlacesList.isClickable = true
        savedPlacesList.onItemClickListener = this

        savedPlacesList.setOnItemClickListener{_, _, pos, _ ->
            val placeDetailDialog = PlaceDetailDialogFragment.newInstance(pos)
            placeDetailDialog.show(fragmentManager, "dialog")
            drawer_layout.closeDrawer(Gravity.START)
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val placeDetailDialog = PlaceDetailDialogFragment.newInstance(position)
        placeDetailDialog.show(fragmentManager, "dialog")
        drawer_layout.closeDrawer(Gravity.START)
    }

    private fun areLocUpdatesOn(): Boolean {
        val jobScheduler = applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val allJobs = jobScheduler.allPendingJobs
        if (allJobs.size != 0) {
            allJobs.forEach { j ->
                val id = j.id
                if (id == LocationJobScheduler.JOB_ID) {
                    return true
                }
            }
        }
        return false
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setMessage("We need location permissions to map your recent locations")
                        .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { _, _ ->
                            ActivityCompat.requestPermissions(this,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    MY_PERMISSIONS_REQUEST_FINE_LOCATION)
                        }).setNegativeButton(android.R.string.cancel, null)

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            if (!areLocUpdatesOn()) {
                val ljs = LocationJobScheduler()
                ljs.startUpdates(applicationContext)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (!areLocUpdatesOn()) {
                        val ljs = LocationJobScheduler()
                        ljs.startUpdates(applicationContext)
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
    }
}
