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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
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
    lateinit private var progressSpinner: ProgressBar
    lateinit private var progressText: TextView
    lateinit private var spinnerLayout: RelativeLayout

    companion object {
        fun newInstance(lat: Double, lng: Double): NewPlaceDialogFragment {
            val f = NewPlaceDialogFragment()
            val args = Bundle()
            args.putDouble("lat", lat)
            args.putDouble("lng", lng)
            f.arguments = args
            return f
        }
        val TAG = "NewPlaceDialog"
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
        addressField.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                if (!addressField.text.toString().isEmpty()) {
                    val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    getLatLngFromAddress()
                }
                false
            } else {
                false
            }

        }
        showOnMapCheckbox = v.findViewById(R.id.show_on_map)
        showOnMapCheckbox.setOnClickListener({ /*don't need to do anything*/ })
        latTextInput = v.findViewById(R.id.lat_input_layout)
        latField = v.findViewById(R.id.place_lat)
        latField.keyListener = null
        lngTextInput = v.findViewById(R.id.lng_input_layout)
        lngField = v.findViewById(R.id.place_lng)
        lngField.keyListener = null
        lngField.setText(lng.toString())
        latField.setText(lat.toString())
        spinnerLayout = v.findViewById(R.id.address_spinner)
        progressSpinner = v.findViewById(R.id.progress_bar)
        progressText = v.findViewById(R.id.progress_bar_text)
        progressSpinner.visibility = View.GONE
        progressText.visibility = View.GONE
        spinnerLayout.visibility = View.GONE
        cancelBtn = v.findViewById(R.id.cancel_btn)
        saveBtn = v.findViewById(R.id.save_btn)
        cancelBtn.setOnClickListener({ dismiss() })
        saveBtn.setOnClickListener({
            if (validateFields()) {
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
                (activity as MapActivity).getSavedLocations()

                dismiss()
            }
        })

        return v
    }

    private fun validateFields(): Boolean {
        var formValid = true
        val name = nameField.text.toString()
        if (name.isEmpty()) {
            formValid = false
            nameTextInput.error = "You must enter a name"
        } else {
            nameTextInput.error = null
        }
        val address = addressField.text.toString()
        if (address.isEmpty()) {
            formValid = false
            addressTextInput.error = "You must enter an address"
        } else {
            addressTextInput.error = null
        }
        val lat = latField.text.toString()
        if (lat.isEmpty()) {
            formValid = false
            latTextInput.error = "Address must have a valid latitude"
        } else {
            latTextInput.error = null
        }
        val lng = lngField.text.toString()
        if (lng.isEmpty()) {
            formValid = false
            lngTextInput.error = "Address must have a valid longitude"
        }
        return formValid
    }

    private fun getLatLngFromAddress(): Boolean {
        val address = addressField.text.toString()
        Log.d(TAG, "finding lat, lng from address: " + address)
        if (address.isEmpty()) {
            return false
        }
        progressSpinner.visibility = View.VISIBLE
        progressText.visibility = View.VISIBLE
        spinnerLayout.visibility = View.VISIBLE
        Log.d(TAG, "displaying spinner")
        val geocoder = Geocoder(mContext, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(address, 1)
        if (addresses.isEmpty()) {
            return false
        }
        val gAddress = addresses[0]
        Log.d(TAG, "found lat, lng [" + gAddress.latitude.toString() + ", "
                + gAddress.longitude.toString() + "]")
        latField.setText(gAddress.latitude.toString())
        lngField.setText(gAddress.longitude.toString())
        Log.d(TAG, "hiding spinner")
        progressSpinner.visibility = View.GONE
        progressText.visibility = View.GONE
        spinnerLayout.visibility = View.GONE
        return true
    }

    private fun getAddressFromLatLng(): String {
        val addresses: List<Address>
        val geocoder = Geocoder(mContext, Locale.getDefault())

        addresses = geocoder.getFromLocation(lat, lng, 1)
        Log.d(TAG, "Address from current lat, lng:" + addresses[0].toString())
        return addresses[0].getAddressLine(0)
        /*val city = addresses[0].getLocality()
        val state = addresses[0].getAdminArea()
        val country = addresses[0].getCountryName()
        val postalCode = addresses[0].getPostalCode()
        val knownName = addresses[0].getFeatureName()*/
    }

    private fun writeSavedPlaces() {
        val storage = mContext.getSharedPreferences(Utils.SAVED_PLACES_STORAGE_NAME, 0)
        val editor = storage.edit()
        val mapper = jacksonObjectMapper()
        val savedPlacesObjString = mapper.writeValueAsString(MapActivity.savedPlaces)
        Log.d(TAG, "saving places: " + savedPlacesObjString)
        editor.putString(Utils.SAVED_PLACES_STORAGE_KEY, savedPlacesObjString)
        editor.apply()
    }


}