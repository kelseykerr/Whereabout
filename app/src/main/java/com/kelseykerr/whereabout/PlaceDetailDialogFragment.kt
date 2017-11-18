package com.kelseykerr.whereabout

import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * Created by kelseykerr on 11/18/17.
 */
class PlaceDetailDialogFragment : DialogFragment() {

    lateinit private var showOnMapCheckbox: CheckBox
    lateinit private var placeName: TextView
    lateinit private var placeAddress: TextView
    lateinit private var placeLatLng: TextView
    lateinit private var okBtn: Button
    lateinit private var deleteBtn: Button
    lateinit private var savedPlace: SavedPlace
    private lateinit var mContext: Context

    var position: Int = 0


    companion object {
        fun newInstance(position: Int): PlaceDetailDialogFragment {
            val f = PlaceDetailDialogFragment()
            val args = Bundle()
            args.putInt("position", position)
            f.arguments = args
            return f
        }

        val TAG = "PlaceDetailDialog"
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
        position = arguments.getInt("position")
        savedPlace = MapActivity.savedPlaces[position]
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
            dialog.window.setLayout((width * 0.90).toInt(), (height * 0.60).toInt())
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.place_detail_dialog, container, false)
        placeName = v.findViewById(R.id.place_name)
        placeName.text = savedPlace.name
        placeAddress = v.findViewById(R.id.place_address)
        placeAddress.text = savedPlace.address
        placeLatLng = v.findViewById(R.id.place_lat_lng)
        placeLatLng.text = String.format(resources.getString(R.string.lat_lng_string), savedPlace.lat, savedPlace.lng)
        showOnMapCheckbox = v.findViewById(R.id.show_on_map)
        showOnMapCheckbox.isChecked = savedPlace.showOnMap
        showOnMapCheckbox.setOnClickListener({ /*don't need to do anything*/ })
        deleteBtn = v.findViewById(R.id.delete_btn)
        okBtn = v.findViewById(R.id.ok_btn)
        deleteBtn.setOnClickListener({
            MapActivity.savedPlaces.removeAt(position)
            writeSavedPlaces()
            dismiss()
        })
        okBtn.setOnClickListener({
            val showOnMap = showOnMapCheckbox.isChecked
            if (showOnMap != savedPlace.showOnMap) {
                savedPlace.showOnMap = showOnMap
                MapActivity.savedPlaces.add(position, savedPlace)
                writeSavedPlaces()
            }
            dismiss()
        })

        return v
    }


    private fun writeSavedPlaces() {
        val storage = mContext.getSharedPreferences(Utils.SAVED_PLACES_STORAGE_NAME, 0)
        val editor = storage.edit()
        val mapper = jacksonObjectMapper()
        val savedPlacesObjString = mapper.writeValueAsString(MapActivity.savedPlaces)
        Log.d(NewPlaceDialogFragment.TAG, "saving places: " + savedPlacesObjString)
        editor.putString(Utils.SAVED_PLACES_STORAGE_KEY, savedPlacesObjString)
        editor.apply()
    }

}

