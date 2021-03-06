package com.kelseykerr.whereabout

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.kelseykerr.whereabout.models.SavedPlace

/**
 * Created by kelseykerr on 11/16/17.
 */
class SavedPlaceListAdapter(private var places: List<SavedPlace>, context: Context): BaseAdapter() {
    private var mContext: Context = context

    companion object {
        const val TAG = "SavedPlaceAdapter"
    }

    override fun getItem(position: Int) = places[position]

    override fun getItemId(position: Int) = 1L

    override fun getCount() = places.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        Log.d(TAG, "getView")
        var cView = convertView
        if (cView == null) {
            cView = LayoutInflater.from(mContext).inflate(R.layout.saved_place_row, parent, false)
        }
        val place = places[position]
        val nameText = cView?.findViewById<TextView>(R.id.place_name)
        val addressText = cView?.findViewById<TextView>(R.id.place_address)
        nameText?.text = place.name
        addressText?.text = place.address
        return cView!!
    }

}