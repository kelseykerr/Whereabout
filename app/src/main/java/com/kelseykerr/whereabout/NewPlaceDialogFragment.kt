package com.kelseykerr.whereabout

import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*


/**
 * Created by kelseykerr on 11/15/17.
 */
class NewPlaceDialogFragment : DialogFragment() {

    var lat: Double = 0.0
    var lng: Double = 0.0
    lateinit private var nameTextInput: TextInputLayout
    lateinit private var nameField: EditText
    lateinit private var addressTextInput: TextInputLayout
    lateinit private var addressField: EditText
    lateinit private var showOnMapCheckbox: CheckBox
    lateinit private var latTextInput: TextInputLayout
    lateinit private var lngTextInput: TextInputLayout
    lateinit private var latField: EditText
    lateinit private var lngField: EditText
    lateinit private var saveBtn: Button
    lateinit private var cancelBtn: Button
    private lateinit var mContext: Context

    companion object {
        fun newInstance(lat: Double, lng: Double): NewPlaceDialogFragment {
            val f = NewPlaceDialogFragment()
            val args = Bundle()
            args.putDouble("lat", lat)
            args.putDouble("lng", lng)
            f.arguments = args
            return f
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }
        return dialog
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lat = arguments.getDouble("lat")
        lng = arguments.getDouble("lng")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val window = getDialog().window
            val size = Point()
            val display = window.windowManager.defaultDisplay
            display.getSize(size)
            val width = size.x
            val height = size.y
            dialog.window.setLayout((width * 0.90).toInt(), (height * 0.70).toInt())
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.new_place_dialog, container, false)
        nameTextInput = v.findViewById(R.id.name_input_layout)
        nameField = v.findViewById(R.id.place_name)
        addressTextInput = v.findViewById(R.id.address_input_layout)
        addressField = v.findViewById(R.id.place_address)
        addressField.setText(getAddressFromLatLng())
        showOnMapCheckbox = v.findViewById(R.id.show_on_map)
        showOnMapCheckbox.setOnClickListener({v ->
            //don't need to do anything
        })
        latTextInput = v.findViewById(R.id.lat_input_layout)
        latField = v.findViewById(R.id.place_lat)
        latField.keyListener = null
        lngTextInput = v.findViewById(R.id.lng_input_layout)
        lngField = v.findViewById(R.id.place_lng)
        lngField.keyListener = null
        lngField.setText(lng.toString())
        latField.setText(lat.toString())
        cancelBtn = v.findViewById(R.id.cancel_btn)
        saveBtn = v.findViewById(R.id.save_btn)

        cancelBtn.setOnClickListener({ dismiss() })
        saveBtn.setOnClickListener({
            val newPlace = SavedPlace()
            val name = nameField.text.toString()
            val address = addressField.text.toString()
            val lat = latField.text.toString().toDouble()
            val lng = lngField.text.toString().toDouble()
            val showOnMap = showOnMapCheckbox.isChecked
            newPlace.name = name
            newPlace.address = address
            newPlace.lat = lat
            newPlace.lng = lng
            newPlace.showOnMap = showOnMap
            MapActivity.savedPlaces.add(newPlace)
            writeSavedPlaces()
            dismiss()
        })

        return v
    }

    private fun getAddressFromLatLng(): String {
        val addresses: List<Address>
        val geocoder = Geocoder(mContext, Locale.getDefault())

        addresses = geocoder.getFromLocation(lat, lng, 1)
        Log.d("*address*", addresses[0].toString())
        val address = addresses[0].getAddressLine(0)
        val city = addresses[0].getLocality()
        val state = addresses[0].getAdminArea()
        val country = addresses[0].getCountryName()
        val postalCode = addresses[0].getPostalCode()
        val knownName = addresses[0].getFeatureName()
        return address
    }

    private fun writeSavedPlaces() {
        val storage = mContext.getSharedPreferences(Utils.SAVED_PLACES_STORAGE_NAME, 0)
        val editor = storage.edit()
        val mapper = jacksonObjectMapper()
        val savedPlacesObjString = mapper.writeValueAsString(MapActivity.savedPlaces)
        editor.putString(Utils.LOCATION_STORAGE_KEY, savedPlacesObjString)
        editor.apply()
    }


}